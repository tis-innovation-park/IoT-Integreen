// This #include statement was automatically added by the Spark IDE.
#include "HttpClient.h"

// This #include statement was automatically added by the Spark IDE.
#include "Adafruit_BMP085/Adafruit_BMP085.h"

#define auth_token "NDAwNmZjNWYtNTYxMS00YjVkLTllYzMtZTIxNTNhMzAwYjA0Njg5NjJjNDctMWUzNC00ZDY4LTgyZDItODkzMjVjNDI0YTli"
#define SO_ID "140454735590212222f719ffc4f72983cd6c1158b0ee8"


/*
	Wiring
	------
	BMP085 Vcc to 3.3V
	BMP085 GND to GND
	BMP085 SCL to D1
	BMP085 SDA to D0
*/

Adafruit_BMP085 bmp;

// Initialize BMP085

void InitializeBMP085(){
	if (!bmp.begin()) {
		Serial.println("Could not find a valid BMP085 sensor, check wiring!");
		while (1) {}
	}
}

// Publish Pressure, Altitude
void PublishBMP085Info(){
    Serial.print("Temperature = ");
    Serial.print(bmp.readTemperature());
    Serial.println(" *C");
    
    Serial.print("Pressure = ");
    Serial.print(bmp.readPressure());
    Serial.println(" Pa");
    
    // Calculate altitude assuming 'standard' barometric
    // pressure of 1013.25 millibar = 101325 Pascal
    Serial.print("Altitude = ");
    Serial.print(bmp.readAltitude());
    Serial.println(" meters");

  // you can get a more precise measurement of altitude
  // if you know the current sea level pressure which will
  // vary with weather and such. If it is 1015 millibars
  // that is equal to 101500 Pascals.
    Serial.print("Real altitude = ");
    Serial.print(bmp.readAltitude(101500));
    Serial.println(" meters");
    
    //char szEventInfo[64];
    
    //sprintf(szEventInfo, "Temperature=%.2f °C, Pressure=%.2f hPa", bmp.readTemperature(), bmp.readPressure()/100.0);
    
    //Spark.publish("bmpo85info", szEventInfo);
    
    String temp(bmp.readTemperature());
    String alt(bmp.readAltitude());
    
    
    
    
    
    Serial.println(updateSensor("sensor", "temperature",temp, "altitude", alt));
}

// Initialize applicaiton
void InitializeApplication(){
    Serial.begin(9600);
	pinMode(D7, OUTPUT);
}

// Blink LED and wait for some time
void BlinkLED(){
    digitalWrite(D7, HIGH);   
    delay(500);
    digitalWrite(D7, LOW);   
    delay(500);
}

void setup() {
    InitializeApplication();
    
	InitializeBMP085();
}

void loop() {
    // Publish events. Wait for 2 second between publishes
    PublishBMP085Info(); 
    
    BlinkLED();   
	
	delay(5000);
}

String updateSensor(String stream, String channel1, String value1, String channel2, String value2) {
    HttpClient http;
    String path;

    http_header_t headers[] = {
      { "Content-Type", "application/json" },
      { "Authorization" , auth_token},
      { NULL, NULL } // NOTE: Always terminate headers will NULL
    };


    http_request_t request;
    http_response_t response;
    
    request.hostname = "api.servioticy.com";
    request.port = 80;
    
    path = "/";
    path.concat(SO_ID);
    path.concat("/streams/");
    path.concat(stream);
    
    request.path = path;
    
    String json = "{\"channels\": {\"";
    json.concat(channel1);
    json.concat("\": {\"current-value\": \"");
    json.concat(value1);
    json.concat("\"}, \"");
    json.concat(channel2);
    json.concat("\": {\"current-value\": \"");
    json.concat(value2);
    json.concat("\"}}, \"lastUpdate\": ");
    json.concat(Time.now());
    json.concat("}");
    
    request.body = json;
    
    
    http.put(request, response, headers);
    
    return response.body;
    
}