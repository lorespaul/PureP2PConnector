package com.lorenzodaneo.p2pBase.messages;

public enum DiscoveryMessage {
    MULTICASTING_REQUEST("MULTICASTING_REQUEST"),
    MULTICASTING_RESPONSE("MULTICASTING_RESPONSE"),
    ;

    private final String message;

    DiscoveryMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
