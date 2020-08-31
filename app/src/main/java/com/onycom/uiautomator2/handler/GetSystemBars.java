package com.onycom.uiautomator2.handler;

import android.app.Instrumentation;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.api.SystemBarsModel;
import com.onycom.uiautomator2.utils.Logger;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class GetSystemBars extends SafeRequestHandler {

    public GetSystemBars(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        Logger.info("Get status bar height of the device");

        Instrumentation instrumentation = getInstrumentation();

        int height = getStatusBarHeight(instrumentation);
        return new AppiumResponse(getSessionId(request), new SystemBarsModel(height));
    }

    private int getStatusBarHeight(Instrumentation instrumentation) {
        int result = 0;
        int resourceId = instrumentation.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = instrumentation.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
