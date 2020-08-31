/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onycom.uiautomator2.handler;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;

import java.util.Arrays;
import java.util.List;

import com.onycom.uiautomator2.common.exceptions.ElementNotFoundException;
import com.onycom.uiautomator2.common.exceptions.InvalidArgumentException;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.AndroidElement;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.By;
import com.onycom.uiautomator2.model.KnownElements;
import com.onycom.uiautomator2.model.api.ScrollToModel;
import com.onycom.uiautomator2.model.internal.NativeAndroidBySelector;
import com.onycom.uiautomator2.utils.Logger;

import static com.onycom.uiautomator2.model.internal.NativeAndroidBySelector.SELECTOR_ACCESSIBILITY_ID;
import static com.onycom.uiautomator2.model.internal.NativeAndroidBySelector.SELECTOR_ANDROID_UIAUTOMATOR;
import static com.onycom.uiautomator2.model.internal.NativeAndroidBySelector.SELECTOR_CLASS;
import static com.onycom.uiautomator2.utils.Device.scrollToElement;
import static com.onycom.uiautomator2.utils.ElementLocationHelpers.toSelector;
import static com.onycom.uiautomator2.utils.ModelUtils.toModel;

public class ScrollTo extends SafeRequestHandler {
    private static final List<String> SUPPORTED_STRATEGIES =
            Arrays.asList(SELECTOR_ACCESSIBILITY_ID, SELECTOR_CLASS, SELECTOR_ANDROID_UIAUTOMATOR);

    public ScrollTo(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws UiObjectNotFoundException {
        ScrollToModel model = toModel(request, ScrollToModel.class);
        String strategy = model.params.strategy;
        String selector = model.params.selector;
        By by = new NativeAndroidBySelector().pickFrom(strategy, selector);
        final UiSelector uiselector;
        if (by instanceof By.ByAccessibilityId) {
            uiselector = new UiSelector().description(selector);
        } else if (by instanceof By.ByClass) {
            uiselector = new UiSelector().className(selector);
        } else if (by instanceof By.ByAndroidUiAutomator) {
            uiselector = toSelector(by.getElementLocator());
        } else {
            throw new InvalidArgumentException(
                    String.format("Unsupported strategy: '%s'. The only supported strategies are: %s.",
                            strategy, SUPPORTED_STRATEGIES));
        }

        UiScrollable origin = null;
        if (model.origin != null) {
            KnownElements ke = AppiumUIA2Driver.getInstance().getSessionOrThrow().getKnownElements();
            AndroidElement element = ke.getElementFromCache(model.origin.getUnifiedId());
            if (element == null) {
                throw new ElementNotFoundException();
            }
            if (!(element.getUiObject() instanceof UiObject)) {
                throw new IllegalArgumentException("The given origin element must be a valid scrollable UiObject");
            }
            origin = new UiScrollable(((UiObject) element.getUiObject()).getSelector());
        }
        scrollToElement(origin, uiselector, model.params.maxSwipes);

        Logger.info(String.format("Scrolled via strategy: '%s' and selector '%s'.", strategy, selector));

        return new AppiumResponse(getSessionId(request));
    }
}
