package com.lorenzodaneo.p2pBase.multicasting;

import com.lorenzodaneo.p2pBase.messages.DiscoveryMessage;
import com.lorenzodaneo.p2pBase.messages.PP2PMessage;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

public class Multicaster extends Thread {

    private static InetAddress _MULTICAST_ADDRESS = null;
    private static final int _MULTICAST_PORT = 4446;
    private static final String _PROTOCOL_DIVIDER = "://";
    private static final String _SLASH = "/";
    private static final String _PP2P = "pp2p";


    private static enum PP2PPacketEnum{
        PROTOCOL,
        DISCOVERY,
        ADDRESS,
        PP2P,
        MESSAGE
    }

    static {
        try {
            _MULTICAST_ADDRESS = InetAddress.getByName("224.1.0.1");
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
    private final boolean starting;
    private int timeoutCounter = 0;


    public Multicaster(boolean starting) throws IOException {
        setSocket(new MulticastSocket(_MULTICAST_PORT));
        getSocket().setTimeToLive(255);
        getSocket().setSoTimeout(5000);

        setResearcher(new ExternalAddressResearcher());
        this.starting = starting;
    }


    public void accessPP2PNetwork(){
        publishMessage(DiscoveryMessage.MULTICASTING_REQUEST, PP2PMessage.GET_NET_INFO);
    }


    private void publishMessage(DiscoveryMessage discovery, PP2PMessage pp2p) {
        publishMessage(discovery, pp2p, null);
    }

    private void publishMessage(DiscoveryMessage discovery, PP2PMessage pp2p, String message) {
        try {
            String localAddress = getLocalAddress();
            String send = _PP2P + _PROTOCOL_DIVIDER + discovery.getMessage() + _SLASH + localAddress + _SLASH + pp2p;
            if(message != null){
                send += _SLASH + message;
            }
            byte[] buffer = send.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, _MULTICAST_ADDRESS, _MULTICAST_PORT);
            getSocket().send(packet);
            System.out.println("Message sent: " + send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String parsePacket(String packet, PP2PPacketEnum stringPart){
        String[] firstSplit = packet.split(_PROTOCOL_DIVIDER);
        String[] secondSplit = firstSplit[1].split(_SLASH);
        switch (stringPart){
            case PROTOCOL: return firstSplit[0];
            case DISCOVERY: return secondSplit[0];
            case ADDRESS: return secondSplit[1];
            case PP2P: return secondSplit[2];
            case MESSAGE:
                if(secondSplit.length == 4){
                    return secondSplit[3];
                }
                break;
        }
        return null;
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
                    if(timeoutCounter++ == 10 && starting){
                        break;
                    }
                    continue;
                }
                String received = new String(packet.getData(), 0, packet.getLength());

                if(getLocalAddress() != null && !getLocalAddress().equals(parsePacket(received, PP2PPacketEnum.ADDRESS))){

                    if (parsePacket(received, PP2PPacketEnum.DISCOVERY).equals(DiscoveryMessage.MULTICASTING_REQUEST.getMessage())) {
                        System.out.println("Received message request: " + received);
                        Thread.sleep(2000);
                        publishMessage(DiscoveryMessage.MULTICASTING_RESPONSE, PP2PMessage.RETURN_NET_INFO, "infooooo");
                    } else if(starting && parsePacket(received, PP2PPacketEnum.DISCOVERY).equals(DiscoveryMessage.MULTICASTING_RESPONSE.getMessage())){
                        System.out.println("Received message response: " + received);
                        contactHost = parsePacket(received, PP2PPacketEnum.ADDRESS);
                        break;
                    }

                }
            }
            getSocket().leaveGroup(_MULTICAST_ADDRESS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    private String getLocalAddress() throws SocketException {
        List<NetworkInterface> niList = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface net : niList){
            for (InterfaceAddress addr : net.getInterfaceAddresses()){
                String ip = addr.getAddress().getHostAddress();
                if(ip.startsWith("192.168.0")){
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
