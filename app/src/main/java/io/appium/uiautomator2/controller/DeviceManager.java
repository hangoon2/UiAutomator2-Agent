package io.appium.uiautomator2.controller;

import android.graphics.Point;

import io.appium.uiautomator2.utils.Logger;
import io.appium.uiautomator2.handler.PressKeyCode;

import static io.appium.uiautomator2.utils.Device.getUiDevice;

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

    public void touchDown(final Point pt) {
        Logger.info("Touch Down : " + pt.x + ", " + pt.y);
        getUiDevice().click(pt.x, pt.y);
    }

    public void touchMove(final Point pt) {
        
    }

    public void touchUp(final Point pt) {
        
    }

}