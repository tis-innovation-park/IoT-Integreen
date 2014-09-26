Hackathon Luna
=====

This example shows how to control the RGB LED from Grove, through the Spark Cloud API.


Flash the core with the provided code (remember to include the .h header file) and then set the LED color, like:

```sh
curl https://api.spark.io/v1/devices/53ff7a065075535142471187/setColor -d access_token=SPARK_ACCESS_TOKEN -d "args=100,255,0"
```

* Replace the core id ("53ff...") with yours and put your Spark core access token!
