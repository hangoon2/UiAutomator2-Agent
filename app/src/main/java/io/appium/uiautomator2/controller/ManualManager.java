package io.appium.uiautomator2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import static io.appium.uiautomator2.utils.Device.getUiDevice;

public class ManualManager {

    private static final int COUNT_OF_LONGEVENT = 50;
    private static final int SPEED_OF_SWIPE = 5;

    private static final int KEYCODE_HOME = 3;
    private static final int KEYCODE_BACK = 4;
    private static final int KEYCODE_POWER = 26;
    private static final int KEYCODE_RECENT_APP = 300;

    private Thread eventWorkThread = null;

    private List<MotionEvent> eventList = Collections.synchronizedList( new ArrayList<MotionEvent>() );

    private static long downTime = 0;

    public ManualManager() {

    }

    public void addMotionEvent(MotionEvent event) {
        eventList.add(event);
    }

    public void startTouchEventManager() {
        eventWorkThread = new Thread(new Runnable() {

            public void run() {

            }

        });
        eventWorkThread.start();
    }

}