package com.onycom.uiautomator2.model;

public class AutomationInfo {

    public boolean bLongPress;
    public boolean bWholeWord;
    public short scrollType;
    public short scrollMaxCount;
    public short scrollInstance;
    public String scrollClass = null;
    public short objType;
    public short objInstance;
    public String value = null;

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

}
