#include <math.h>
#include <ArduinoBLE.h>
#include <Arduino_LSM6DS3.h>
//make sure you downmlaod arduino ble and arduino lsm6ds3 in the library before running code
//also make sure that you use the data transfer cable

BLEService imuService("180A");
BLEFloatCharacteristic magCharacteristic("2A57", BLERead | BLENotify);
BLEByteCharacteristic commandCharacteristic("2A58", BLEWrite);
//these are signifiers for the ble windows explorer, should come up as BraceIMU. see below

float basePitch = 0, baseRoll = 0;

void computePitchRoll(float ax, float ay, float az, float &pitch, float &roll) {
  roll  = atan2(ay, az) * 180.0 / PI;
  pitch = atan2(-ax, sqrt(ay*ay + az*az)) * 180.0 / PI;
}
//computes pitch and roll, this should be polling at whatever frequency value at the bottom of the program

void calibrate() {
  float ax, ay, az;
  IMU.readAcceleration(ax, ay, az);
  computePitchRoll(ax, ay, az, basePitch, baseRoll);
}
//initial calibration, takes user's current 'optimal' posture at the beginning

void setup() {
  IMU.begin();
  BLE.begin();

  BLE.setLocalName("BraceIMU");    //heres the name. change for testing purposes NOTE 2/20, for some reason the nano cant connect long term to ble explorer for whatever reason, trying to find that out
  BLE.setAdvertisedService(imuService);

  imuService.addCharacteristic(magCharacteristic);
  imuService.addCharacteristic(commandCharacteristic);
  BLE.addService(imuService);

  BLE.advertise();
  calibrate();
}
//this is the ble setup, allows for the device to be acknowledged and service ble connections

void loop() {
  BLEDevice central = BLE.central();

  if (central) {
    while (central.connected()) {

      if (commandCharacteristic.written()) {
        if (commandCharacteristic.value() == 'Z') {
          calibrate();
        }
      }

      float ax, ay, az;
      IMU.readAcceleration(ax, ay, az);

      float pitch, roll;
      computePitchRoll(ax, ay, az, pitch, roll);

      float mag = sqrt(pow(pitch - basePitch, 2) + pow(roll - baseRoll, 2));

      magCharacteristic.writeValue(mag);

      delay(50);
    }
  }
}