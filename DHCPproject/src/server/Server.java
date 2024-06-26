package server;

import message.DhcpMessage;
import structures.NetworkIP;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    DatagramSocket serverSock;

    // range of ip addresses is 192.168.1.X/28 => 16 items
    private static volatile ArrayList<NetworkIP> rangeIP = new ArrayList<>();
    ExecutorService pool;

    public Server() {
        System.out.println("an empty constructor");
    }

    public Server(int serverSock) throws SocketException {
        this.serverSock = new DatagramSocket(serverSock);
        for (int i = 0; i < 16; i++) {
            byte[] oneIP = new byte[4];
            oneIP[0] = (byte) 192;
            oneIP[1] = (byte) 168;
            oneIP[2] = (byte) 1;
            oneIP[3] = (byte) (100 + i);
            NetworkIP item = new NetworkIP(oneIP);
            rangeIP.add(item);
        }
        pool = Executors.newFixedThreadPool(16);
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

    public byte[] getIP(byte[] ch) {
        byte[] clientIp = new byte[4];
        boolean found = false;
        for (NetworkIP xip : rangeIP) {
            if (Arrays.equals(ch, xip.getCHAddress())){
                System.out.println("This is a duplicated or malicious request ... ");
                JOptionPane.showMessageDialog(null, "This is a duplicated or a malicious request ");
                return clientIp;
            }
        }
        for (NetworkIP x_ip : rangeIP) {
            if (x_ip.isAvailable) {
                    x_ip.isAvailable = false;
                    x_ip.isWaiting = true;
                    x_ip.setClientHardware(ch);
                    clientIp = x_ip.getMyIp();
                    pool.execute(x_ip);
                    found = true;
                    break;
            }
        }
        if (!found) {
            System.out.println("server can not found available ip for this request ");
            JOptionPane.showMessageDialog(frame, "Server can not found available ip for this request");
        }
        return clientIp;
    }

    private boolean allocation(byte[] requestedIp, byte[] client_hardware) {
        boolean found2 = false;
        for (NetworkIP myIp : rangeIP) {
            if (Arrays.equals(requestedIp, myIp.getMyIp()) && Arrays.equals(client_hardware, myIp.getCHAddress())) {
                if (myIp.isWaiting && !myIp.isAvailable) {
                    myIp.isWaiting = false;
                    found2 = true;
                    break;
                }
            }
        }
        if (!found2) {
            System.out.println("sorry ! There is a problem!");
            JOptionPane.showMessageDialog(frame, "Sorry! There is a problem in Allocate Operation!");
            // send NACK
        }
        return found2;
    }

    public byte[] getServerIP() {
        byte[] sip = new byte[4];
        try {
            sip = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return sip;
    }

    private void DiscoverToOffer(DhcpMessage rc) throws IOException {
        rc.op = 2;
        rc.yiaddr = getIP(rc.chaddr);
        rc.siaddr = getServerIP();
        rc.options[6] = (byte) 2; //DHCPOFFER

        byte[] offerBuffer = serialize(rc);
        DatagramPacket offerPack = new DatagramPacket(offerBuffer, offerBuffer.length, InetAddress.getByName("255.255.255.255"), 20068);

        serverSock.setBroadcast(true);
        serverSock.send(offerPack);
    }

    private void ReqToAck(DhcpMessage rc) throws IOException {
        boolean AckNack = allocation(rc.ciaddr, rc.chaddr);
        rc.op = 2;
        rc.yiaddr = rc.ciaddr.clone();
        rc.ciaddr = ByteBuffer.allocate(4).putInt(0).array();
        if (AckNack) {
            rc.options[6] = (byte) 5; //DHCPACK
        } else {
            rc.options[6] = (byte) 6; //DHCPNACK
        }

        byte[] offerBuffer = serialize(rc);
        DatagramPacket offerPack = new DatagramPacket(offerBuffer, offerBuffer.length, InetAddress.getByName("255.255.255.255"), 20068);

        serverSock.setBroadcast(true);
        serverSock.send(offerPack);
    }

    public static JFrame frame;
    JPanel mainPanel;
    JTable jt;

    private void Graphic() {
        frame = new JFrame("DHCP Server");
        frame.setBounds(400, 150, 300, 300);

        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(100, 200, 100, 200));
        mainPanel.setBackground(Color.ORANGE);
        mainPanel.setLayout(new GridLayout(0, 1));

        String data[][] = new String[8][2];
        int x = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 2; j++) {
                data[i][j] = rangeIP.get(x).getStrigifyIp();
                x++;
            }
        }
        String column[] = {"IP", "IP"};

        jt = new JTable(data, column){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);

                Object value = getModel().getValueAt(row, col);
                for (NetworkIP nip: rangeIP){
                    if (value.equals(nip.getStrigifyIp())){
                        if (nip.isAvailable) {
                            comp.setBackground(Color.GREEN);
                        } else if(nip.isWaiting){
                            comp.setBackground(new Color(190,0,255));
                        }else if(!nip.isWaiting && !nip.isAvailable) {
                            comp.setBackground(new Color(51,153,255));
                        }
                    }
                }
                return comp;
            }
        };
        jt.setRowHeight(30);
        jt.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(jt);
        sp.setPreferredSize(new Dimension(260,265));

        mainPanel.add(sp);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300,400);
        frame.pack();
        frame.setVisible(true);
    }

    public void run() throws IOException {
        boolean listening = true;
        Graphic();
        while (listening) {
            byte[] buffer = new byte[2048];  //2048
            DatagramPacket pack = new DatagramPacket(buffer, buffer.length);
            System.out.println("Server starts listening ...");
            serverSock.receive(pack);
            try {
                DhcpMessage receiveMessage = (DhcpMessage) deserialize(pack.getData());
                if ((receiveMessage.options[4] == 53) && (receiveMessage.options[6] == 1)) {
                    System.out.println("discovery => Offer");
//                    JOptionPane.showMessageDialog(frame, "A DISCOVERY DHCP Message ");
                    DiscoverToOffer(receiveMessage);
                } else if ((receiveMessage.options[4] == 53) && (receiveMessage.options[6] == 3)) {
                    System.out.println("request => Ack");
//                    JOptionPane.showMessageDialog(frame, "A REQUEST DHCP Message ");
                    ReqToAck(receiveMessage);
                }
                jt.validate();
                mainPanel.revalidate();
                frame.validate();
                frame.setState(JFrame.ICONIFIED);
                frame.setState(JFrame.MAXIMIZED_BOTH);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                listening = false;
            }

        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(20067);
            server.run();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
