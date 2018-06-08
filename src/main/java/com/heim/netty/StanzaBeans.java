package com.heim.netty;

import org.springframework.stereotype.Component;

@Component
public class StanzaBeans {


    private String features;
    private String bindOk;
    private String authOk;
    private String success;
    private String start;
    private String rosterTest;
    private String rosterSergey;
    private String message;

    public StanzaBeans() {
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getBindOk() {
        return bindOk;
    }

    public void setBindOk(String bindOk) {
        this.bindOk = bindOk;
    }

    public String getAuthOk() {
        return authOk;
    }

    public void setAuthOk(String authOk) {
        this.authOk = authOk;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getRosterTest() {
        return rosterTest;
    }

    public void setRosterTest(String rosterTest) {
        this.rosterTest = rosterTest;
    }

    public String getRosterSergey() {
        return rosterSergey;
    }

    public void setRosterSergey(String rosterSergey) {
        this.rosterSergey = rosterSergey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
