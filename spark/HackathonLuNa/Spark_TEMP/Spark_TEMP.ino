// This #include statement was automatically added by the Spark IDE.
#include "HttpClient.h"

#define auth_token "XXXYYYYY"
#define SO_ID "XXXX"

void setup() {
    Serial.begin(9600);
    
    delay(2000);
    
    Serial.println("starting...");
    
    // Connect the temperature sensor to A0 and configure it
    // to be an input
    pinMode(A0, INPUT);
    

}

void loop() {
    int read = analogRead(A0);
    double voltage = (read * 3.3)/4095;
    double temp = ((voltage - 0.5) * 100) - 100;
    String value(temp);
    Serial.println(value);
    
    Serial.println(updateSensor("temperature", "temperature", value));
    
    delay(5000);
    

}

String updateSensor(String stream, String channel, String value) {
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
    json.concat(channel);
    json.concat("\": {\"current-value\": \"");
    json.concat(value);
    json.concat("\"}}, \"lastUpdate\": ");
    json.concat(Time.now());
    json.concat("}");
    
    request.body = json;
    
    http.put(request, response, headers);
    
    return response.body;
    
}