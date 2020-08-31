package com.onycom.uiautomator2.handler;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;

import java.util.List;
import java.util.UUID;

import com.onycom.uiautomator2.common.exceptions.ElementNotFoundException;
import com.onycom.uiautomator2.common.exceptions.UiAutomator2Exception;
import com.onycom.uiautomator2.core.AccessibilityNodeInfoGetter;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.AndroidElement;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.Session;
import com.onycom.uiautomator2.utils.Logger;

import static com.onycom.uiautomator2.utils.Device.getAndroidElement;

/**
 * This method return first visible element inside provided element
 */
public class FirstVisibleView extends SafeRequestHandler {

    public FirstVisibleView(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws UiObjectNotFoundException {
        Logger.info("Get first visible element inside provided element");
        String elementId = getElementId(request);
        Session session = AppiumUIA2Driver.getInstance().getSessionOrThrow();

        AndroidElement element = session.getKnownElements().getElementFromCache(elementId);
        if (element == null) {
            throw new ElementNotFoundException();
        }
        Object firstObject = null;
        if (element.getUiObject() instanceof UiObject) {
            UiObject uiObject = (UiObject) element.getUiObject();
            Logger.debug("Container for first visible is a uiobject; looping through children");
            for (int i = 0; i < uiObject.getChildCount(); i++) {
                UiObject object = uiObject.getChild(new UiSelector().index(i));
                if (object.exists()) {
                    firstObject = object;
                    break;
                }
            }
        } else {
            UiObject2 uiObject = (UiObject2) element.getUiObject();
            Logger.debug("Container for first visible is a uiobject2; looping through children");
            List<UiObject2> childObjects = uiObject.getChildren();
            if (childObjects.isEmpty()) {
                throw new UiObjectNotFoundException("Could not get children for container object");
            }
            for (UiObject2 childObject : childObjects) {
                try {
                    if (AccessibilityNodeInfoGetter.fromUiObject(childObject) != null) {
                        firstObject = childObject;
                        break;
                    }
                } catch (UiAutomator2Exception ignored) {
                }
            }
        }

        if (firstObject == null) {
            throw new ElementNotFoundException();
        }

        String id = UUID.randomUUID().toString();
        AndroidElement androidElement = getAndroidElement(id, firstObject, false);
        session.getKnownElements().add(androidElement);
        return new AppiumResponse(getSessionId(request), androidElement.toModel());
    }
}
