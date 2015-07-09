# FTC App Networked Simulator

> The Android App is adapted from the [FTC SDK](https://github.com/ftctechnh/ftc_app) with small changes to the FTDI USB serial driver.

The goal of this project is to create a standalone app that will replace the USB connected Modern Robotics hardware (USB Motor Controller, Legacy Motor Controller, USB Servo Controller) with a networked connected computer. This computer will then run a simulation of the controllers and provide a 3d view of a simulated robot. To accomplish this, the FTDI USB to serial driver library file will be removed and replaced with a stub driver that implements the required calls. The old USB calls will then be rerouted to the computer using UDP packets.

## Software Used
* Android Studio
  * Version: 1.2.1.1
* Eclipse 
  * Version: Mars Release (4.5.0)
* V-Rep Pro EDU
  * Version: 3.2.1
  * Vendor: Coppelia Robotics
* Java JDK 
  * Version: jdk1.8.0_45
* TCPView  (Used to kill process holding port 6500 when pc java program doesn't release it. TODO: fix)
 * Vendor: www.sysinternals.com

## Getting Started

### Download / Clone

Clone the repo using Git:

```bash
git clone --bare https://github.com/HagertyRobotics/ftc_app_networked_simulator
```

> Remember, please use the "onboard" branch as the master branch is still in flux.

### What's included

In the repo you'll find the following directories and files.

| File/Folder     | Provides                                       |
|-----------------|------------------------------------------------|
| Android/App | Android app cloned from the FTC Robot Controller (Android Studio) |
| PC/Java            | PC side simulator.  Connects phone to v-rep (Eclipse Project) |
| PC/V-rep | Coppelia Robotics V-rep simulator scene files. |
| LICENSE.md         | Project license information.                   |
| README.md       | Details for quickly understanding the project. |


### Build

* Use Android Studio to open existing project and point to Android/App.
  * Change PC_IP_ADDRESS define to your PC's address in file: FtcRobotController/src/main/java/com/ftdi.j2xx/NetworkManager.java
  * Compile and run the app
    * Setup the newly found legacy controller with two motors "motor_1" and "motor_2"
    * Save configuration
* Open PC Path editor and add c:\Program Files (x86)\V-REP3\V-REP_PRO_EDU\programming\remoteApiBindings\java\lib\64Bit
* Open V-Rep simulator and open scene file PC/V-rep/FTCRobot.ttt
* Open Eclipse and import PC/Java/RobotSimulator
  * Compile and Run RobotSimulator  
    * Robot Simulator will listen on socket 6500 for incoming packets from the Phone and forward motor commands to the V-Rep simulator
* Open the Driver Control Station App on a 2nd Phone and start ether the TestOp or the TestTankOp commands.  
  * The TestTankOp Mode requires a joystick connected with a OTG cable.
  * The TestOp will increment both motors in a loop.  This will currently show the bug where the 2nd motor write will overwrite the first and the robot will drive in circles.
