#include <SoftwareSerial.h>
#include <NewPing.h>
#include <Servo.h>
#include "EEPROM.h"

// robot code with ultrasonic sensor - 18. august

#define D1 2          // direction of motor rotation left
#define M1 3          // PWM right motor
#define D2 4          // direction of motor rotation right
#define M2 5          // PWM left motor
#define HORN 13       // additional channel 1

#define MYSERIAL_TX 6 // software serial TX to bluetooth
#define MYSERIAL_RX 7 // software serial RX from bluetooth

#define TRIGGER_PIN  8 // Ultrasonic trigger pin
#define ECHO_PIN    12 // Ultrasonic echo pin
#define MAX_DISTANCE 200 // Ultrasonic maximum distance [cm]
#define MIN_DISTANCE 5 // Ultrasonic minimum distance [cm]

// pins 9 + 10 are reserved for servo timer - do not use
#define SERVO_PIN 11   // servo motor pin
#define MAX_SERVO 136 // maximum servo motor value [deg] - 136 is the greatest possible
#define MIN_SERVO 12  // minimum servo motor value [deg] - 12 is the least possible

#define cmdL 'L'      // UART-command for left motor
#define cmdR 'R'      // UART-command for right motor
#define cmdH 'H'      // UART-command for additional channel (for example Horn)
#define cmdF 'F'      // UART-command for EEPROM operation
#define cmdr 'r'      // UART-command for EEPROM operation (read)
#define cmdw 'w'      // UART-command for EEPROM operation (write)
#define cmdS 'S'      // UART-command for servo motor
#define cmdE 'e'      // UART-command for command execution
#define cmdT 't'      // UART-command for EEPROM command execution

char incomingByte;    // incoming data

#define ARRAY_SIZE(a) sizeof(a)/sizeof(a[0])

char L_Data[5];       // array data for left motor (4 chars necessary eg. "-255" + 0x00)
byte L_index = 0;     // index of array L
char R_Data[5];       // array data for right motor (4 chars necessary eg. "-255" + 0x00)
byte R_index = 0;     // index of array R
char H_Data[2];       // array data for additional channel (1 char necessary eg. "1" + 0x00)
byte H_index = 0;     // index of array H
char F_Data[8];       // array data for EEPROM (at least 5 chars necessary)
byte F_index = 0;     // index of array F
char S_Data[4];       // array data for servo motor (3 chars necessary eg. "255" + 0x00)
byte S_index = 0;     // index of array S

char command;         // command

unsigned long lastTimeCommand, autoOFF; // [ms]
int oldServoAngle = MIN_SERVO; // old servo angle [deg]

SoftwareSerial mySerial(MYSERIAL_RX, MYSERIAL_TX); // RX, TX for bluetooth
NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // sonar init
Servo servo; // Servo motor object

void setup() {
  mySerial.begin(9600);
  pinMode(HORN, OUTPUT);    // additional channel
  pinMode(D1, OUTPUT);      // output for motor rotation
  pinMode(M1, OUTPUT);
  pinMode(D2, OUTPUT);
  pinMode(M2, OUTPUT);
  /*EEPROM.write(0,255);
   EEPROM.write(1,255);
   EEPROM.write(2,255);
   EEPROM.write(3,255);*/
  Timer_Init();             // initialization software timer
  
  servo.attach(SERVO_PIN); // servo init
  servo.write(MIN_SERVO);
}

void Timer_Init() {
  char sw_autoOFF = EEPROM.read(0);      // read EEPROM "is activated or not stopping the car when losing connection"
  if(sw_autoOFF == '1'){                 // if activated
    char var_Data[] = {
      (char)EEPROM.read(1),
      (char)EEPROM.read(2),
      (char)EEPROM.read(3),
      '\0'
    };
    autoOFF = atoi(var_Data)*100UL;      // variable autoOFF [ms], UL since > 2^15-1 (32767)
  }
  else if(sw_autoOFF == '0'){
    autoOFF = 999999UL;                  // UL since > 2^15-1 (32767)
  }
  else{
    autoOFF = 2500UL;                    // if the EEPROM is blank, default value is 2.5 sec
  }
}

