package com.onycom.uiautomator2.model.api.touch.appium;

import com.onycom.uiautomator2.model.RequiredField;
import com.onycom.uiautomator2.model.api.BaseModel;

public class TouchGestureModel extends BaseModel {
    @RequiredField
    public TouchLocationModel touch;
    @RequiredField
    public Double time;

    public TouchGestureModel() {}
}
