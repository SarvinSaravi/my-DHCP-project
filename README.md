# my-DHCP-project

## Project Coding structure
In this project, 4 classes (in Java language) are used, 2 of which are executable, and of the other 2 classes, one is created to implement the structure of Dhcp message architecture and the other is to implement the concept of IP address.

The Client class has an executive method (main) designed to simulate the behavior of IP-requesting hosts, which has sendDiscovery(), getOffer(), sendRequest(), and getAckCheck() methods. The exchanged messages are in the DhcpMessage class format, the implementation of which is similar to the previously mentioned format, and they are serialized (serialize and deserialize) for sending in UDP format.

The Server class also has an execution method (main), which is the same as the main DHCP Server class, and the main response processes are implemented in this class. Each IP address is considered as a thread from the NetworkIP class, which is called multithreading in the Server class if needed, so that each one responds to a request from a host, and is additionally busy. Do not become a server.

In this protocol, the server uses port 67 and the client uses port 68 to send and receive messages. But in this project, due to the simulation of DHCP protocol, the server uses port 20067 and the client uses port 20068.

The sections related to the graphic interface of the project are created in the Server class and mostly in the Graphic() method and are called in the run() method. If there is no need for a graphical interface or if the execution speed of the program increases, you can simply delete these sections from the code and the program will not lose its functionality because the messages needed for the user to understand, in addition to the graphical interface, Console are also displayed.

An example of the program's graphical interface for a state where an IP is assigned and a request is in a Waiting state is attached to the project.
