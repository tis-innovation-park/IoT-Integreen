// Modified Arduino Grove temperature program

// by Matthias Dieter Wallnöfer, TIS innovation park,
//                               Bolzano/Bozen - Italy
// year 2014
// This code is released under public domain.

const int sensorPin = A0;
const int B = 3975; // B value of the thermistor
const int del = 50000; // delay [ms]

void setup() {
	Serial.begin(9600); // open a serial port
}

void loop() {
	int sensorVal = analogRead(sensorPin);

	Serial.print("Sensor Value: ");
	Serial.print(sensorVal);

	// convert the ADC reading to voltage
	float voltage = (sensorVal / 1024.0) * 5.0;

	Serial.print(", Volts: ");
	Serial.print(voltage);

	// convert the voltage to temperature in degrees
        float resistance = (float)(1023-sensorVal)*10000/sensorVal;
        float temperature = 1/(log(resistance/10000)/B+1/298.15)-273.15;

	Serial.print(", degrees C: ");
	Serial.println(temperature);

	delay(del);
}

