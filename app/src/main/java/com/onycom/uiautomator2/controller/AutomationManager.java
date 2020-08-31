package com.onycom.uiautomator2.controller;

import androidx.test.uiautomator.UiObject;

public class AutomationManager {

    public class UiObjectItem {

        public boolean bLongPress;
        public boolean bWholeWord;

        public short scrollType;
        public short scrollMaxCount;
        public short scrollInstance;
        public short objType;
        public short objInstance;

        public String scrollClass = null;
        public String value = null;

    }

    public enum ScrollType {
        None,
        Vertical,
        Horizontal,
    }

    public enum ObjectType {
        Text,
        Description,
        Class,
        Index,
    }

    private boolean bPlayStop = false;

    public AutomationManager() {

    }

    public String dumpHierarchyData(final String fileName) {
        return null;
    }

    public boolean searchObject(final UiObjectItem objItem) {
        return false;
    }

    public boolean selectObject(final UiObjectItem objItem) {
        boolean ret = false;

        return ret;
    }

    public void setPlayStop(final boolean bPlayStop) {
        this.bPlayStop = bPlayStop;
    }

    private UiObject findObject(final UiObjectItem objItem) {
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

    private UiObject scrollIntoView(final UiObjectItem objItem) {
        return null;
    }

}
