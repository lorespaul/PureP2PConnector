package com.lorenzodaneo.p2pBase.multicasting;

import com.lorenzodaneo.p2pBase.messages.DiscoveryMessage;

import java.io.IOException;
import java.net.*;

public class Multicaster extends Thread {

    private static InetAddress _MULTICAST_ADDRESS = null;
    private static final int _MULTICAST_PORT = 4446;
    private static final String _PROTOCOL_DIVIDER = "://";

    static {
        try {
            _MULTICAST_ADDRESS = InetAddress.getByName("230.0.0.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static final InetAddress MULTICAST_ADDRESS(){
        return _MULTICAST_ADDRESS;
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    public ExternalAddressResearcher getResearcher() {
        return researcher;
    }

    public void setResearcher(ExternalAddressResearcher researcher) {
        this.researcher = researcher;
    }

    private MulticastSocket socket;
    private ExternalAddressResearcher researcher;

    public String getContactHost() {
        return contactHost;
    }

    private String contactHost = null;
    private boolean starting = true;
    private int timeoutCounter = 0;


    public Multicaster() throws IOException {
        setSocket(new MulticastSocket(_MULTICAST_PORT));
        getSocket().setTimeToLive(255);
        getSocket().setSoTimeout(5000);

        setResearcher(new ExternalAddressResearcher());
    }

    public void publishMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, _MULTICAST_ADDRESS, _MULTICAST_PORT);
        getSocket().send(packet);
    }




    public void run(){
        try {
            getSocket().joinGroup(_MULTICAST_ADDRESS);
            byte[] buf = new byte[1000];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try{
                    getSocket().receive(packet);
                } catch (SocketTimeoutException e){
                    timeoutCounter++;
                    if(timeoutCounter == 10 && starting){
                        break;
                    }
                    continue;
                }
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.equals(DiscoveryMessage.MULTICASTING_REQUEST.getMessage())) {
                    System.out.println("Received message request: " + received);
                    String currentIp = getLocalAddress();// getResearcher().getWanIp();
                    publishMessage(DiscoveryMessage.MULTICASTING_RESPONSE.getMessage() + _PROTOCOL_DIVIDER + currentIp);
                } else if(starting && received.startsWith(DiscoveryMessage.MULTICASTING_RESPONSE.getMessage())){
                    System.out.println("Received message response: " + received);
                    String[] splitResponse = received.split(_PROTOCOL_DIVIDER);
                    if(splitResponse.length >= 2)
                        contactHost = received.split(_PROTOCOL_DIVIDER)[1];
                    starting = false;
                    break;
                }
            }
            getSocket().leaveGroup(_MULTICAST_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String getLocalAddress() throws SocketException {
        while (NetworkInterface.getNetworkInterfaces().hasMoreElements()){
            NetworkInterface net = NetworkInterface.getNetworkInterfaces().nextElement();
            for (InterfaceAddress addr : net.getInterfaceAddresses()){
                String ip = addr.getAddress().getHostAddress();
                if(ip.startsWith("192")){
                    return ip;
                }
            }
        }
        return null;
    }


    public void close(){
        getSocket().close();
    }

}
