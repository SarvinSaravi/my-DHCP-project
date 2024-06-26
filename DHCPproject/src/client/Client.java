package client;

import message.DhcpMessage;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class Client {
    public int transactionID;

    DatagramSocket clientSocket;
    DhcpMessage dhcpMsg ;
    DatagramPacket dgpack;

    public Client() {
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public Client(int port) {
        try {
            clientSocket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public void run(){
        transactionID = new Random().nextInt(1024);

        try {
            sendDiscovery();
            getOffer();
            sendRequest();
            getAckCheck();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }
    }

    public void sendDiscovery() throws IOException{
        dhcpMsg = new DhcpMessage();

        dhcpMsg.op = 1; // OP: request
        dhcpMsg.htype = 1; // hw_type: ethernet
        dhcpMsg.hlen = 6; // hw_addr_len: 6 for ethernet
        dhcpMsg.hops = 0;

        dhcpMsg.xid = ByteBuffer.allocate(4).putInt(transactionID).array();

        dhcpMsg.secs = ByteBuffer.allocate(2).putShort((short) 0).array();
        dhcpMsg.flags = ByteBuffer.allocate(2).putShort((short) 0).array();

        dhcpMsg.ciaddr = ByteBuffer.allocate(4).putInt(0).array();

        dhcpMsg.yiaddr = ByteBuffer.allocate(4).putInt(0).array();

        dhcpMsg.siaddr = ByteBuffer.allocate(4).putInt(0).array();

        dhcpMsg.giaddr = ByteBuffer.allocate(4).putInt(0).array();

        InetAddress localHost = InetAddress.getLocalHost();
        NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
        byte[] hardwareAddress = ni.getHardwareAddress();
        dhcpMsg.chaddr = ByteBuffer.allocate(16).putInt(0).array();
        for (int i = 0; i < hardwareAddress.length; i++) {
            dhcpMsg.chaddr[i] = hardwareAddress[i];
        }

        dhcpMsg.sname = ByteBuffer.allocate(32).putInt(0).array();

        dhcpMsg.file = ByteBuffer.allocate(128).putInt(0).array();

        //magic cookie options 0x63825363
        dhcpMsg.options[0] = (byte) 99;
        dhcpMsg.options[1] = (byte) 130;
        dhcpMsg.options[2] = (byte) 83;
        dhcpMsg.options[3] = (byte) 99;
        //type of DHCP message (1 byte option type, 1 byte option length in bytes, n bytes option value)
        dhcpMsg.options[4] = (byte) 53;
        dhcpMsg.options[5] = (byte) 1;
        dhcpMsg.options[6] = (byte) 1; //discovery message
        // DHCP options End Octet
        dhcpMsg.options[7] = (byte) 255;

        byte[] buffer = serialize(dhcpMsg);

        dgpack = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 20067);

        clientSocket.setBroadcast(true);
        clientSocket.send(dgpack);
        System.out.println("end of discovery");

    }

    public void getOffer() throws IOException, ClassNotFoundException {
        byte[] offerBuf = new byte[1024];
        DatagramPacket receivedOffer = new DatagramPacket(offerBuf, offerBuf.length);
        clientSocket.receive(receivedOffer);
        dhcpMsg = (DhcpMessage) deserialize(receivedOffer.getData());
        System.out.println("end of get Offer message");
    }

    public void sendRequest() throws IOException {
        System.out.println("try to request for : ");
        for (byte x: dhcpMsg.yiaddr){
            System.out.printf(String.valueOf(x & 0xff));
            System.out.printf(" ");
        }
        System.out.println();
        dhcpMsg.op = 1;
        dhcpMsg.ciaddr = dhcpMsg.yiaddr.clone();
        dhcpMsg.yiaddr = ByteBuffer.allocate(4).putInt(0).array();

        dhcpMsg.options[4] = (byte) 53;
        dhcpMsg.options[5] = (byte) 1;
        dhcpMsg.options[6] = (byte) 3; //request message

        dhcpMsg.options[7] = (byte) 255;

        byte[] buffer = serialize(dhcpMsg);

        dgpack = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 20067);

        clientSocket.setBroadcast(true);
        clientSocket.send(dgpack);

        System.out.println("end of send request");

    }

    public void getAckCheck() throws IOException, ClassNotFoundException {
        byte[] AckBuf = new byte[1024];
        DatagramPacket receivedAck = new DatagramPacket(AckBuf, AckBuf.length);
        clientSocket.receive(receivedAck);
        dhcpMsg = (DhcpMessage) deserialize (receivedAck.getData());
        if ((dhcpMsg.options[4] == 53) && (dhcpMsg.options[6] == 5)){
            System.out.println("Acknowledge");
        } else if ((dhcpMsg.options[4] == 53) && (dhcpMsg.options[6] == 6)){
            System.out.println("Not Acknowledge");
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }


    public static void main(String[] args) {

        Client myClient = new Client(20068);
        myClient.run();

    }
}
