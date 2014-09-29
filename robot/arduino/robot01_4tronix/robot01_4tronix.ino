/*
  Robot 01
  Moves the car forwards, backwards, right rotate, left rotate, halt.
  No speed control is used
  Basic test to ensure correct connections
 
  This example code is in the public domain.
 */
 
int L1 = 7, L2 = 6, R1 = 4, R2 =5; // Pins connected to Motor driver H-Bridge

// the setup routine runs once when you press reset:
void setup()
{                
  // initialize the digital pins we will use as an output.
  pinMode(L1, OUTPUT);     
  pinMode(L2, OUTPUT);     
  pinMode(R1, OUTPUT);     
  pinMode(R2, OUTPUT);
  //Serial.begin(115200);
}

// the loop routine runs over and over again forever:
void loop()
{
  forward(2000);
  halt(500);
  reverse(2000);
  halt(500);
  left(2000);
  halt(500);
  right(2000);
  halt(1000);
}

// robMove routine switches teh correct inputs to the L298N for teh direction selected.
void robMove(int l1, int l2, int r1, int r2)
{
  digitalWrite(L1, l1);
  digitalWrite(L2, l2);
  digitalWrite(R1, r1);
  digitalWrite(R2, r2);  
}

void forward(int wait)
{
  robMove(LOW, HIGH, LOW, HIGH);
  delay(wait);
}

void reverse(int wait)
{
  robMove(HIGH, LOW, HIGH, LOW);
  delay(wait);
}

void left(int wait)
{
  robMove(HIGH, LOW, LOW, HIGH);
  delay(wait);
}

void right(int wait)
{
  robMove(LOW, HIGH, HIGH, LOW);
  delay(wait);
}

void halt(int wait)
{
  robMove(LOW, LOW, LOW, LOW);
  delay(wait);
}


