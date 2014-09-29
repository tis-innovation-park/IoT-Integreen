/*
  Robot 03
  Uses the IR Sensors to prevent the car hitting obstacles.
  Uses the Pan/Tilt with light sensors to move towards the lightest point
  Uses PWM to set the speed
 
  This example code is in the public domain.
 */

// Motor Drive Definitions
int L1 = 7, L2 = 6, R1 = 4, R2 =5; // Pins connected to Motor driver H-Bridge (L2 and R2 must be PWM)

// IR Sensor Definitions
int lDetect = 11, rDetect = 9;  // Pins for obstacle IR sensors. Left sensor to Digital pin 11. Right sensor to Digital pin 9
int wait = 1;  // ms to wait between moves - set to short time except for testing
int count = 0;
int flag = 0;

// Light Follower Definitions
#include <Servo.h> 
 
Servo panServo, tiltServo;  // create servo object to control a servo 
int panPin = 8;     // Pan servo is on Digital Pin 8
int tiltPin = 10;    // Tilt servo is on Digital Pin 10

int leftLDR = A0;       // Analog input pin for left LDR (pullup with 10K resistor to +5V)
int rightLDR = A1;      // Analog input pin for right LDR (pullup with 10K resistor to +5V)
int upLDR = A2;         // Analog input pin for up LDR (pullup with 10K resistor to +5V)
int downLDR = A3;       // Analog input pin for down LDR (pullup with 10K resistor to +5V)
int lData, rData, uData, dData;     // Data values for LDRs
int deadband = 1;    // Creates a dead-band, adjust appropriately. Prevents the servos thrashing when point straight at light source

#define maxServo 130
#define minServo 10
#define midServo 90

int panPosition = midServo;   // Set Pan servo to middle of range
int tiltPosition = midServo;  // Set Tilt servo to middle of range


// the setup routine runs once when you press reset:
void setup()
{                
  // Motor Initialisation
  pinMode(L1, OUTPUT);     
  pinMode(L2, OUTPUT);     
  pinMode(R1, OUTPUT);     
  pinMode(R2, OUTPUT);
  
  // IR Sensor Initialisation
  pinMode (lDetect, INPUT);
  pinMode (rDetect, INPUT);
  
  // Light Follower Initialisation
  panServo.attach(panPin);      // attaches the pan servo to the servo object 
  tiltServo.attach(tiltPin);    // attaches the tilt servo to the servo object
  panServo.write(panPosition);    // Initialise Pan servo
  tiltServo.write(tiltPosition);  // initialise Tilt servo
  pinMode (leftLDR, INPUT);
  pinMode (rightLDR, INPUT);
  pinMode (upLDR, INPUT);
  pinMode (downLDR, INPUT);

  Serial.begin(115200);
}

void loop()
{
  forward(255);
  for (int j = 0; j < 5; j++)  // spin car to point at light source
  {
    for (int i = 0; i < 10; i++)
      followLight();
    if (panPosition > (midServo + 10))
    {
      lSpin(255);
      delay(100);
    }
    else if (panPosition < (midServo - 10))
    {
      rSpin(255);
      delay(100);
    }
    else
      break;  // we're close to ideal position so stop checking
  }
  if (count>5) // Trapped in a corner more than 5 times ?
  {
    count=0;
    reverse(255); // Escape from corner
    delay(1000);
    rSpin(255);
    delay(2000);
  }
  if (digitalRead(lDetect)==0) // Test left sensor
  {
    Serial.println(String("lDetect") + ":" + String(count));
    if(flag == 1)                  // Check previous state
      count++;
    else
      count=0;
    flag = 0;
    reverse(255);
    delay(1000);
    rSpin(255);
    delay(1000);
  }
  if (digitalRead(rDetect)==0)         // Test right switch
  {
    Serial.println(String("rDetect") + ":" + String(count));
    if(flag == 0)                  // Check previous state
      count++;
    else
      count=0;
    flag = 1;
    reverse(255);
    delay(1000);
    lSpin(200);
    delay(1000);
  }
}

// robMove routine sets the PWM and Direction for each side of the robot
void robMove(int lSpeed, int rSpeed)
{
  String s2 = String(lSpeed) + ":" + String(rSpeed);
  Serial.println(s2);
  if(lSpeed == 0 && rSpeed == 0)
  {
    digitalWrite(L1, LOW);
    digitalWrite(L2, LOW);
    digitalWrite(R1, LOW);
    digitalWrite(R2, LOW);  
  }
  else
  {
    if(lSpeed < 0)
    {
      digitalWrite(L1, LOW);
      analogWrite(L2, abs(lSpeed));
    }
    else
    {
      digitalWrite(L1, HIGH);
      analogWrite(L2, 255 - lSpeed);
    }
    if(rSpeed < 0)
    {
      digitalWrite(R1, LOW);
      analogWrite(R2, abs(rSpeed));
    }
    else
    {
      digitalWrite(R1, HIGH);
      analogWrite(R2, 255 - rSpeed);
    }
  }
}

void forward(int rate)
{
  robMove(rate, rate);
  delay(wait);
}

void reverse(int rate)
{
  robMove(-rate, -rate);
  delay(wait);
}

void left(int rate)
{
  robMove(rate/2, rate);  // set the left motor(s) slower so the car drives left
  delay(wait);
}

void right(int rate)
{
  robMove(rate, rate/2);  // set the right motor(s) slower so the car drives right
  delay(wait);
}

void halt()
{
  robMove(0, 0);
  delay(wait);
}

void lSpin(int rate)
{
  robMove(-rate, rate);
  delay(wait);
}

void rSpin(int rate)
{
  robMove(rate, -rate);
  delay(wait);
}

// followLight() routine moves the PanTilt towards the brightest light source
void followLight() 
{ 
  lData = analogRead(leftLDR);
  rData = analogRead(rightLDR);
  uData = analogRead(upLDR);
  dData = analogRead(downLDR);
  
  if (lData > (rData + deadband) || rData > (lData + deadband))  // check if there is enough difference in readings to adjust servos
  {
    if(lData < rData)
      panPosition--;  // rotate left
    else
      panPosition++;  // rotate right
    if(panPosition > maxServo)
      panPosition = maxServo;      // ensure we don't go past end stops these numbers will change depending on your servos and construction
    else if (panPosition < minServo)
      panPosition = minServo;
    panServo.write(panPosition);  // output new value to servo
  }

  if (uData > (dData + deadband) || dData > (uData + deadband))  // check if there is enough difference in readings to adjust servos
  {
    if(dData < uData)
      tiltPosition--;  // move down
    else
      tiltPosition++;  // move up
    if(tiltPosition > maxServo)
      tiltPosition = maxServo;      // ensure we don't go past end stops these numbers will change depending on your servos and construction
    else if (tiltPosition < minServo)
      tiltPosition = minServo;
    tiltServo.write(tiltPosition);   // output new value to servo
  }
  delay(15);                           // waits for the servo to get there. Adjust to suit speed of servos
} 
