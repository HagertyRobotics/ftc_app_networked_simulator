# ftc_app_networked_simulator

The goal of this project is to create a standalone app that will replace the USB connected Modern Robotics hardware (USB Motor Controller, Legacy Motor Controller, USB Servo Controller) with a networked connected computer. This computer will then run a simulation of the controllers and provide a 3d view of a simulated robot. To accomplish this, the FTDI USB to serial driver library file will be removed and replaced with a stub driver that implements the required calls. The old USB calls will then be rerouted to the computer using UDP packets.
