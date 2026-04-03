#include <Arduino.h>
#include <math.h>

/*
  FULL POSTURE ML SKELETON (SIMULATED NOW)
  Commands in Serial Monitor (115200):
  N = record NORMAL 8s
  I = record IDEAL  8s
  A = auto-set motion threshold 8s (hold still)
  L = start live printing
  S = stop live printing
*/

// ======================= TYPES =======================

struct Angles { float pitch; float roll; };

struct CurvatureFeatures {
  float dPitch_UM, dRoll_UM;
  float dPitch_ML, dRoll_ML;
  float dPitch_UL;
};

struct PrototypeVec {
  float mean[4];
  float stdev[4];
  bool ready;
};

enum State { GOOD=0, BAD_PENDING=1, VIBRATING=2, COOLDOWN=3 };

// ======================= PROTOTYPES (IMPORTANT!) =======================
// (This prevents Arduino from auto-generating broken prototypes)

Angles simulateAnglesUpper();
Angles simulateAnglesMiddle();
Angles simulateAnglesLower();

CurvatureFeatures computeCurvatureFrom3Sensors(const Angles &U, const Angles &M, const Angles &L);
void featuresToVec4(const CurvatureFeatures &f, float x[4]);

void readCurrentFeatureVec(float x[4]);
float normDist4(const float x[4], const PrototypeVec &P);
void recordPrototype8s(PrototypeVec &P, const char *label);

float simulateMotion();
float readCurrentMotion();
void autoSetMotionThresholdFromIdeal();

const char* stateLabel(State s);
void setVibration(bool on);
void liveStepOnce();

// 3 sensors -> pitch/roll -> curvature features 

// SIMULATED 
Angles simulateAnglesUpper() {
  float noiseP = random(-5, 6) * 0.1f;
  float noiseR = random(-5, 6) * 0.1f;
  return { 10.0f + noiseP, 1.0f + noiseR };
}
Angles simulateAnglesMiddle() {
  float noiseP = random(-5, 6) * 0.1f;
  float noiseR = random(-5, 6) * 0.1f;
  return { 7.0f + noiseP, 0.5f + noiseR };
}
Angles simulateAnglesLower() {
  float noiseP = random(-5, 6) * 0.1f;
  float noiseR = random(-5, 6) * 0.1f;
  return { 5.0f + noiseP, 0.2f + noiseR };
}

CurvatureFeatures computeCurvatureFrom3Sensors(const Angles &U, const Angles &M, const Angles &L) {
  CurvatureFeatures f;
  f.dPitch_UM = U.pitch - M.pitch;
  f.dRoll_UM  = U.roll  - M.roll;
  f.dPitch_ML = M.pitch - L.pitch;
  f.dRoll_ML  = M.roll  - L.roll;
  f.dPitch_UL = U.pitch - L.pitch;
  return f;
}

void featuresToVec4(const CurvatureFeatures &f, float x[4]) {
  x[0] = f.dPitch_UM;
  x[1] = f.dPitch_ML;
  x[2] = f.dRoll_UM;
  x[3] = f.dRoll_ML;
}

// ML prototypes (Normal vs Ideal) 

PrototypeVec P_normal = { {0,0,0,0}, {1,1,1,1}, false };
PrototypeVec P_ideal  = { {0,0,0,0}, {1,1,1,1}, false };

void readCurrentFeatureVec(float x[4]) {
  Angles U = simulateAnglesUpper();
  Angles M = simulateAnglesMiddle();
  Angles L = simulateAnglesLower();

  CurvatureFeatures f = computeCurvatureFrom3Sensors(U, M, L);
  featuresToVec4(f, x);
}

float normDist4(const float x[4], const PrototypeVec &P) {
  float sum = 0;
  for (int i=0;i<4;i++) {
    float sd = (P.stdev[i] < 0.001f) ? 0.001f : P.stdev[i];
    float z = (x[i] - P.mean[i]) / sd;
    sum += z*z;
  }
  return sqrt(sum);
}

void recordPrototype8s(PrototypeVec &P, const char *label) {
  const unsigned long DURATION_MS = 8000;
  const unsigned long SAMPLE_MS = 200;

  float sum[4]   = {0,0,0,0};
  float sumsq[4] = {0,0,0,0};
  int n = 0;

  unsigned long start = millis();
  while (millis() - start < DURATION_MS) {
    float x[4];
    readCurrentFeatureVec(x);

    for (int i=0;i<4;i++) { sum[i] += x[i]; sumsq[i] += x[i]*x[i]; }
    n++;
    delay(SAMPLE_MS);
  }

  for (int i=0;i<4;i++) {
    float mean = sum[i] / n;
    float var  = (sumsq[i] / n) - (mean*mean);
    if (var < 1e-6f) var = 1e-6f;
    P.mean[i]  = mean;
    P.stdev[i] = sqrt(var);
  }
  P.ready = true;

  Serial.print("Recorded "); Serial.print(label);
  Serial.print(" prototype. Samples="); Serial.println(n);
}

