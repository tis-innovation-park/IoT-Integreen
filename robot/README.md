## Equipment

First thing to do is to order the right equipment:

* [Ultimate Starter Kit: Pirate – 4WD Mobile Platform](https://www.dfrobot.com/product-97.html)
* [Arduino UNO](https://www.exp-tech.de/plattformen/arduino/4380/arduino-uno-r3). (But if you want you can choose any Arduino you like.)
* You need 5 AA rechargeable Batteries with 2400mA/h
* [This is the power supply we used](https://www.hobbydirekt.de/sonstiges/sonstiges/ULTRAMAT-10-Graupner-6410::31105.html?MODsid=6c7079831968f22091511ccb7dd79fd9)
* [Connection wire for the Arduino - USB printer cable](https://www.exp-tech.de/zubehoer/kabel/usb/5285/usb-kabel-2.0-a-stecker-auf-b-stecker-2m).
*  [Stackable Bluetooth shield](https://www.exp-tech.de/module/wireless/bluetooth/8738/seeed-studio-ble-shield)
* You need for the Power supply of the Batteries a [power jack connector](https://www.aliexpress.com/item/520393587.html)
* A 125 x 95 [mm] piece of wood with a min thickness of 4mm.
* Min. 3, 20mm flat head screws

Once you have organised all this stuff you will have to assemble it using the instructions of tolik777 on Instructables [http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/](http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/). 
At your discretion you may connect additional sensors from the Pirate – 4WD Starter Kit (we have tested the ultrasonic sensor and the servo motor to flip the robot).

**Software**: When you are ready for the software deployment **please use our** Arduino sketches and Android phone app (_everything available here in this repo_) since the original releases offered on Instructables had plenty of serious bugs. 

## Software Modifications

### Arduino Sketch

The biggest problem here was the lack of correct C-like buffer handling. Buffers with missing 0-termination considered as strings or missing index out-of-bounds controls were common. Further we had to adapt the script for the ultrasonic sensor and the servo motor. Also the pin assignment of I/O has to be done here: which pin you use is your decision, but there are tight restrictions (motors need to be connected to PWM-capable ones, servo motors need Arduino's timer pins to be left free ecc.). Of course we had to solve some little problems here and there, in the delay, pin selection and so on, but in the end we will show you also the assembled hardware.

### Phone App

First of all we had to change the bluetooth MAC address because the original developer used another module. The default UI language had been Russian which we removed in favour of English. Then we introduced big improvements on both the UI and the technical side (general code refactoring, new servo control, rewritten touchscreen motor control, improved bluetooth connection stability). This all had been a lot of work (including the adaptions needed for a more recent Android version) but finally it was time spent well.

## Protocol

| Purpose | Syntax [EBNF] | Description | Example |
|---------|---------------|-------------|---------|
| Read EEPROM settings | ```Fr``` | Read the watchdog settings from Arduino EEPROM | ```Fr``` |
| Response of read EEPROM settings (from Robot) | ```FData:(0\|1\|char #FF)(0..9)(0..9)(0..9)\r\n``` | ```0``` means watchdog disabled - 999999ms, ```1``` means watchdog enabled with the specified time ```(0..9)(0..9)(0..9)``` in the xxx00ms format (1/10 of seconds), char #FF means factory default watchdog 2500ms  | ```FData:0000\r\n```|
| Write EEPROM settings | ```Fw(0\|1(0..9)(0..9)(0..9)\|char #FF)``` | Write the watchdog settings to Arduino EEPROM ```0``` disable watchdog (set to 999999ms), ```1``` enable watchdog with the specified time ```(0..9)(0..9)(0..9)```..in the xxx00ms format (1/10 of seconds), char #FF means factory default watchdog 2500ms   | ```Fw1243``` |
| Response of write EEPROM response (from Robot) | ```FWOK\r\n``` | Successful update | ```FWOK\r\n```|
| EEPROM cmd execution | ```\t``` | Applies transmitted EEPROM command | ```\t``` |
| Motor left | ```L<[-](0..2)(0..9)(0..9)>``` | Controls the 2 left motors [```-```]..inverted direction if present ```(0..2)(0..9)(0..9)```..speed from 0..255 | ```L-255``` |
| Motor right | ```R<[-](0..2)(0..9)(0..9)>``` | Controls the 2 right motors [```-```]..inverted direction if present ```(0..2)(0..9)(0..9)```..speed from 0..255 | ```R120``` |
| Horn | ```H(1\|0)``` | Controls the horn signal (pin 13) (```1\|0```)..pin 13 on/off, connected to a LED on the Arduino board | ```H1``` |
| Servo | ```S(0..2)(0..9)(0..9)``` | Controls the servo flipper motor ```(0..2)(0..9)(0..9)```..angle from 0..255 (0..on the back, 255..on the front) | ```S0```|
| Motor/horn/servo cmd execution | ```\r``` | Applies transmitted motor/horn/servo commands | ```\r``` |

## Hardware Composition

1. Composition of the Arduino (*Bluetooth shield already mounted) and the motor driver:
     * You will have a black, white, gray, violet, bus-wire which is for the connection between the Arduino and the motor driver. The pin of the black wire goes into the ENA slot and the rest in the order of the colors next to each other in the 5V, 5V, and the ENB slot. On the Arduino or respectively the Bluetooth shield, the violet wire of the bus goes into the slot 2, the gray into the slot 3, the black one into the slot 4 and the white one into the slot 5.
2. Composition of the ultrasonic sensor with the bluetooth shield:
     * Trigger Pin 8
     * Echo Pin 12
     * and naturally 5V and GND
3. Composition of the servo motor with the bluetooth shield:
     * You got another bus-wire for the servo motor brown, red, yellow. You have to connect the brown to the GND, the red one to the 5V slot and the yellow to the slot nr. 11. The bus is already connected to the servo.

If this is to confusing just have a look at the file [Circuit.svg](drawings/Circuit.svg) in the _drawings_ directory. The flip arm (which you can use if you want your car to flip over itself) is shown in [Fliparmextension.svg](drawings/Fliparmextension.svg).

## Special Features

This RC car is equipped with an servo motor which, combined with the flip arm extension and fixed on the top of the car, is a useful tool which can flip up the car if it felt upside down.
Also you could add some very cool sensors like infrared sensor, sound sensor, temperature sensor, light sensor,...

## Licensing

The ownership of the original software belongs to tolik777 [http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/](http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/). As far as it is stated, the author released instructions and code under _Creative Commons Attribution-NonCommercial-ShareAlike 2.5 Generic License_, so we had to keep it the same.

## Future Plans

In the future the RC car could become a sensor car. With many different sensors it would be able to reach places where humans can’t go and take sensor values from the air, the temperature humidity and so on. A first step to the future could be the addition of an autonomous camera on the top of the car.
The second step would be the substitution of the bluetooth shield with a Wi-Fi shield to increase the connection range and make the car accessible from more than one client. Also the favours of clients would increase, since bluetooth is limited to smartphones and tablets.
Also it could be adopted as an autonomous car in the basement of vine production farms, to take over the values of CO2 in there. The car would send a signal to a LED if the values are too high for a human to breath. So the farmer would understand that he should not go into the basement without turning on the ventilation.

Feel free to use and let us know any comments on the GitHub issue tracker or
contact us on https://github.com/tis-innovation-park.

TIS innovation park - Free Software & Open Technologies - Bolzano/Bozen, Italy
