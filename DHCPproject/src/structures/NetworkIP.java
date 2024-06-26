package structures;

import server.Server;

import javax.swing.*;

public class NetworkIP implements Runnable{

    private byte[] ip;
    private byte[] client_HAddr ;

    int timeRelease = 10 ; // for Waiting in second

    public boolean isAvailable = true;
    public boolean isWaiting = false;

    public NetworkIP(byte[] ip) {
        this.ip = ip;
    }

    public byte[] getCHAddress() {
        return client_HAddr;
    }

    public byte[] getMyIp() {
        return ip;
    }

    public String getStrigifyIp() {
        String itemIp = "";
        for (byte xip : this.ip) {
            itemIp = itemIp + String.valueOf(xip & 0xff) + ".";
        }
        itemIp = itemIp.substring(0, itemIp.length()-1 );
        return itemIp;
    }

    public void setClientHardware (byte[] chaddress){
        this.client_HAddr = chaddress;
    }


    private void acceptance() throws InterruptedException {
        int release = 30; //in second
        while (release > 0 && !isAvailable){
            release--;
            Thread.sleep(1000);
        }
        //end of allocation
        this.isAvailable = true;
        this.isWaiting = false;
        System.out.println("end of allocation for:" );
        for (byte ch: client_HAddr){
            System.out.printf(String.valueOf(ch & 0xff));
            System.out.printf(" ");
        }
        this.client_HAddr = null;
        System.out.println();
        JOptionPane.showMessageDialog(Server.frame, "End of Allocation ! ");
        Server.frame.validate();
        Server.frame.setState(JFrame.ICONIFIED);
        Server.frame.setState(JFrame.MAXIMIZED_BOTH);
    }

    @Override
    public void run() {
        while (timeRelease > 0 && isWaiting) {
            try {
                timeRelease--;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        if (!isWaiting) {
            if (!isAvailable){
                try {
                    acceptance();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else {
            if (!isAvailable){
//                end of time release of waiting for client request
                isAvailable = true;
                isWaiting = false;
                System.out.println("end of waiting for:");
                for (byte ch: client_HAddr){
                    System.out.printf(String.valueOf(ch & 0xff));
                    System.out.printf(" ");
                }
                client_HAddr = null;
                timeRelease = 10;
                System.out.println();
                JOptionPane.showMessageDialog(Server.frame, "End of Waiting ... ");
                Server.frame.validate();
                Server.frame.setState(JFrame.ICONIFIED);
                Server.frame.setState(JFrame.MAXIMIZED_BOTH);
            }
        }

    }
}
