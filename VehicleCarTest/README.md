HC-05
=====

This app will easily setup a connection for you with the HC-05 bluetooth module, which is used for example on the Arduino. Once you are connected it will open a socket, so you can pass data to the device.

All logic is handled in the activity itself. There is also a writer thread which will keep running to send data to the bluetooth device once it's connected. Current parameters are: "up", "down", "left", "right" and a sensitivity slider.

This code is not tested on the HC-06 module, so please report if it's working.