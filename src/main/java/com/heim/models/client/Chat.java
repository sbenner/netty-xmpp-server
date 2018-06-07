package com.heim.models.client;

import java.util.Set;

public class Chat {

    private String threadId;
    private Set<String> peers;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }
}
