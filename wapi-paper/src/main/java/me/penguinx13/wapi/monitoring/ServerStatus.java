package me.penguinx13.wapi.monitoring;

public class ServerStatus {

    private String serverId;
    private double tps;
    private int players;
    private int maxPlayers;
    private long ramUsed;
    private long ramMax;
    private long timestamp;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(final String serverId) {
        this.serverId = serverId;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(final double tps) {
        this.tps = tps;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(final int players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(final int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public long getRamUsed() {
        return ramUsed;
    }

    public void setRamUsed(final long ramUsed) {
        this.ramUsed = ramUsed;
    }

    public long getRamMax() {
        return ramMax;
    }

    public void setRamMax(final long ramMax) {
        this.ramMax = ramMax;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
}
