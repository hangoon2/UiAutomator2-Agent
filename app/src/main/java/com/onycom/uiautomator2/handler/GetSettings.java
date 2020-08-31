package com.onycom.uiautomator2.handler;

import androidx.annotation.VisibleForTesting;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;

import java.util.HashMap;
import java.util.Map;

import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.settings.ISetting;
import com.onycom.uiautomator2.model.settings.Settings;
import com.onycom.uiautomator2.utils.Logger;

/**
 * This method return settings
 */
public class GetSettings extends SafeRequestHandler {

    public GetSettings(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        Logger.debug("Get settings:");
        return new AppiumResponse(getSessionId(request), getPayload());
    }

    @VisibleForTesting
    public Map<String, Object> getPayload() {
        Map<String, Object> result = new HashMap<>();
        for (Settings value : Settings.values()) {
            try {
                @SuppressWarnings("rawtypes")
                ISetting setting = value.getSetting();
                result.put(setting.getName(), setting.getValue());
            } catch (IllegalArgumentException e) {
                Logger.error("No Setting: " + value.toString() + " : " + e);
            }
        }
        return result;
    }
}
