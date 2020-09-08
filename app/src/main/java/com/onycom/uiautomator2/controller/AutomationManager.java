package com.onycom.uiautomator2.controller;

import androidx.test.uiautomator.UiObject;

import com.onycom.uiautomator2.model.AutomationInfo;

import static com.onycom.uiautomator2.utils.Device.getUiDevice;

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
        return null;
    }

    public boolean searchObject(final AutomationInfo info) {
        return false;
    }

    public boolean selectObject(final AutomationInfo info) {
        boolean ret = false;

        return ret;
    }

    public void setPlayStop(final boolean bPlayStop) {
        this.bPlayStop = bPlayStop;
    }

    private UiObject findObject(final AutomationInfo info) {
        return null;
    }

    private UiObject getObject(final short type, final String data,
                               final short instance, final boolean bWholeWord) {
        return null;
    }

    private boolean isVisibleObject(final UiObject object) {
        boolean ret = false;

        return ret;
    }

    private UiObject scrollIntoView(final AutomationInfo info) {
        return null;
    }

}
