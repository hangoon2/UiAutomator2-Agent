package com.onycom.uiautomator2.controller;

import android.os.Environment;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import com.onycom.uiautomator2.core.AccessibilityNodeInfoDumper;
import com.onycom.uiautomator2.model.AutomationInfo;
import com.onycom.uiautomator2.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.onycom.uiautomator2.utils.Device.getUiDevice;
import static com.onycom.uiautomator2.utils.AXWindowHelpers.refreshAccessibilityCache;

public class AutomationManager {

    private static final int SCROLL_STEPS = 55;

    private final static short OBJECT_TYPE_TEXT = 0;
    private final static short OBJECT_TYPE_DESCRIPTION = 1;
    private final static short OBJECT_TYPE_CLASS = 2;
//	private final static short OBJECT_TYPE_INDEX = 3;

    private final static short SCROLL_TYPE_NONE = 0;
    private final static short SCROLL_TYPE_VERTICAL = 1;
//	final static short SCROLL_TYPE_HORIZONTAL = 2;

    private boolean bPlayStop = false;

    public AutomationManager() {

    }

    public String dumpHierarchyData(final String fileName) {
        final String dataDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String dumpDir = dataDir + "/local/tmp";
        final String filePath = dumpDir + "/" + fileName;
        final File dir = new File(dumpDir);
        if( dir.exists() == false ) {
            // 디렉토리가 존재하지 않으면 생성한다.
            dir.mkdirs();
        }

        boolean ret = getUiDevice().takeScreenshot( new File(dumpDir + "/scene.png") );

        final File file = new File(filePath);
        if( file.exists() ) {
            // 기존 파일이 존재하면 삭제한다.
            file.delete();
        }

        refreshAccessibilityCache();
        String dump = new AccessibilityNodeInfoDumper().dumpToXml();

        try {
            OutputStream out = new FileOutputStream(filePath);
            out.write( dump.getBytes() );

            return filePath;
        } catch (Exception e) {
            Logger.info( "AutomationManager::dumpHierarchyData Exception - " + e.getMessage() );
        }

        return null;
    }

    public boolean searchObject(final AutomationInfo info) {
        final UiObject object = findObject(info);
        if( object != null && object.exists() ) {
            return true;
        }

        return false;
    }

    public boolean selectObject(final AutomationInfo info) {
        boolean ret = false;

        Logger.info("SelectObject : " + info.value + ", " + info.bLongPress + ", " + info.bWholeWord + ", " + info.objInstance);
        final UiObject object = findObject(info);
        Logger.info("Select Object2 : " + object);
        if( object != null && object.exists() ) {
            try {
                if(info.bLongPress) {
                    object.longClick();
                } else {
                    object.clickAndWaitForNewWindow();
                }

                ret = true;
            } catch(final UiObjectNotFoundException e) {
                Logger.info( "AutomationManager::selectObject UiObjectNotFoundException - " + e.getMessage() );
            }
        }

        Logger.info("Select Object Result : " + ret);
        return ret;
    }

    public void setPlayStop(final boolean bPlayStop) {
        this.bPlayStop = bPlayStop;
    }

    private UiObject findObject(final AutomationInfo info) {
        UiObject object = getObject(info.objType, info.value, info.objInstance, info.bWholeWord);
        if( object == null || object.exists() == false ) {
            if(info.scrollClass != null && info.scrollType != SCROLL_TYPE_NONE) {
                object = scrollIntoView(info);
            }
        }

        return object;
    }

    private UiObject getObject(final short type, final String data,
                               final short instance, final boolean bWholeWord) {
        UiObject object = null;

        switch (type) {
            case OBJECT_TYPE_TEXT:
                object = new UiObject( new UiSelector().text(data).instance(instance) );
                break;

            case OBJECT_TYPE_DESCRIPTION:
                object = new UiObject( new UiSelector().description(data).instance(instance) );
                break;

            case OBJECT_TYPE_CLASS:
                object = new UiObject( new UiSelector().className(data).instance(instance) );
                break;
        }

        return object;
    }

    private boolean isVisibleObject(final UiObject object) {
        boolean ret = false;
        int displayWidth = getUiDevice().getDisplayWidth();
        int displayHeight = getUiDevice().getDisplayHeight();

        try {
            if (object != null && object.exists()) {
                if (object.getBounds().centerX() < displayWidth &&
                        object.getBounds().centerY() < displayHeight) {
                    ret = true;
                }
            }
        } catch(final UiObjectNotFoundException e) {
            Logger.info( "AutomationManager::isVisibleObject UiObjectNotFoundException - " + e.getMessage() );
        }

        return ret;
    }

    private UiObject scrollIntoView(final AutomationInfo info) {
        final UiScrollable scroll = new UiScrollable( new UiSelector().className(info.scrollClass).instance(info.scrollInstance) );
        if( scroll == null || scroll.exists() == false ) {
            return null;
        }

        UiObject object = null;

        try {
            for(int i = 0; i < info.scrollMaxCount; i++) {
                if(bPlayStop) {
                    setPlayStop(false);
                    return null;
                }

                object = getObject(info.objType, info.value, info.objInstance, info.bWholeWord);
                if( isVisibleObject(object) ) {
                    return object;
                }

                if(info.scrollType == SCROLL_TYPE_VERTICAL) {
                    if( !scroll.swipeDown(SCROLL_STEPS) ) {
                        break;
                    }
                } else {
                    if( !scroll.swipeRight(SCROLL_STEPS) ) {
                        break;
                    }
                }
            }

            for(int i = 0; i < info.scrollMaxCount * 2; i++) {
                if(bPlayStop) {
                    setPlayStop(false);
                    return null;
                }

                object = getObject(info.objType, info.value, info.objInstance, info.bWholeWord);
                if( isVisibleObject(object) ) {
                    return object;
                }

                if(info.scrollType == SCROLL_TYPE_VERTICAL) {
                    if( !scroll.swipeUp(SCROLL_STEPS) ) {
                        break;
                    }
                } else {
                    if( !scroll.swipeLeft(SCROLL_STEPS) ) {
                        break;
                    }
                }
            }
        } catch(final UiObjectNotFoundException e) {
            Logger.info( "AutomationManager::scrollIntoView UiObjectNotFoundException - " + e.getMessage() );
        }

        object = getObject(info.objType, info.value, info.objInstance, info.bWholeWord);
        if( isVisibleObject(object) ) {
            return object;
        }

        return object;
    }

}
