package com.softwarevax.flume.client.configura;

public enum Action {

    START("/start"), STOP("/stop"), INTERACT("/interact");

    private String url;

    Action(String url) {
        this.url = url;
    }

    public String url() {
        return this.url;
    }
}