void loop() {
  if (mySerial.available() > 0) {          // if received UART data
    incomingByte = mySerial.read();        // read byte

    if (incomingByte == 'C') {           // BLE AT-09 "Connected\r\n" sequence
      while (mySerial.read() != '\n');
      return;
    }

    if(incomingByte == cmdL) {           // if received data for left motor L
      command = cmdL;                    // current command
      memset(L_Data,0,sizeof(L_Data));   // clear array
      L_index = 0;                       // resetting array index
    }
    else if(incomingByte == cmdR) {      // if received data for left motor R
      command = cmdR;
      memset(R_Data,0,sizeof(R_Data));
      R_index = 0;
    }
    else if(incomingByte == cmdH) {      // if received data for additional channel
      command = cmdH;
      memset(H_Data,0,sizeof(H_Data));
      H_index = 0;
    }   
    else if(incomingByte == cmdF) {      // if received data for EEPROM op
      command = cmdF;
      memset(F_Data,0,sizeof(F_Data));
      F_index = 0;
    }
    else if (incomingByte == cmdS) {     // if received data for servo motor
      command = cmdS;
      memset(S_Data,0,sizeof(S_Data));
      S_index = 0;
    }
    else if(incomingByte == '\r') command = cmdE;   // end of line
    else if(incomingByte == '\t') command = cmdT;   // end of line for EEPROM op

    if(command == cmdL && (incomingByte >= '0' && incomingByte <= '9' || incomingByte == '-')){
      L_Data[L_index] = incomingByte;              // values [-][0..9] => [-][0..255]
      if (L_index < ARRAY_SIZE(L_Data)-2) L_index++; // increment array index but the last char needs to remain 0x00
    }
    else if(command == cmdR && (incomingByte >= '0' && incomingByte <= '9' || incomingByte == '-')){
      R_Data[R_index] = incomingByte;              // values [-][0..9] => [-][0..255]
      if (R_index < ARRAY_SIZE(R_Data)-2) R_index++;
    }
    else if(command == cmdH && incomingByte >= '0' && incomingByte <= '1'){
      H_Data[H_index] = incomingByte;              // values [0,1]
      if (H_index < ARRAY_SIZE(H_Data)-2) H_index++;
    }   
    else if(command == cmdF && (incomingByte >= '0'
         && incomingByte <= '9' || incomingByte == 0xFF || incomingByte == cmdr || incomingByte == cmdw)){
      F_Data[F_index] = incomingByte;              // values [0..9]|0xFF|r|w => r|(w(0|1)[0..9][0..9][0..9])|0xFF)
      if (F_index < ARRAY_SIZE(F_Data)-1) F_index++; // this is not null-terminated!
    }
    else if (command == cmdS && incomingByte >= '0' && incomingByte <= '9') {
      S_Data[S_index] = incomingByte;              // values [0..9] => [0..255]
      if (S_index < ARRAY_SIZE(S_Data)-2) S_index++;
    }
    else if(command == cmdE){                       // if we take the line end execute and reset
      Control4WD(atoi(L_Data),atoi(R_Data),atoi(H_Data),atoi(S_Data));
      command = '\0';
    }
    else if(command == cmdT){                       // if we take the EEPROM line end execute and reset
      Flash_Op(F_Data[0],F_Data[1],F_Data[2],F_Data[3],F_Data[4]);
      command = '\0';
    }
    lastTimeCommand = millis();                    // read the time elapsed since application start
  }
  if(millis() >= (lastTimeCommand + autoOFF)){     // if current timer >= lastTimeCommand + autoOFF
    Control4WD(0,0,0,-1);                          // stop the car for security reasons
  }
  
  // Check ultrasonic minimum distance
  int dist = sonar.ping() / US_ROUNDTRIP_CM; // Sonar distance [cm]
  if (dist <= MIN_DISTANCE && (atoi(L_Data) > 0 || atoi(R_Data) > 0)) { // if it drives forward
    Control4WD(0,0,0,-1);                         // stop the car for security reasons
  }
}

void Control4WD(int mLeft, int mRight, byte Horn, int mServo){
  byte directionL = 0, directionR = 0;  // direction of motor rotation L298N
  byte valueL = 0, valueR = 0;          // PWM M1, M2 (0-255)
  
  if(mLeft > 0 && mLeft <= 255){
    valueL = mLeft;
    directionL = 0;
  }
  else if(mLeft < 0 && mLeft >= -255){
    valueL = 255 - abs(mLeft);
    directionL = 1;
  }
 
  if(mRight > 0 && mRight <= 255){
    valueR = mRight;
    directionR = 0;
  }
  else if(mRight < 0 && mRight >= -255){
    valueR = 255 - abs(mRight);
    directionR = 1;
  }

  analogWrite(M1, valueL);            // set speed for left motor
  analogWrite(M2, valueR);            // set speed for right motor
  digitalWrite(D1, directionL);       // set direction of left motor rotation
  digitalWrite(D2, directionR);       // set direction of right motor rotation
  digitalWrite(HORN, Horn);           // additional channel
  
  if (mServo >= 0 && mServo <= 255) { // servo handling, change state only if necessary
    int servoAngle = MIN_SERVO + mServo * (MAX_SERVO - MIN_SERVO) / 255;
    if (oldServoAngle != servoAngle) {
      servo.write(servoAngle);
      oldServoAngle = servoAngle;
    }
  }
}

void Flash_Op(char FCMD, char z1, char z2, char z3, char z4){
  if(FCMD == cmdr){                 // if EEPROM data read command
    mySerial.print("FData:");       // send EEPROM data
    mySerial.write(EEPROM.read(0)); // read value from the memory with 0 address and print it to UART
    mySerial.write(EEPROM.read(1));
    mySerial.write(EEPROM.read(2));
    mySerial.write(EEPROM.read(3));
    mySerial.print("\r\n");         // mark the end of the transmission of data EEPROM
  }
  else if(FCMD == cmdw){            // if EEPROM data write command
    EEPROM.write(0,z1);             // z1 record to a memory with 0 address
    EEPROM.write(1,z2);
    EEPROM.write(2,z3);
    EEPROM.write(3,z4);
    Timer_Init();                   // reinitialize the timer
    mySerial.print("FWOK\r\n");     // send a message that the data is successfully written to EEPROM
  }
}
