#!/usr/bin/env python

# Arduino temperature -> CSV
# Copyright 2014 Matthias Dieter Wallnöfer, TIS innovation park,
#                                           Bolzano/Bozen - Italy
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import serial, csv, re, time

SERIAL_PORT="/dev/ttyACM0"
FILE="temp.csv"

ser = serial.Serial(SERIAL_PORT, 9600)
f = open(FILE, 'ab')
csvWriter = csv.writer(f)
try:
    while True:
        buf = ser.readline()

        row = re.search(r'Sensor Value: (\w+), Volts: (\w+\.\w+), degrees C: (\w+\.\w+)', buf)
        if row is not None:
            value = row.group(1)
            voltage = row.group(2)
            celsius = row.group(3)

            timestr = time.strftime("%d.%m.%Y  %H:%M:%S")
            print timestr
            csvWriter.writerow([timestr, value, voltage, celsius])
except KeyboardInterrupt:
    pass
ser.close()
f.close()
