package com.lorenzodaneo.p2pBase.broadcasting;

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

    public MulticastSocket getReceiverSocket() {
        return receiverSocket;
    }

    public void setReceiverSocket(MulticastSocket receiverSocket) {
        this.receiverSocket = receiverSocket;
    }

    public DatagramSocket getPublisherSocket() {
        return publisherSocket;
    }

    public void setPublisherSocket(DatagramSocket publisherSocket) {
        this.publisherSocket = publisherSocket;
    }

    private MulticastSocket receiverSocket;
    private DatagramSocket publisherSocket;

    public String getContactHost() {
        return contactHost;
    }

    private String contactHost = null;
    private boolean starting = true;


    public Multicaster() throws IOException {
        setReceiverSocket(new MulticastSocket(_MULTICAST_PORT));
        getReceiverSocket().setTimeToLive(1000);
        setPublisherSocket(new DatagramSocket());
    }

    public void publishMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, _MULTICAST_ADDRESS, _MULTICAST_PORT);
        getPublisherSocket().send(packet);
        getPublisherSocket().close();
    }


    public void run(){
        try {
            getReceiverSocket().joinGroup(_MULTICAST_ADDRESS);
            byte[] buf = new byte[1000];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                getReceiverSocket().receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.equals(DiscoveryMessage.BROADCASTING_REQUEST.getMessage())) {
                    String currentIp = getPublisherSocket().getLocalAddress().getHostAddress();
                    publishMessage(DiscoveryMessage.BROADCASTING_RESPONSE.getMessage() + _PROTOCOL_DIVIDER + currentIp);
                } else if(starting && received.startsWith(DiscoveryMessage.BROADCASTING_RESPONSE.getMessage())){
                    contactHost = received.split(_PROTOCOL_DIVIDER)[1];
                    starting = false;
                    break;
                }
            }
            getReceiverSocket().leaveGroup(_MULTICAST_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getReceiverSocket().close();
        }
    }

}
