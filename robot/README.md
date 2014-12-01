## Equipment

First thing you should do is to buy this equipment:

* [Ultimate Starter Kit: Pirate – 4WD Mobile Platform](http://www.google.com/url?q=http%3A%2F%2Fwww.ebay.it%2Fitm%2FUltimate-Starter-Kit-4WD-Robot-Car-DFRobot-Robotics-Platform-without-Arduino-%2F161177884052%3Fpt%3DUK_Computing_Other_Computing_Networking%26hash%3Ditem2586f35594&sa=D&sntz=1&usg=AFQjCNFsclWrIZmguzXOZ2l50J0sR6UQaw)
* [Arduino UNO](http://www.google.com/url?q=http%3A%2F%2Fwww.exp-tech.de%2FMainboards%2FArduino-Uno-R3.html&sa=D&sntz=1&usg=AFQjCNHGArFMFaQnLCd8ewy8VN35o50w6A). (But if you want you can choose any Arduino you like.)
* You need 5 AA rechargeable Batteries with 2400mA/h
* [This is the power supply we used](http://www.google.com/url?q=http%3A%2F%2Fwww.hobbydirekt.de%2FULTRAMAT-10-Graupner-6410%3A%3A31105.html&sa=D&sntz=1&usg=AFQjCNEGVlLp_pUX0QSUZEL4diPseNTHjg)
* [Connection wire for the Arduino - USB printer cable](http://www.google.com/url?q=http%3A%2F%2Fwww.exp-tech.de%2FZubehoer%2FKabel%2FUSB-Kabel-A-B-180cm.html&sa=D&sntz=1&usg=AFQjCNGyCFDfp5prfzifM4VIoPf9ZOmrWg). (The best would be, if you are a beginner, to buy the Arduino Starter Kit)
*  [Stackable Bluetooth shield](http://www.google.com/url?q=http%3A%2F%2Fwww.exp-tech.de%2FShields%2FStackable-Bluetooth-Shield-v2-2-Master-Slave.html&sa=D&sntz=1&usg=AFQjCNH2JAunmOozX3M2P8sK0N3S5HT0Xg)
* You need for the Power supply of the Batteries a [power jack connector](http://www.google.it/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&docid=ANLm9SJzSQyMWM&tbnid=NVHLy_ufW8dfwM:&ved=0CAUQjRw&url=http://www.aliexpress.com/item/CCTV-5-5mm-x-2-1mm-Female-Male-DC-Power-Jack-Connector-free-shipping/520393587.html&ei=-zH8U_iRDq3G7AaltoCgAg&bvm=bv.73612305,d.bGE&psig=AFQjCNEEyAgf-KXzFsm741qSrjV5iHZWBA&ust=1409123087640777)
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
---------------------------------------------------
| 


This is the code for TIS' car robot project based on the Pirate - 4WD Mobile
Platform. It is a fork of http://www.instructables.com/id/Simple-RC-car-for-beginners-Android-control-over-/?ALLSTEPS.

Our documentation: https://docs.google.com/document/d/1IL_beqkgcuiGGH9j_lA7q79ZszZSrrkFruT2HyHj-Vg/edit?usp=sharing

Feel free to use and let us know any comments on the GitHub issue tracker or
contact us on https://github.com/tis-innovation-park.

TIS innovation park - Free Software & Open Technologies - Bolzano/Bozen, Italy
