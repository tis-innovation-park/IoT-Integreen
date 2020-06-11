// This #include statement was automatically added by the Spark IDE.
#include "HttpClient.h"

#define auth_token "XXXYYY"
#define SO_ID "XXXX"



int dht = D4;
int rh = 0;
int temp = 0;

int ms = 0;

void setup() 
{
    Serial.begin(9600);
    Spark.variable("rh", &rh, INT);
    Spark.variable("temp", &temp, INT);
    pinMode(dht, INPUT_PULLUP);
}


void loop() 
{
  if (millis() - ms > 5000) {
    read_dht(dht, &rh, &temp);
    
    Serial.print(temp);
    Serial.print(":");
    Serial.println(rh);
    ms = millis();
    
    String temperature(temp);
    String humi(rh);
    
    updateSensor("sensor", "temperature",temperature, "humidity", humi);
  }
}

int read_dht(int pin, int *humidity, int *temperature)
{
    uint8_t data[5] = {0, 0, 0, 0, 0};
    
    noInterrupts();
    pinMode(pin, OUTPUT);
    digitalWrite(pin, LOW);
    delay(20);
    pinMode(pin, INPUT_PULLUP);
    if (detect_edge(pin, HIGH, 10, 200) == -1) {
        return -1;
    }
    if (detect_edge(pin, LOW, 10, 200) == -1) {
        return -1;
    }
    if (detect_edge(pin, HIGH, 10, 200) == -1) {
        return -1;
    }
    for (uint8_t i = 0; i < 40; i++) {
        if (detect_edge(pin, LOW, 10, 200) == -1) {
            return -1;
        }
        int counter = detect_edge(pin, HIGH, 10, 200);
        if (counter == -1) {
            return -1;
        }
        data[i/8] <<= 1;
        if (counter > 4) {
            data[i/8] |= 1;
        }
    }
    interrupts();
    
    if (data[4] != ((data[0] + data[1] + data[2] + data[3]) & 0xFF)) {
        return -1;
    }
    
    *humidity = data[0];    
    *temperature = data[2];
    return 0;
}

int detect_edge(int pin, int val, int interval, int timeout)
{
    int counter = 0;
    while (digitalRead(pin) == val && counter < timeout) {
        delayMicroseconds(interval);
        ++counter;
    }
    if (counter > timeout) {
        return -1;
    }
    return counter;
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