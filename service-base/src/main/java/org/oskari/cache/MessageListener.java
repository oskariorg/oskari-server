package org.oskari.cache;

@FunctionalInterface
public interface MessageListener {
    void onMessage(String msg);
}
