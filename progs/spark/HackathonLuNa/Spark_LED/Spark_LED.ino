#include "PubSubClient.h"

#define auth_token  "XXXXYYYYY"
#define SO_ID "XXXX"

//byte broker_ip[] = {  147, 83, 30, 150 };
byte broker_ip[] = {  83, 212, 96, 61 };

int led  = D5;
int led2 = D7;

void callback(char* topic, byte* payload, unsigned int length) {
    
    digitalWrite(led, HIGH);   // Turn ON the LED
    delay(1000);               // Wait for 1000mS = 1 second
    digitalWrite(led, LOW);    // Turn OFF the LED
    delay(1000); 
}

TCPClient tcpClient;
PubSubClient client(broker_ip, 1883, callback, tcpClient);

void setup() {
    
    Serial.begin(9600);
    
    
    delay(2000);
    
    pinMode(led, OUTPUT);
    pinMode(led2, OUTPUT);
    
    
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