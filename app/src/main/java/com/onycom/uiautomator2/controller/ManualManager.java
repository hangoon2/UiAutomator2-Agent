package com.onycom.uiautomator2.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerProperties;
import android.view.MotionEvent.PointerCoords;

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

    public void sendMotionEvent(final int type, final Point[] pt, final int width, final int height) {
        final PointerProperties[] properties = new PointerProperties[pt.length];
        final PointerCoords[] pointerCoords = new PointerCoords[pt.length];

        for(int i = 0; i < pt.length; i++) {
            if(pt[i].x < 0 || pt[i].x > width) {
                break;
            }

            if(pt[i].y < 0 || pt[i].y > height) {
                break;
            }

            final PointerCoords touch = new PointerCoords();
            touch.x = pt[i].x;
            touch.y = pt[i].y;
            touch.pressure = 1;
            touch.size = 1;
            pointerCoords[i] = touch;

            final PointerProperties prop = new PointerProperties();
            prop.id = i;
            prop.toolType = MotionEvent.TOOL_TYPE_FINGER;
            properties[i] = prop;
        }

        final long time = SystemClock.uptimeMillis();

        switch(type) {
            case MotionEvent.ACTION_DOWN: {
                final MotionEvent downEvent = MotionEvent.obtain(time,
                        SystemClock.uptimeMillis(), type, 1, properties,
                        pointerCoords, 0, 0, 1, 1, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);
                addMotionEvent(downEvent);
                downTime = time;
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final MotionEvent moveEvent = MotionEvent.obtain(downTime,
                        SystemClock.uptimeMillis(), type, pt.length, properties,
                        pointerCoords, 0, 0, 1, 1, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);
                addMotionEvent(moveEvent);
            }
            break;

            case MotionEvent.ACTION_UP: {
                final MotionEvent upEvent = MotionEvent.obtain(downTime,
                        SystemClock.uptimeMillis(), type, 1, properties,
                        pointerCoords, 0, 0, 1, 1, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);
                addMotionEvent(upEvent);
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                final MotionEvent multiDownEvent = MotionEvent.obtain(downTime,
                        SystemClock.uptimeMillis(),
                        getPointerAction(MotionEvent.ACTION_POINTER_DOWN, 1), 2, properties,
                        pointerCoords, 0, 0, 1, 1, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);
                addMotionEvent(multiDownEvent);
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                final MotionEvent multiUpEvent = MotionEvent.obtain(downTime,
                        SystemClock.uptimeMillis(),
                        getPointerAction(MotionEvent.ACTION_POINTER_UP, 1), 2, properties,
                        pointerCoords, 0, 0, 1, 1, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);
                addMotionEvent(multiUpEvent);
                downTime = 0;
            }
            break;
        }
    }

    private int getPointerAction(int motionEvent, int index) {
        return motionEvent + (index << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
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

                            Logger.info("Motion Event[" + event.getAction() + "] : " + event.getX() + ", " + event.getY());
                            UiAutomatorBridge.getInstance().getInteractionController().injectEvent(event);
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
        final long downTime = SystemClock.uptimeMillis();
        UiAutomatorBridge.getInstance().getInteractionController().injectEvent(new KeyEvent(downTime, downTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, 0));
        UiAutomatorBridge.getInstance().getInteractionController().injectEvent(new KeyEvent(downTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN, keyCode, 1, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, KeyEvent.FLAG_LONG_PRESS));
        UiAutomatorBridge.getInstance().getInteractionController().injectEvent(new KeyEvent(downTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, 0));

//        final KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
//        final Thread task = new Thread(new Runnable() {
//            private Handler handler;
//
//            @OverrideUiAutomatorBridge.getInstance().getInteractionController()
//            public void run() {
//                Looper.prepare();
//                handler = new Handler();
//                handler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(keyEvent);
//
//                        for (int i = 0; i < COUNT_OF_LONGEVENT; i++) {
//                            final KeyEvent newEvent = KeyEvent
//                                    .changeTimeRepeat(keyEvent,
//                                            SystemClock.uptimeMillis(), i,
//                                            keyEvent.getFlags()
//                                                    | KeyEvent.FLAG_LONG_PRESS);
//
//                            UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(newEvent);
//
//                            try {
//                                Thread.sleep(5);
//                            } catch (final InterruptedException e) {
//                                // error
//                                Logger.info("ManualManager::keyLongPress() InterruptedException - " + e.getMessage());
//                            }
//                        }
//
//                        UiAutomatorBridge.getInstance().getInteractionController().injectEventSync(new KeyEvent(KeyEvent.ACTION_UP, keyEvent.getKeyCode()));
//                    }
//                });
//
//                Looper.loop();
//            }
//        });
//
//        task.start();
    }

    public void close() {
        if(eventWorkThread != null) {
            eventWorkThread.interrupt();
            eventWorkThread = null;
        }
    }

}