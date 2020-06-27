package com.lorenzodaneo.p2pBase.messages;

public enum PP2PMessage {
    GET_NET_INFO("GET_NET_INFO"),
    RETURN_NET_INFO("RETURN_NET_INFO")
    ;

    private final String message;

    PP2PMessage(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
