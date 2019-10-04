## Equipment

First thing you should do is to buy this equipment:

* [Ultimate Starter Kit: Pirate – 4WD Mobile Platform](https://www.dfrobot.com/product-97.html)
* [Arduino UNO](https://www.exp-tech.de/plattformen/arduino/4380/arduino-uno-r3). (But if you want you can choose any Arduino you like.)
* You need 5 AA rechargeable Batteries with 2400mA/h
* [This is the power supply we used](https://www.hobbydirekt.de/sonstiges/sonstiges/ULTRAMAT-10-Graupner-6410::31105.html?MODsid=6c7079831968f22091511ccb7dd79fd9)
* [Connection wire for the Arduino - USB printer cable](https://www.exp-tech.de/zubehoer/kabel/usb/5285/usb-kabel-2.0-a-stecker-auf-b-stecker-2m).
*  [Stackable Bluetooth shield](https://www.exp-tech.de/module/wireless/bluetooth/8738/seeed-studio-ble-shield)
* You need for the Power supply of the Batteries a [power jack connector](https://www.aliexpress.com/item/520393587.html)
* A 125 x 95 [mm] piece of wood with a min thickness of 4mm.
* Min. 3, 20mm flat head screws

If you have all this stuff you will have to compare everything together and if you want, you can add some sensors form the 4WDStarter Kit. (ATTENTION! Our scripts are only for the ultrasonic sensor and the servo motor to flip the robot)
The original Arduino script and the Android phone app had plenty of bugs so we had to make big changes. [Here](http://www.google.com/url?q=http%3A%2F%2Fwww.instructables.com%2Fid%2FSimple-RC-car-for-beginners-Android-control-over-%2F&sa=D&sntz=1&usg=AFQjCNG8QoCKxEOKsWGcAQMe-jnU9oSa_g) you may download the original code (But I can assure you it will not work).

## Software modifications

### Arduino script

The biggest problem here was the lack of correct C-like buffer handling. Buffers with missing 0-termination considered as strings or missing index out-of-bounds controls were common. Further we had to adapt the script for the ultrasonic sensor and the servo motor. Also the pin assignment of I/O has to be done here: which pin you use is your decision, but there are tight restrictions (motors need to be connected to PWM-capable ones, servo motors need Arduino's timer pins to be left free ecc.). Of course we had to solve some little problems here and there, in the delay, pin selection and so on, but we will show you also the assembled hardware to help you to understand and to assembly everything right.

### Modifications on the phone app

First of all we had to change the bluetooth MAC address because the original developer used another module. The default UI language had been Russian which we removed in favor of English. Then we introduced big improvements on both UI and technical side (general code refactoring, new servo control, rewritten touchscreen motor control, improved bluetooth connection stability). This all had been a lot of work, in particular we updated also to a newer Android version, but finally we got this for you.

### Protocol

| Purpose | Syntax [EBNF] | Description | Example |
|---------|---------------|-------------|---------|
| Read EEPROM settings | ```Fr``` | Read the watchdog settings from Arduino EEPROM | ```Fr``` |
| Response of read EEPROM settings (from Robot) | ```FData:(0\|1\|char #FF)(0..9)(0..9)(0..9)\r\n``` | ```0``` means watchdog disabled - 999999ms, ```1``` means watchdog enabled with the specified time ```(0..9)(0..9)(0..9)``` in the xxx00ms format (1/10 of seconds), char #FF means factory default watchdog 2500ms  | ```FData:0000\r\n```|
| Write EEPROM settings | ```Fw(0\|1(0..9)(0..9)(0..9)\|char #FF)``` | Write the watchdog settings to Arduino EEPROM ```0``` disable watchdog (set to 999999ms), ```1``` enable watchdog with the specified time ```(0..9)(0..9)(0..9)```..in the xxx00ms format (1/10 of seconds), char #FF means factory default watchdog 2500ms   | ```Fw1243``` |
| Response of write EEPROM response (from Robot) | ```FWOK\r\n``` | Successful update | ```FWOK\r\n```|
| EEPROM cmd execution | ```\t``` | Applies transmitted EEPROM command | ```\ţ``` |
| Motor left | ```L<[-](0..2)(0..9)(0..9)>``` | Controls the 2 left motors [```-```]..inverted direction if present ```(0..2)(0..9)(0..9)```..speed from 0..255 | ```L-255``` |
| Motor right | ```R<[-](0..2)(0..9)(0..9)>``` | Controls the 2 right motors [```-```]..inverted direction if present ```(0..2)(0..9)(0..9)```..speed from 0..255 | ```R120``` |
| Horn | ```H(1\|0)``` | Controls the horn signal (pin 13) (```1\|0```)..pin 13 on/off, connected to a LED on the Arduino board | ```H1``` |
| Servo | ```S(0..2)(0..9)(0..9)``` | Controls the servo flipper motor ```(0..2)(0..9)(0..9)```..angle from 0..255 (0..on the back, 255..on the front) | ```S0```|
| Motor/horn/servo cmd execution | ```\r``` | Applies transmitted motor/horn/servo commands | ```\r``` |


### Composition of the hardware

1. Composition of the Arduino (*Bluetooth shield already mounted) and the motor driver:
     * You will have a black, white, gray, violet, bus-wire which is for the connection between the Arduino and the motor driver. The pin of the black wire goes into the ENA slot and the rest in the order of the colors next to each other in the 5V, 5V, and the ENB slot. On the Arduino or respectively the Bluetooth shield, the violet wire of the bus goes into the slot 2, the gray into the slot 3, the black one into the slot 4 and the white one into the slot 5.
2. Composition of the ultrasonic sensor with the bluetooth shield:
     * Trigger Pin 8
     * Echo Pin 12
     * and naturally 5V and GND
3. Composition of the servo motor with the bluetooth shield:
     * You got another bus-wire for the servo motor brown, red, yellow. You have to connect the brown to the GND, the red one to the 5V slot and the yellow to the slot nr. 11. The bus is already connected to the servo.

(Or take a look at the .png file in the documentation directory)
The drawing of the extension of the flip arm(which you have to use if you want your car to flip by it self), the scripts and some pictures are added in the directory.

### Special features
This RC car is equipped with an servo motor which, combined with the flip arm extension and fixed on the top of the car, is a useful tool which can flip up the car if it felt upside down.
Also you could add some very cool sensors like infrared sensor, sound sensor, temperature sensor, light sensor,...

### Licensing

The ownership of the original software belongs to tolik777 [http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/](http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/). As far as it is stated, the author has released instructions and code under Creative Commons Attribution-NonCommercial-ShareAlike 2.5 Generic License, so we need to keep it the same.

### Future plans

In the future the RC car could become a sensor car. With many different sensors it would be able to reach places where humans can’t go and take sensor values from the air, the temperature humidity and so on. A first step to the future could be the addition of an autonomous camera on the top of the car.
The second step would be the substitution of the bluetooth shield with a Wi-Fi shield to increase the connection range and make the car accessible from more than one client. Also the type of clients would increase, since bluetooth is limited to smartphones and tablets.
Also it could be thought as an autonomous car in the basement of vine production farms, to take over the values of CO2 in there. The car would send a signal to a LED if the values are too high for a human to breath. So the farmer sees that he should not go into the basement without turning on the ventilation.

This is the code for TIS' car robot project based on the Pirate - 4WD Mobile
Platform. It is a fork of http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/?ALLSTEPS.

Feel free to use and let us know any comments on the GitHub issue tracker or
contact us on https://github.com/tis-innovation-park.

TIS innovation park - Free Software & Open Technologies - Bolzano/Bozen, Italy
