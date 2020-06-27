package com.lorenzodaneo.p2pBase;

import com.lorenzodaneo.p2pBase.multicasting.Multicaster;
import com.lorenzodaneo.p2pBase.messages.DiscoveryMessage;

import java.io.IOException;

public class P2PConsole {

    static Multicaster multicaster;


    public static void main(String[] argv) throws IOException, InterruptedException {
        multicaster = new Multicaster(true);
        multicaster.accessPP2PNetwork();
        multicaster.start();
        multicaster.join();

        if(multicaster.getContactHost() != null){
            System.out.println(multicaster.getContactHost() + " -- Oh yeah!!!");
        }

        multicaster = new Multicaster(false);
        multicaster.start();
        multicaster.join();
    }

}
