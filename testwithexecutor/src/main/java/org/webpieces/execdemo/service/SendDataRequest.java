package org.webpieces.execdemo.service;

public class SendDataRequest {
    private int num;

    public SendDataRequest() {
    }
    public SendDataRequest(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
