#include <Arduino_LSM6DS3.h>
#include <math.h>

float basePitch = 0.0f;
float baseRoll  = 0.0f;
bool baselineSet = false;
//initial vals

static void computePitchRoll(float ax, float ay, float az, float &pitchDeg, float &rollDeg) {
  rollDeg  = atan2f(ay, az) * 180.0f / M_PI;
  pitchDeg = atan2f(-ax, sqrtf(ay * ay + az * az)) * 180.0f / M_PI;
}
//generic degree computations, main calls this every second 

static void clearSerialInput() {
  while (Serial.available()) Serial.read();
}
//clears serial monitor

static void waitForUserUprightAndCalibrate() {
  Serial.println();
  Serial.println("I will now take your optimal posture readings.");
  Serial.println("Please stand straight against a wall.");
  Serial.println("Please type Y and Enter to confirm this is your optimal posture");
  Serial.println("If you want to change your optimal posture readings, press R and Enter");

  //simple prompts for the user. the user is supposed to stand against a wall, but the user has to be able to follow the directions
  //i probably could change this so that it can at least detect when the user is laying down, and say this is not the correct approach

  clearSerialInput();

  while (true) {
    if (Serial.available()) {
      char c = Serial.read();
      if (c == 'Y' || c == 'y') break;
      if (c == 'R' || c == 'r') {
        Serial.println("Taking new optimal posture readings, please press Y and Enter to confirm.");
      }
    }
  }

  const int N = 80;
  float sumPitch = 0.0f, sumRoll = 0.0f;
  //useful ints to reduce noise and unwanted signals

  Serial.println("Taking optimal posture readings, please stand still.");

  for (int i = 0; i < N; i++) {
    float ax, ay, az;

    while (!IMU.accelerationAvailable()) {}
    IMU.readAcceleration(ax, ay, az);

    float pitch, roll;
    computePitchRoll(ax, ay, az, pitch, roll);

    sumPitch += pitch;
    sumRoll  += roll;
    delay(10); // this should compute pitch and roll for every 1 second, change this val for higher or lower polling
  }

  basePitch = sumPitch / N;
  baseRoll  = sumRoll  / N;
  baselineSet = true;

  Serial.print("Readings are successful. Pitch=");
  Serial.print(basePitch, 2);
  Serial.print(" deg, Roll=");
  Serial.print(baseRoll, 2);
  Serial.println(" deg");
  Serial.println("These are the degree values for deviation in posture.");
  Serial.println("If you'd like to reset, press Z and Enter.");
  clearSerialInput();
}

void setup() {
  Serial.begin(115200);
  while (!Serial) {}

  if (!IMU.begin()) {
    Serial.println("IMU init failure, make sure you downloaded the proper libraries above.");
    while (1) {}
  }

  Serial.println("IMU good to read");
  waitForUserUprightAndCalibrate();
}
//simple tests to make sure initialization would go smoothly

void loop() {
  // Allow re-zero at any time
  if (Serial.available()) {
    char c = Serial.read();
    if (c == 'Z' || c == 'z') {
      baselineSet = false;
      waitForUserUprightAndCalibrate();
      return;
    }
  }
  //this is the reading reset. this allows the user to redo the readings.

  if (!baselineSet) return;

  float ax, ay, az;
  if (!IMU.accelerationAvailable()) return;
  IMU.readAcceleration(ax, ay, az);

  float pitch, roll;
  computePitchRoll(ax, ay, az, pitch, roll);

  float dPitch = pitch - basePitch;
  float dRoll  = roll  - baseRoll;

  //below is the formula to compute a single number to detect deviation from optimal posture
  float mag = sqrtf(dPitch * dPitch + dRoll * dRoll);
  
 
  Serial.print(dPitch, 2);
  Serial.print(" ");
  Serial.print(dRoll, 2);
  Serial.print(" ");
  Serial.println(mag, 2);

  delay(1000); // this updates every 1 second, along with the readings
}