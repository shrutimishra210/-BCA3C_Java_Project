package com.uniswap.gui;

/** Implemented by every panel that needs to reload its data each time it's shown. */
public interface Refreshable {
    void refresh();
}
