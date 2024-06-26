package message;

import java.io.Serializable;

public class DhcpMessage implements Serializable {
    public byte op; //req OR rep
    public byte htype;
    public byte hlen;
    public byte hops; //number or router between client and server

    public byte[] xid = new byte[4]; //transaction ID

    public byte[] secs = new byte[2];
    public byte[] flags = new byte[2];

    public byte[] ciaddr = new byte[4];

    public byte[] yiaddr = new byte[4];

    public byte[] siaddr = new byte[4];

    public byte[] giaddr = new byte[4];

    public byte[] chaddr = new byte[16];

    public byte[] sname = new byte[64];

    public byte[] file = new byte[128];

    public byte[] options = new byte[312];

}
