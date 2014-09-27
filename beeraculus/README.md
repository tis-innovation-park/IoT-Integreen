Beeraculus
=============

Internet of Things demonstration - never stay out of beer again!

We need to now understand how to wire together a system that can be reusable with most beer crates, something universal like IKEA.

The repo contains all source code for the Spark core, the NodeRED flow that check the light sensor threshold and sends a SMS through Twilio and a sample Glassware app for sending noticiations on google glass.

Try the glassware app at http://mobiledemo.compose-project.eu/mirror/beeraculus.php on Google glass
to send a message to the Glass it should work by making a GET request like:
http://mobiledemo.compose-project.eu/mirror/beeraculus.php?message='this is a message from Beeraculus!'
