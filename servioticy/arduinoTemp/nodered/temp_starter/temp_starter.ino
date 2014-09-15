// Modified Arduino starter-kit temperature program which allows to control the three leds
// Supported code words to be sent over serial port (UART):
// led1on, led2on, led3on, led1off, led2off, led3off

// by Matthias Dieter Wallnöfer, TIS innovation park,
//                               Bolzano/Bozen - Italy
// year 2014
// This code is released under public domain.

const int sensorPin = A0;
const int del = 50000; // delay [ms]

void setup() {
	Serial.begin(9600); // open a serial port

	for (int pinNumber = 2; pinNumber < 5; ++pinNumber) {
		pinMode(pinNumber, OUTPUT);
		digitalWrite(pinNumber, LOW);
	}
}

void serialEvent() {
        // led control
        String inp = "";
        if (Serial.available() > 0) {
            char buff[10] = { '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0' };
            Serial.readBytes(buff, 6);
            if (buff[5] == 'f') buff[6] = Serial.read(); // append 'f'
            inp = buff;
        }
        if (inp.indexOf("led1on") != -1) {
            digitalWrite(2, HIGH);
        }
        if (inp.indexOf("led1off") != -1) {
            digitalWrite(2, LOW);
        }
        if (inp.indexOf("led2on") != -1) {
            digitalWrite(3, HIGH);
        }
        if (inp.indexOf("led2off") != -1) {
            digitalWrite(3, LOW);
        }
        if (inp.indexOf("led3on") != -1) {
            digitalWrite(4, HIGH);
        }
        if (inp.indexOf("led3off") != -1) {
            digitalWrite(4, LOW);
        }
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
	float temperature = (voltage - 0.5) * 100;

	Serial.print(", degrees C: ");
	Serial.println(temperature);

	delay(del);
}

