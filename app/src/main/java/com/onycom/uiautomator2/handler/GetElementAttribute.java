package com.onycom.uiautomator2.handler;

import androidx.test.uiautomator.UiObjectNotFoundException;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.common.exceptions.ElementNotFoundException;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.AndroidElement;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.Session;

public class GetElementAttribute extends SafeRequestHandler {

    public GetElementAttribute(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws UiObjectNotFoundException {
        String id = getElementId(request);
        String attributeName = getNameAttribute(request);
        Session session = AppiumUIA2Driver.getInstance().getSessionOrThrow();
        AndroidElement element = session.getKnownElements().getElementFromCache(id);
        if (element == null) {
            throw new ElementNotFoundException();
        }
        String attribute = element.getAttribute(attributeName);
        return new AppiumResponse(getSessionId(request), attribute);
    }

}
