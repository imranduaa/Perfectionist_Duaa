#include <Arduino.h>
#include <math.h>
#include <Arduino_LSM6DS3.h>

/*
  1-SENSOR POSTURE ML (LIVE IMU)
  Serial Monitor @115200:

  N = record NORMAL 8s (live IMU)
  I = record IDEAL  8s (live IMU) + auto-set motion threshold from stillness
  L = live mode ON (prints + classifies)
  S = live mode OFF
*/

struct PrototypeVec {
  float mean[4];
  float stdev[4];
  bool ready;
};

enum State { GOOD=0, BAD_PENDING=1, VIBRATING=2, COOLDOWN=3 };

// ---------- Pins / Timing ----------
const int VIB_PIN = 6;

const unsigned long SAMPLE_MS   = 200;   // 5 samples/sec
const unsigned long BAD_HOLD_MS = 3000;  // must be bad for 3 sec
const unsigned long COOLDOWN_MS = 7000;  // cooldown after vibration

const float START_MARGIN = 0.25f;
const float STOP_MARGIN  = 0.10f;

// ---------- Globals ----------
PrototypeVec P_normal = { {0,0,0,0}, {1,1,1,1}, false };
PrototypeVec P_ideal  = { {0,0,0,0}, {1,1,1,1}, false };

float motion_threshold = 999.0f; // auto-set during IDEAL recording
bool motionHigh = false;

State state = GOOD;
unsigned long lastSample = 0;
unsigned long badStart = 0;
unsigned long cooldownStart = 0;

bool liveMode = false;

// for feature vector change terms
bool havePrev = false;
float prevPitch = 0.0f, prevRoll = 0.0f;

// ---------- Helpers ----------
static void computePitchRoll(float ax, float ay, float az, float &pitchDeg, float &rollDeg) {
  rollDeg  = atan2f(ay, az) * 180.0f / M_PI;
  pitchDeg = atan2f(-ax, sqrtf(ay * ay + az * az)) * 180.0f / M_PI;
}

static const char* stateLabel(State s) {
  switch(s) {
    case GOOD: return "GOOD";
    case BAD_PENDING: return "BAD_PENDING";
    case VIBRATING: return "VIBRATING";
    case COOLDOWN: return "COOLDOWN";
  }
  return "?";
}

static void setVibration(bool on) {
  digitalWrite(VIB_PIN, on ? HIGH : LOW);
  digitalWrite(LED_BUILTIN, on ? HIGH : LOW);
}

static bool readPitchRoll(float &pitch, float &roll) {
  if (!IMU.accelerationAvailable()) return false;
  float ax, ay, az;
  IMU.readAcceleration(ax, ay, az);
  computePitchRoll(ax, ay, az, pitch, roll);
  return true;
}

// Motion metric: prefer gyro magnitude; fallback to accel magnitude deviation
static float readCurrentMotion() {
  if (IMU.gyroscopeAvailable()) {
    float gx, gy, gz;
    IMU.readGyroscope(gx, gy, gz);
    return sqrtf(gx*gx + gy*gy + gz*gz);
  }

  if (IMU.accelerationAvailable()) {
    float ax, ay, az;
    IMU.readAcceleration(ax, ay, az);
    float amag = sqrtf(ax*ax + ay*ay + az*az); // ~1.0 when still
    return fabsf(amag - 1.0f) * 10.0f;
  }

  return 0.0f;
}

/*
  4D feature vector from ONE sensor:
  x[0] = pitch
  x[1] = roll
  x[2] = dPitch (change since last sample)
  x[3] = dRoll  (change since last sample)
*/
static bool readCurrentFeatureVec(float x[4], float &pitchOut, float &rollOut) {
  float pitch, roll;
  if (!readPitchRoll(pitch, roll)) return false;

  float dP = 0.0f, dR = 0.0f;
  if (havePrev) {
    dP = pitch - prevPitch;
    dR = roll  - prevRoll;
  } else {
    havePrev = true;
  }

  prevPitch = pitch;
  prevRoll  = roll;

// STABLE 4D features (no prev needed)
  x[0] = pitch;
  x[1] = roll;
  x[2] = pitch * pitch;
  x[3] = roll  * roll;

  pitchOut = pitch;
  rollOut  = roll;
  return true;
}

static float normDist4(const float x[4], const PrototypeVec &P) {
  float sum = 0.0f;
  for (int i=0;i<4;i++) {
    float sd = (P.stdev[i] < 0.001f) ? 0.001f : P.stdev[i];
    float z = (x[i] - P.mean[i]) / sd;
    sum += z*z;
  }
  return sqrtf(sum);
}

