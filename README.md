# ftc_app

***NOTE: Copy of FTC Robot Controller App, forked from ftctechnh/ftc_app***

The goal of this project is to create a standalone app that will replace the USB connected Modern Robotics hardware (USB Motor Controller, Legacy Motor Controller, USB Servo Controller) with a networked connected computer.  This computer will then run a simulation of the controllers and provide a 3d view of a simulated robot.  To accomplish this, the FTDI USB to serial driver library file will be removed and replaced with a stub driver that implements the required calls.  The old USB calls will then be rerouted to the computer using UDP packets.

**Original README:**

FTC Android Studio project to create FTC Robot Controller app.

This is the FTC SDK that can be used to create an FTC Robot Controller app, with custom op modes.
The FTC Robot Controller app is designed to work in conjunction with the FTC Driver Station app.
The FTC Driver Station app is available through Google Play.

To use this SDK, download/clone the entire project to your local computer.
Use Android Studio to open the folder as an "existing Android Studio project".

We are working on providing documentation (both javadoc reference documentation and a PDF user manual)
for this SDK soon.

For technical questions regarding the SDK, please visit the FTC Technology forum:

  http://ftcforum.usfirst.org/forumdisplay.php?156-FTC-Technology
  
T. Eng
May 28, 2015

