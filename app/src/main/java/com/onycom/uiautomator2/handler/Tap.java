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

import android.graphics.Rect;

import androidx.test.uiautomator.UiObjectNotFoundException;

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.common.exceptions.ElementNotFoundException;
import com.onycom.uiautomator2.common.exceptions.InvalidElementStateException;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.AndroidElement;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.Point;
import com.onycom.uiautomator2.model.Session;
import com.onycom.uiautomator2.model.api.TapModel;
import com.onycom.uiautomator2.utils.Device;
import com.onycom.uiautomator2.utils.PositionHelper;

import static com.onycom.uiautomator2.utils.Device.getUiDevice;
import static com.onycom.uiautomator2.utils.ModelUtils.toModel;

public class Tap extends SafeRequestHandler {

    public Tap(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws UiObjectNotFoundException {
        TapModel model = toModel(request, TapModel.class);
        final Point tapLocation;
        if (model.getUnifiedId() == null) {
            if (model.x == null || model.y == null) {
                throw new IllegalArgumentException(
                        "Both x and y tap coordinates must be set if element id is not provided");
            }
            tapLocation = PositionHelper.getDeviceAbsPos(new Point(model.x, model.y));
        } else {
            Session session = AppiumUIA2Driver.getInstance().getSessionOrThrow();
            AndroidElement element = session.getKnownElements()
                    .getElementFromCache(model.getUnifiedId());
            if (element == null) {
                throw new ElementNotFoundException();
            }
            Rect bounds = element.getBounds();
            Point offset = (model.x != null && model.y != null)
                ? new Point(model.x, model.y)
                : new Point(bounds.width() / 2, bounds.height() / 2);
            tapLocation = element.getAbsolutePosition(offset);
        }

        if (!getUiDevice().click(tapLocation.x.intValue(), tapLocation.y.intValue())) {
            throw new InvalidElementStateException(String.format("Tap at %s has failed", tapLocation));
        }
        Device.waitForIdle();
        return new AppiumResponse(getSessionId(request));
    }
}
