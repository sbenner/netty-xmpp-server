package com.heim.netty;


import com.heim.models.client.Chat;
import io.netty.channel.ChannelHandlerContext;

import java.util.Set;

public class SessionContext {

    private ChannelHandlerContext ctx;
    private String to;
    private String from;
    private String user;
    private boolean secured;
    private String jid;
    private StringBuilder packetBuffer;
    private Set<Chat> chatThreads;

    private boolean authorized = false;

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "\n[" +
                " user: " + getUser() +
                " to: " + getTo() +
                " from: " + getFrom() +
                " isAuthorized: " + isAuthorized() +
                " JID: " + getJid() +
                " ContextWritable " + getCtx().channel().isWritable() +
                " ]\n";
    }

    public StringBuilder getPacketBuffer() {
        return packetBuffer;
    }

    public void setPacketBuffer(StringBuilder packetBuffer) {
        this.packetBuffer = packetBuffer;
    }

    public Set<Chat> getChatThreads() {
        return chatThreads;
    }

    public void setChatThreads(Set<Chat> chatThreads) {
        this.chatThreads = chatThreads;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }
}
