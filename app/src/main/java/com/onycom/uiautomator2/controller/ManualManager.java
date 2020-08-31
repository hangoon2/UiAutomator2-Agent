package com.onycom.uiautomator2.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.onycom.uiautomator2.utils.Logger;
import com.onycom.uiautomator2.core.UiAutomatorBridge;

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

    public void touchDown(int x, int y) {
        UiAutomatorBridge.getInstance().getInteractionController().doTouchDown(x, y);
//        downTime = SystemClock.uptimeMillis();
//
//        MotionEvent event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0);
//        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
//        addMotionEvent(event);
    }

    public void touchMove(int x, int y) {
        UiAutomatorBridge.getInstance().getInteractionController().doTouchMove(x, y);
//        MotionEvent event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_MOVE, x, y, 0);
//        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
//        addMotionEvent(event);
    }

    public void touchUp(int x, int y) {
        UiAutomatorBridge.getInstance().getInteractionController().doTouchUp(x, y);
//        MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
//        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
//        addMotionEvent(event);
//        downTime = 0;
    }

    public void startTouchEventManager() {
        eventWorkThread = new Thread(new Runnable() {
            private Handler handler;

            public void run() {
                Looper.prepare();

                handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MotionEvent event = null;
                        while( !Thread.currentThread().isInterrupted() ) {
                            try {
                                Thread.sleep(1);
                            } catch(InterruptedException e) {
                                break;
                            }

                            event = null;

                            synchronized (eventList) {
                                if( !eventList.isEmpty() ) {
                                    event = eventList.get(0);
                                    eventList.remove(0);
                                }
                            }

                            if(event == null) continue;

                            UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(event);
                        }
                    }
                });

                Looper.loop();
            }

        });
        eventWorkThread.start();
    }

    public void sendKey(final int keyCode, final boolean bLong) {
        if(keyCode == KEYCODE_RECENT_APP) {

        } else {
            sendKeyEvent(keyCode, bLong);
        }
    }

    private void sendKeyEvent(final int keyCode, final boolean bLong) {
        if(bLong) {
//            if(keyCode == KEYCODE_POWER) return;

            keyLongPress(keyCode);
        } else {
            UiAutomatorBridge.getInstance().getInteractionController().sendKey(keyCode, 0);
//            final int key = keyCode;
//            final Thread task = new Thread(new Runnable() {
//                private Handler handler;
//
//                @Override
//                public void run() {
//                    Looper.prepare();
//
//                    handler = new Handler();
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            ic.sendKey(key, 0);
//                        }
//                    });
//
//                    Looper.loop();
//                }
//            });
//            task.start();
        }
    }

    private void keyLongPress(final int keyCode) {
        final KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        final Thread task = new Thread(new Runnable() {
            private Handler handler;

            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(keyEvent);

                        for (int i = 0; i < COUNT_OF_LONGEVENT; i++) {
                            final KeyEvent newEvent = KeyEvent
                                    .changeTimeRepeat(keyEvent,
                                            SystemClock.uptimeMillis(), i,
                                            keyEvent.getFlags()
                                                    | KeyEvent.FLAG_LONG_PRESS);

                            UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(newEvent);

                            try {
                                Thread.sleep(5);
                            } catch (final InterruptedException e) {
                                // error
                                Logger.info("ManualManager::keyLongPress() InterruptedException - " + e.getMessage());
                            }
                        }

                        UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(new KeyEvent(KeyEvent.ACTION_UP, keyEvent.getKeyCode()));
                    }
                });

                Looper.loop();
            }
        });

        task.start();
    }

    public void close() {
        if(eventWorkThread != null) {
            eventWorkThread.interrupt();
            eventWorkThread = null;
        }
    }

}