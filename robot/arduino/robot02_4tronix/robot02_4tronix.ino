/*
  Robot 02
  Uses the IR obstacle Sensors to prevent the car hitting obstacles
  Uses PWM to set the speed
 
  This example code is in the public domain.
 */
 
int L1 = 7, L2 = 6, R1 = 4, R2 =5; // Pins connected to Motor driver H-Bridge (L2 and R2 must be PWM)
int lDetect = 11, rDetect = 9;  // Pins for obstacle sensors
int wait = 10;  // ms to wait between moves - set to short time exept for testing
int count = 0;
int flag = 0;

// the setup routine runs once when you press reset:
void setup()
{                
  // initialize the digital pins we will use as an output.
  pinMode(L1, OUTPUT);     
  pinMode(L2, OUTPUT);     
  pinMode(R1, OUTPUT);     
  pinMode(R2, OUTPUT);
  pinMode (lDetect, INPUT);
  pinMode (rDetect, INPUT);
  Serial.begin(115200);
}

void loop()
{
  forward(255);
  if (count>5){ // Trapped in a corner more than 5 times ?
    count=0;
    reverse(255); // Escape from corner
    delay(3000);
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
    reverse(255); // Normal operate
    delay(2000);
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
    reverse(255); // Normal operate
    delay(2000);
    lSpin(200);
    delay(1000);
  }
}

// robMove sets the H-Bridge inputs appropriately for the direction and sets the PWM values
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
  robMove(rate/2, rate);
  delay(wait);
}

void right(int rate)
{
  robMove(rate, rate/2);
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
