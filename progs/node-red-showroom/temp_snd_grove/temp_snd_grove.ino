// Arduino Grove temperature + sound sensor program

// by Matthias Dieter Wallnöfer, TIS innovation park,
//                               Bolzano/Bozen - Italy
// year 2014
// This code is released under public domain.

const int tempSensorPin = A0;
const int sndSensorPin = A1;
const int B = 3975; // B value of the thermistor
const int del = 1000; // delay [ms]

void setup() {
  Serial.begin(9600); // open a serial port
}

void loop() {
  int sensorVal = analogRead(tempSensorPin);
  float resistance = (float)(1023-sensorVal)*10000/sensorVal;
  float temperature = 1/(log(resistance/10000)/B+1/298.15)-273.15;
  Serial.print("degrees C: ");
  Serial.print(temperature);

  sensorVal = analogRead(sndSensorPin);
  Serial.print(", sound: ");
  Serial.println(sensorVal);

  delay(del);
}

