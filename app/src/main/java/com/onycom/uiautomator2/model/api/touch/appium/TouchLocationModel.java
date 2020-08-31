package com.onycom.uiautomator2.model.api.touch.appium;

import com.onycom.uiautomator2.model.RequiredField;
import com.onycom.uiautomator2.model.api.BaseModel;

public class TouchLocationModel extends BaseModel {
    @RequiredField
    public Double x;
    @RequiredField
    public Double y;

    public TouchLocationModel() {}
}
