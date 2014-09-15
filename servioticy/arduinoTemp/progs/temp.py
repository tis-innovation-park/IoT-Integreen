#!/usr/bin/env python

# Arduino temperature -> ServIoTicy
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

import serial, time, json, re, httplib, urllib

SERIAL_PORT="/dev/ttyACM0"
SERV_API_URL="127.0.0.1:8080"
AUTH_TOKEN="M2JhMmRkMDEtZTAwZi00ODM5LThmYTktOGU4NjNjYmJmMjc5N2UzNzYwNWItNTc2ZS00MGVlLTgyNTMtNTgzMmJhZjA0ZmIy"

def createJsonValue(celsius):
    return json.dumps({ "channels": { "celsius": { "current-value": float(celsius) }}, "lastUpdate": int(time.time()) })

def sendData(jsonValue, soId, streamId, servApiUrl=SERV_API_URL, token=AUTH_TOKEN):
    conn = httplib.HTTPConnection(servApiUrl)
    conn.request("PUT", "/" + soId + "/streams/" + streamId, jsonValue,
                 { "Content-Type": "application/json", "Authorization": token })
    response = conn.getresponse()
    print(response.read(), response.status, response.reason)


f = open("tempServiceObject.id")
try:
    soId = f.read()
    soId = soId.rstrip()
finally:
    f.close()

ser = serial.Serial(SERIAL_PORT, 9600)
try:
    while True:
        buf = ser.readline()
        print(buf)

        celsius = re.search(r'degrees C: (\w+\.\w+)', buf)
        if celsius is not None:
            celsius = celsius.group(1)
            sendData(createJsonValue(celsius), soId, streamId="temp1")
except KeyboardInterrupt:
    pass
ser.close()
