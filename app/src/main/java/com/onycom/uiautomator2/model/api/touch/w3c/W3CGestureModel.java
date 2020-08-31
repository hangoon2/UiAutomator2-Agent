package com.onycom.uiautomator2.model.api.touch.w3c;

import com.onycom.uiautomator2.model.RequiredField;
import com.onycom.uiautomator2.model.api.BaseModel;

public class W3CGestureModel extends BaseModel {
    @RequiredField
    public String type;
    public Long duration;
    public Object origin;
    public Double x;
    public Double y;
    public Integer button;
    public String value;
    public Double size;
    public Double pressure;

    public W3CGestureModel() {}
}
