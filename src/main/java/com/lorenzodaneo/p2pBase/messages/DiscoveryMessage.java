package com.lorenzodaneo.p2pBase.messages;

public enum DiscoveryMessage {
    BROADCASTING_REQUEST("BROADCASTING_REQUEST"),
    BROADCASTING_RESPONSE("BROADCASTING_RESPONSE"),
    ;

    private String message;

    DiscoveryMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
