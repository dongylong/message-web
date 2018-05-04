package com.noriental.messageweb.message.vo;

/**
 * Created by dongyl on 1/20/17.
 */
public enum ResponseStatus {
    SUCCESS("success"),
    FAILED("fail");
    private final String status;

    ResponseStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
