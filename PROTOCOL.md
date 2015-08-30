# Protocol
FTC Networked Simulator

## Main Data Communication
The protocol does the following:
- Tells the Robot Controller what devices the simulator has, 
  the ports they use, and their serial numbers
- Allows the data exchange to take place
- Allows the data to be sorted by what it affects (or "*type*"),
  and depending on the implementation further subdivided by what
  module the data is directed at
- Ulitizes the design of Google's Protobuf
- The program is delimited by the size of the next packets
  * An example of is to consider the following stream:
  > 9 (size) 34 64 33 200 234 245 45 22 64 (data) 34 (size) 31 67 243 53 75... 
 - The size of the data uses 4 bytes
 - Any string within the data uses the ASCII character encoding (later versions, may switch to UTF-8
   for the payload encoding, but still mantain that for non-payload strings be in ASCII)
 - A single heartbeat is sent if the connection is idle for more than 1 second (not implemented correctly)
 - That the request to send data, first waits to until the connection channel is writable
 
  ### Main Data Protocol Implementation
 To be added...
 
## Multicast Protcol
This protocol does the following:
 - Communicates the IP address, and verfies that the sender IP is the same as
 the data in the protocol
 - Communicates what the port the main data is lisenting on (optionally if the port is 7002);
 - Uses the UDP port 7003
 
 ### Multicast Implementation
 To be added...
 