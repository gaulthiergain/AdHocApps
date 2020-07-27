package com.montefiore.gaulthiergain.chatadhoc;

public class Message {

    private long time;
    private String text;
    private MemberData data;
    private boolean belongsToCurrentUser;
    private String broadcastId;

    public Message() {

    }

    Message(String text, MemberData data, boolean belongsToCurrentUser) {
        this.time = System.currentTimeMillis();
        this.text = text;
        this.data = data;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.broadcastId = data.getName() + time;
    }

    Message(String text, boolean belongsToCurrentUser) {
        this.time = System.currentTimeMillis();
        this.text = text;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public MemberData getData() {
        return data;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public long getTime() {
        return time;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public void setBelongsToCurrentUser(boolean belongsToCurrentUser) {
        this.belongsToCurrentUser = belongsToCurrentUser;
    }
}
