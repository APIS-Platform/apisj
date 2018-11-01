package org.apis.util;

public class MasternodeSize {
    private long size;
    private byte[] lastNode;

    public MasternodeSize() {
        size = 0;
        lastNode = null;
    }

    public byte[] getLastNode() {
        return lastNode;
    }

    public long getSize() {
        return size;
    }

    public void setLastNode(byte[] lastNode) {
        this.lastNode = lastNode;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
