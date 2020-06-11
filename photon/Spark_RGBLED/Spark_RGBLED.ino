// This #include statement was automatically added by the Spark IDE.
#include "ChainableLED.h"

ChainableLED leds(1, 2, 1);
float hue = 0.0;
boolean up = true;

void setup() {
    
    Spark.function("setColor", setColor);
    
    leds.setColorRGB(0, 203, 131, 36);

}

void loop() {
    

}

int setColor(String command)
{
  int Red, Green, Blue;
    
  int loc1 = 0;
  int loc2 = 0;
  int loc3 = 0;

  loc1 = command.indexOf(",");
  Red = command.substring(0,loc1).toInt();

  loc2 = command.indexOf(",",loc1+1);
  Green = command.substring(loc1+1,loc2).toInt();

  loc3 = command.indexOf(",",loc2+1);
  Blue = command.substring(loc2+1,loc3).toInt();
    
  leds.setColorRGB(0, Red, Green, Blue);

  // int status_code = ...
  return 1;
}