// Motion threshold (auto-set) 

float motion_threshold = 999.0f; // will be set by A
bool motionHigh = false;

float simulateMotion() {
  unsigned long t = millis() % 20000;
  if (t < 16000) return 0.9f + (random(-10, 11) * 0.01f);
  return 3.5f;
}

float readCurrentMotion() {
  return simulateMotion();
}

void autoSetMotionThresholdFromIdeal() {
  const unsigned long DURATION_MS = 8000;
  const unsigned long SAMPLE_MS = 200;

  float maxStill = 0.0f;
  unsigned long start = millis();
  while (millis() - start < DURATION_MS) {
    float m = readCurrentMotion();
    if (m > maxStill) maxStill = m;
    delay(SAMPLE_MS);
  }
  motion_threshold = maxStill * 2.0f;
  Serial.print("motion_threshold = ");
  Serial.println(motion_threshold, 2);
}

//False alarms

const int VIB_PIN = 6;

const unsigned long SAMPLE_MS = 200;
const unsigned long BAD_HOLD_MS = 3000;
const unsigned long COOLDOWN_MS = 7000;

const float START_MARGIN = 0.25f;
const float STOP_MARGIN  = 0.10f;

State state = GOOD;

unsigned long lastSample = 0;
unsigned long badStart = 0;
unsigned long cooldownStart = 0;

bool liveMode = false;

const char* stateLabel(State s) {
  switch(s) {
    case GOOD: return "GOOD";
    case BAD_PENDING: return "BAD_PENDING";
    case VIBRATING: return "VIBRATING";
    case COOLDOWN: return "COOLDOWN";
  }
  return "?";
}

void setVibration(bool on) {
  digitalWrite(VIB_PIN, on ? HIGH : LOW);
  digitalWrite(LED_BUILTIN, on ? HIGH : LOW);
}

void liveStepOnce() {
  if (!P_normal.ready || !P_ideal.ready) {
    Serial.println("Record BOTH prototypes first: N then I");
    return;
  }
  if (motion_threshold > 900.0f) {
    Serial.println("Set motion threshold first: press A");
    return;
  }

  float x[4];
  readCurrentFeatureVec(x);

  float motion = readCurrentMotion();
  motionHigh = (motion > motion_threshold);

  float dIdeal = normDist4(x, P_ideal);
  float dNorm  = normDist4(x, P_normal);

  bool looksBad  = (!motionHigh) && (dNorm + START_MARGIN < dIdeal);
  bool looksGood = (!motionHigh) && (dIdeal + STOP_MARGIN < dNorm);

  unsigned long now = millis();

  switch(state) {
    case GOOD:
      if (!motionHigh && looksBad) { state = BAD_PENDING; badStart = now; }
      break;

    case BAD_PENDING:
      if (motionHigh || looksGood) state = GOOD;
      else if (now - badStart >= BAD_HOLD_MS) { state = VIBRATING; setVibration(true); }
      break;

    case VIBRATING:
      setVibration(false);
      state = COOLDOWN;
      cooldownStart = now;
      break;

    case COOLDOWN:
      if (now - cooldownStart >= COOLDOWN_MS) state = GOOD;
      break;
  }

  Serial.print("motion="); Serial.print(motion,2);
  Serial.print(motionHigh ? " (HIGH) " : " (low) ");
  Serial.print("dIdeal="); Serial.print(dIdeal,2);
  Serial.print(" dNorm="); Serial.print(dNorm,2);
  Serial.print(" -> "); Serial.print(dIdeal < dNorm ? "IDEAL" : "NORMAL");
  Serial.print(" | state="); Serial.println(stateLabel(state));
}

void setup() {
  Serial.begin(115200);
  delay(1000);
  randomSeed(analogRead(A0));

  pinMode(VIB_PIN, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  setVibration(false);

  Serial.println("=== Posture ML Skeleton (SIM now, REAL later) ===");
  Serial.println("Commands: N=Normal, I=Ideal, A=Auto motion thr, L=Live ON, S=Live OFF");
}

void loop() {
  if (Serial.available() > 0) {
    char c = Serial.read();

    if (c=='N' || c=='n') { Serial.println("Recording NORMAL 8s..."); recordPrototype8s(P_normal, "NORMAL"); }
    else if (c=='I' || c=='i') { Serial.println("Recording IDEAL 8s..."); recordPrototype8s(P_ideal, "IDEAL"); }
    else if (c=='A' || c=='a') { Serial.println("Auto-setting motion threshold 8s..."); autoSetMotionThresholdFromIdeal(); }
    else if (c=='L' || c=='l') { Serial.println("LIVE ON"); liveMode = true; }
    else if (c=='S' || c=='s') { Serial.println("LIVE OFF"); liveMode = false; }

    while (Serial.available()) Serial.read();
  }

  if (liveMode) {
    unsigned long now = millis();
    if (now - lastSample >= SAMPLE_MS) {
      lastSample = now;
      liveStepOnce();
    }
  }
}