#!/usr/bin/node

/**
 * ServIoTicy service object (SO) generator for InTeGreen
 *
 * Copyright 2014 Matthias Dieter Wallnöfer, TIS innovation park,
 *                                           Bolzano/Bozen - Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

"use strict";

var http = require("http");
var readline = require("readline");
var fs = require("fs");

var rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

var integreenHost="ipchannels.integreen-life.bz.it";
var integreenPort="80";
var integreenStationDetails="get-station-details";
var integreenDatatypes="get-data-types";

function generator(integreenFrontend) {
    var http_req = "/"+integreenFrontend+"/rest/"+integreenStationDetails;

    var http_options = {
        host: integreenHost,
        port: integreenPort,
        path: http_req,
        method: "GET"
    };

    http_req = http.request(http_options, function(res) {
        res.setEncoding("utf8");

        res.on("data", function (chunk) {
            var stationDetails;
            try {
                stationDetails = JSON.parse(chunk);
            } catch (e) { console.error(e+"\n"+stationDetails); }

            http_req = "/"+integreenFrontend+"/rest/"+integreenDatatypes;
            http_options.path = http_req;

            http_req = http.request(http_options, function(res) {
                res.setEncoding("utf8");

                res.on("data", function (chunk) {
                    var datatypes;
                    try {
                        datatypes = JSON.parse(chunk);
                    } catch (e) { console.error(e+"\n"+datatypes); }

                    // For debug purposes
                    console.log("Station details");
                    console.log(stationDetails);
                    console.log("Datatypes");
                    console.log(datatypes);

                    var so = {
                        name: integreenFrontend,
                        description: integreenFrontend + " of InTeGreen",
                        streams: {},
                        customFields: {},
                        actions: [],
                        properties: []
                    };

                    stationDetails.forEach(function(station) {
                        so.streams[station.id] = {
                            channels: {},
                            description: station.name,
                            type: "sensor"
                        };
                        datatypes.forEach(function(type) {
                            so.streams[station.id].channels[type[0]] = {
                                // InTeGreen default data type is numeric
                                type:"number",
                                unit:type[1]
                            };
                        });
                        // also define geodata fields if present
                        if (typeof(station.latitude) == "number" &&
                            typeof(station.longitude) == "number") {
                            so.streams[station.id].channels.latitude = {
                                type:"number",
                                "current-value":station.latitude,
                                unit:"degrees"
                            };
                            so.streams[station.id].channels.longitude = {
                                type:"number",
                                "current-value":station.longitude,
                                unit:"degrees"
                            };
                        }
                    });

                    // For debug purposes
                    console.log("Output:");

                    var output = JSON.stringify(so);
                    console.log(output);
                    // write output to file if 4. argument exists
                    if (process.argv.length > 3) {
                        fs.writeFileSync(process.argv[3], output);
                    }

                    rl.close();
                });
            });

            http_req.write("");
            http_req.end();
        });
    });

    http_req.write("");
    http_req.end();
}

// determine frontend as argument (3. one) or get it by console input
if (process.argv.length > 2) {
    generator(process.argv[2]);
} else {
    rl.question("InTeGreen frontend? ", generator);
}
