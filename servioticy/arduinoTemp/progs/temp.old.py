#!/usr/bin/env python

# Arduino temperature -> ServIoTicy (old version)
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

import sys, os, fcntl, termios, time, json, re, httplib

SERV_API_URL="127.0.0.1:8080"
AUTH_TOKEN="M2JhMmRkMDEtZTAwZi00ODM5LThmYTktOGU4NjNjYmJmMjc5N2UzNzYwNWItNTc2ZS00MGVlLTgyNTMtNTgzMmJhZjA0ZmIy"

##################################################################
# Establish a serial-port connection w. required settings.
##################################################################
def openSerial(portName):
    # The open attempt may fail on account of permissions or on
    # account of somebody's already using the port.
    # Pass such exceptions on to our client.

    # You probably just want to use the builtin open(), here...
    fd = os.open(portName, os.O_RDONLY, 0) # O_RDWR

    # Set up symbolic constants for the list elements returned by
    # tcgetattr.
    [iflag, oflag, cflag, lflag, ispeed, ospeed, cc] = range(7)

    # Set the port baud rate, etc.
    settings = termios.tcgetattr(fd)
    # Set the baud rate.
    settings[ospeed] = termios.B9600 # Output speed
    settings[ispeed] = termios.B0    # Input speed (B0 => match output)
    # Go for 8N1 with hardware handshaking.
    settings[cflag] = (((settings[cflag] & ~termios.CSIZE) |
                        termios.CS8) & ~termios.PARENB)
    # NOTE:  This code relies on an UNDOCUMENTED
    # feature of Solaris 2.4. Answerbook explicitly states
    # that CRTSCTS will not work.  After much searching you
    # will discover that termiox ioctl() calls are to
    # be used for this purpose.  After reviewing Sunsolve
    # databases, you will find that termiox's TCGETX/TCSETX
    # are not implemented.  *snarl*
    #settings[cflag] = settings[cflag] | termios.CRTSCTS
    # Don't echo received chars, or do erase or kill input processing.
    #settings[lflag] = (settings[lflag] &
    #                   ~(termios.ECHO | termios.ICANON))
    # Do NO output processing.
    #settings[oflag] = 0

    # When reading, always return immediately, regardless of
    # how many characters are available.
    #settings[cc][termios.VMIN] = 0
    #settings[cc][termios.VTIME] = 0
   
    # Install the modified line settings.
    termios.tcsetattr(fd, termios.TCSANOW, settings)

    # Set it up for non-blocking I/O.
    #fcntl.fcntl(fd, fcntl.F_SETFL, os.O_NONBLOCK)

    return fd

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

fd = openSerial("/dev/ttyACM0")
try:
    while True:
        ch = '\0'
        buf = ''

        while ch != '\n':
            ch = os.read(fd, 1)
            buf += ch
        print(buf)

        celsius = re.search(r'degrees C: (\w+\.\w+)', buf)
        if celsius is not None:
            celsius = celsius.group(1)
            sendData(createJsonValue(celsius), soId, streamId="temp1")
except KeyboardInterrupt:
    pass
os.close(fd)
