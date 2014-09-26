// This #include statement was automatically added by the Spark IDE.
#include "HttpClient.h"

// This #include statement was automatically added by the Spark IDE.
#include "idDHT22/idDHT22.h"

#define auth_token "NDAwNmZjNWYtNTYxMS00YjVkLTllYzMtZTIxNTNhMzAwYjA0Njg5NjJjNDctMWUzNC00ZDY4LTgyZDItODkzMjVjNDI0YTli"
#define SO_ID "1404402098823d5f1bf416dfb47099137d22f16023602"


// declaration for DHT11 handler
int idDHT22pin = D4; //Digital pin for comunications
void dht22_wrapper(); // must be declared before the lib initialization

// DHT instantiate
idDHT22 DHT22(idDHT22pin, dht22_wrapper);


void setup()
{
	Serial.begin(9600);
	

	Serial.print("LIB version: ");
	Serial.println(idDHT22LIB_VERSION);
	
}
// This wrapper is in charge of calling
// mus be defined like this for the lib work
void dht22_wrapper() {
	DHT22.isrCallback();
}
void loop()
{

	Serial.print("\nRetrieving information from sensor: ");
	Serial.print("Read sensor: ");
	//delay(100);
	DHT22.acquire();
	while (DHT22.acquiring())
		;
	int result = DHT22.getStatus();
	switch (result)
	{
		case IDDHTLIB_OK:
			Serial.println("OK");
			break;
		case IDDHTLIB_ERROR_CHECKSUM:
			Serial.println("Error\n\r\tChecksum error");
			break;
		case IDDHTLIB_ERROR_ISR_TIMEOUT:
			Serial.println("Error\n\r\tISR Time out error");
			break;
		case IDDHTLIB_ERROR_RESPONSE_TIMEOUT:
			Serial.println("Error\n\r\tResponse time out error");
			break;
		case IDDHTLIB_ERROR_DATA_TIMEOUT:
			Serial.println("Error\n\r\tData time out error");
			break;
		case IDDHTLIB_ERROR_ACQUIRING:
			Serial.println("Error\n\r\tAcquiring");
			break;
		case IDDHTLIB_ERROR_DELTA:
			Serial.println("Error\n\r\tDelta time to small");
			break;
		case IDDHTLIB_ERROR_NOTSTARTED:
			Serial.println("Error\n\r\tNot started");
			break;
		default:
			Serial.println("Unknown error");
			break;
	}
	
	
	double var = DHT22.getCelsius();
	String temp(var);
	
	var = DHT22.getHumidity();
	String humi(var);
	
	updateSensor("sensor", "temperature",temp, "humidity", humi);
	
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
