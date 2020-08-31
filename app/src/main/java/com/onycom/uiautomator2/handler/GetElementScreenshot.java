package com.onycom.uiautomator2.handler;

import android.graphics.Rect;

import androidx.test.uiautomator.UiObjectNotFoundException;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.common.exceptions.ElementNotFoundException;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.AndroidElement;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.Session;
import com.onycom.uiautomator2.utils.ScreenshotHelper;

public class GetElementScreenshot extends SafeRequestHandler {

    public GetElementScreenshot(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws UiObjectNotFoundException {
        String id = getElementId(request);
        Session session = AppiumUIA2Driver.getInstance().getSessionOrThrow();
        AndroidElement element = session.getKnownElements().getElementFromCache(id);
        if (element == null) {
            throw new ElementNotFoundException();
        }
        final Rect elementRect = element.getBounds();
        final String result = ScreenshotHelper.takeScreenshot(elementRect);
        return new AppiumResponse(getSessionId(request), result);
    }
}
