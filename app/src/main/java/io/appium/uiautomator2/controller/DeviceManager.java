package io.appium.uiautomator2.controller;

import io.appium.uiautomator2.handler.PressKeyCode;

public class DeviceManager {

    private static DeviceManager device = null;

    private DeviceManager() {

    }

    public static DeviceManager getInstance() {
        if(device == null) {
            device = new DeviceManager();
        }

        return device;
    }

    public void initialize() {

    }

    public void close() {

    }

    public void sendKey(final int keyCode, final boolean bLong) {
        PressKeyCode.sendKeyEvent(keyCode);
    }

}