#include "LED_Bar.h"
#include "PubSubClient.h"

#define auth_token  "XXXXYYY"
#define SO_ID "XXXXX"

int led2 = D7;

//byte broker_ip[] = {  147, 83, 30, 150 };
byte broker_ip[] = {  83, 212, 96, 61 };

LED_Bar bar(6, 7);  

void callback(char* topic, byte* payload, unsigned int length) {
    
    int i=0;
    char buff[length+1];
    
    for(i=0; i<length; i++) {
        buff[i] = payload[i];
    }
    buff[i] = '\0';
    
    bar.setLevel(atoi(buff));
}

TCPClient tcpClient;
PubSubClient client(broker_ip, 1883, callback, tcpClient);

void setup() {

    pinMode(led2, OUTPUT);
    
    Serial.begin(9600);
    
    bar.setLevel(1);
    
    delay(2000);
    
    
    Serial.println(F("connecting to broker and subscribing..."));
    if (client.connect("spark", "compose", "shines")) { // uid:pwd based authentication
        //bool stat = client.subscribe("/1398680352030c33b8197ce354479adf39a3931a19a7f/actions");
        bool stat = client.subscribe("compose_in");
        
        Serial.print(F("Connected and subscribed: "));
        Serial.println(stat);
        digitalWrite(led2, HIGH);
    }
    
    else {
        Serial.println("not connected..");
    }

    
 

}

void loop() {
    
    
    client.loop();

}