// Record NORMAL or IDEAL prototypes for 8 seconds using live IMU.
// If recordMotionToo == true, we also compute motion_threshold from stillness.
static void recordPrototype8s(PrototypeVec &P, const char *label, bool recordMotionToo) {
  const unsigned long DURATION_MS = 8000;

  float sum[4]   = {0,0,0,0};
  float sumsq[4] = {0,0,0,0};
  int n = 0;

  float maxStillMotion = 0.0f;

  unsigned long start = millis();
  while (millis() - start < DURATION_MS) {
    float x[4];
    float pitch=0, roll=0;
    if (readCurrentFeatureVec(x, pitch, roll)) {
      for (int i=0;i<4;i++) { sum[i] += x[i]; sumsq[i] += x[i]*x[i]; }
      n++;

      if (recordMotionToo) {
        float m = readCurrentMotion();
        if (m > maxStillMotion) maxStillMotion = m;
      }
    }
    delay(SAMPLE_MS);
  }

  if (n < 5) {
    Serial.println("⚠️ Not enough IMU samples captured. Try again.");
    return;
  }

  for (int i=0;i<4;i++) {
    float mean = sum[i] / n;
    float var  = (sumsq[i] / n) - (mean*mean);
    if (var < 1e-6f) var = 1e-6f;
    P.mean[i]  = mean;
    P.stdev[i] = sqrtf(var);
  }
  P.ready = true;

  Serial.print("✅ Recorded "); Serial.print(label);
  Serial.print(" prototype. Samples="); Serial.println(n);

  if (recordMotionToo) {
    motion_threshold = maxStillMotion * 2.0f;
    Serial.print("✅ motion_threshold (auto from IDEAL stillness) = ");
    Serial.println(motion_threshold, 2);
  }
}

static void liveStepOnce() {
  if (!P_normal.ready || !P_ideal.ready) {
    Serial.println("⚠️ Record BOTH prototypes first: N then I");
    return;
  }
  if (motion_threshold > 900.0f) {
    Serial.println("⚠️ Motion threshold not set yet. Press I to record IDEAL (it auto-sets motion).");
    return;
  }

  float x[4];
  float pitch=0, roll=0;
  if (!readCurrentFeatureVec(x, pitch, roll)) return;

  float motion = readCurrentMotion();
  motionHigh = (motion > motion_threshold);
  if (motionHigh) {
  Serial.println("🚫 High motion → vibration blocked");
}

  float dIdeal = normDist4(x, P_ideal);
  float dNorm  = normDist4(x, P_normal);

  bool looksBad  = (!motionHigh) && (dNorm + START_MARGIN < dIdeal);
  bool looksGood = (!motionHigh) && (dIdeal + STOP_MARGIN < dNorm);

  unsigned long now = millis();

  switch(state) {
    case GOOD:
      if (!motionHigh && looksBad) { state = BAD_PENDING; badStart = now; Serial.println("⚠️ Bad posture → BAD_PENDING (waiting 3s)");}
      break;

    case BAD_PENDING:
      if (motionHigh || looksGood) state = GOOD;
      else if (now - badStart >= BAD_HOLD_MS) {
        state = VIBRATING;
        setVibration(true);
        Serial.println("🔔 VIBRATION");  // <-- what you asked for
      }
      break;

    case VIBRATING:
      // keep vibration on briefly (visual), then stop and cooldown
      setVibration(false);
      state = COOLDOWN;
      cooldownStart = now;
      Serial.println("🧊 COOLDOWN started (7s)");
      break;

    case COOLDOWN:
      if (now - cooldownStart >= COOLDOWN_MS) state = GOOD;
      break;
  }

  // Live debug printing (readings + decision)
  Serial.print("pitch="); Serial.print(pitch,2);
  Serial.print(" roll="); Serial.print(roll,2);
  Serial.print(" | motion="); Serial.print(motion,2);
  Serial.print(motionHigh ? " (HIGH) " : " (low) ");
  Serial.print("| dIdeal="); Serial.print(dIdeal,2);
  Serial.print(" dNorm="); Serial.print(dNorm,2);
  Serial.print(" -> "); Serial.print(dIdeal < dNorm ? "IDEAL" : "NORMAL");
  Serial.print(" | state="); Serial.println(stateLabel(state));
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(VIB_PIN, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  setVibration(false);

  if (!IMU.begin()) {
    Serial.println("❌ IMU init failed. Check Arduino_LSM6DS3 + board selection.");
    while(1) {}
  }

  Serial.println("✅ IMU OK");
  Serial.println("=== 1-Sensor Posture ML (LIVE IMU) ===");
  Serial.println("Commands: N=Normal, I=Ideal(+motion), L=Live ON, S=Live OFF");
}

void loop() {
  if (Serial.available() > 0) {
    char c = Serial.read();

    if (c=='N' || c=='n') {
      Serial.println("Recording NORMAL 8s... (hold your NORMAL posture)");
      recordPrototype8s(P_normal, "NORMAL", false);
    }
    else if (c=='I' || c=='i') {
      Serial.println("Recording IDEAL 8s... (stand straight + hold still)");
      recordPrototype8s(P_ideal, "IDEAL", true); // auto motion threshold here
    }
    else if (c=='L' || c=='l') {
      Serial.println("LIVE ON");
      liveMode = true;
      state = GOOD;                 // reset state
      setVibration(false);          // ensure outputs off
      cooldownStart = 0;            // optional reset
        }
    else if (c=='S' || c=='s') {
      Serial.println("LIVE OFF");
      liveMode = false;
      setVibration(false);     // make sure outputs are off
      state = GOOD;            // reset state for clean demo
    }

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