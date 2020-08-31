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

import com.onycom.uiautomator2.handler.request.SafeRequestHandler;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.ScreenOrientation;
import com.onycom.uiautomator2.model.api.RotationModel;
import com.onycom.uiautomator2.model.internal.CustomUiDevice;

import static com.onycom.uiautomator2.utils.ModelUtils.toModel;

public class SetRotation extends SafeRequestHandler {
    public SetRotation(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        RotationModel model = toModel(request, RotationModel.class);
        // `x` and `y` are ignored. We only care about `z`
        ScreenOrientation desired = CustomUiDevice.getInstance()
                .setOrientationSync(ScreenOrientation.ofDegrees(model.z));
        return new AppiumResponse(getSessionId(request), desired.toString());
    }
}
