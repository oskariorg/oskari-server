package org.oskari.cluster;

@FunctionalInterface
public interface MessageListener {
    void onMessage(String msg);
}
