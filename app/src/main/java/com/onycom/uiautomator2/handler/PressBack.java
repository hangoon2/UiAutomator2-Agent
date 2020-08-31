package com.onycom.uiautomator2.handler;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;

import static com.onycom.uiautomator2.utils.Device.back;

public class PressBack extends SafeRequestHandler {

    public PressBack(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        back();
        // Press back returns false even when back was successfully pressed.
        // Always return true.
        return new AppiumResponse(getSessionId(request));
    }
}
