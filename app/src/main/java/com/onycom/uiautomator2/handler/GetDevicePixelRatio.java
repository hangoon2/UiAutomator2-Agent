package com.onycom.uiautomator2.handler;

import android.app.Instrumentation;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.utils.Logger;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class GetDevicePixelRatio extends SafeRequestHandler {

    public GetDevicePixelRatio(String mappedUri) {
        super(mappedUri);
    }

    private static float getDeviceScaleRatio(Instrumentation instrumentation) {
        WindowManager windowManager = (WindowManager) instrumentation.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        Logger.info("Get device pixel ratio");
        Instrumentation instrumentation = getInstrumentation();
        Float ratio = getDeviceScaleRatio(instrumentation);
        return new AppiumResponse(getSessionId(request), ratio);
    }
}