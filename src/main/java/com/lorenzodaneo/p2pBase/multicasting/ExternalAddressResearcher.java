package com.lorenzodaneo.p2pBase.multicasting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ExternalAddressResearcher {

    public String getWanIp(){
        try {

            URL url = new URL("http://bot.whatismyipaddress.com");

            BufferedReader bReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String wanIp = bReader.readLine();
            System.out.println("Your IP address is " + wanIp);
            return wanIp;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
