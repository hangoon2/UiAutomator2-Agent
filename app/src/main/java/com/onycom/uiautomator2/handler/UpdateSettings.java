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

import java.util.Map;
import java.util.Map.Entry;

import com.onycom.uiautomator2.common.exceptions.UnsupportedSettingException;
import com.onycom.uiautomator2.http.AppiumResponse;
import com.onycom.uiautomator2.http.IHttpRequest;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.Session;
import com.onycom.uiautomator2.model.api.SettingsModel;
import com.onycom.uiautomator2.model.settings.ISetting;
import com.onycom.uiautomator2.model.settings.Settings;
import com.onycom.uiautomator2.utils.Logger;

import static com.onycom.uiautomator2.utils.ModelUtils.toModel;

public class UpdateSettings extends SafeRequestHandler {

    public UpdateSettings(String mappedUri) {
        super(mappedUri);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) {
        Session session = AppiumUIA2Driver.getInstance().getSessionOrThrow();
        SettingsModel model = toModel(request, SettingsModel.class);
        Map<String, Object> settings = model.settings;
        Logger.debug("Update settings: " + settings.toString());
        for (Entry<String, Object> entry : settings.entrySet()) {
            String settingName = entry.getKey();
            Object settingValue = entry.getValue();
            ISetting setting = getSetting(settingName);
            setting.update(settingValue);
            session.setCapability(settingName, settingValue);
        }
        return new AppiumResponse(getSessionId(request));
    }

    @SuppressWarnings("rawtypes")
    public ISetting getSetting(String settingName) throws UnsupportedSettingException {
        for (final Settings value : Settings.values()) {
            if (value.toString().equals(settingName)) {
                return value.getSetting();
            }
        }
        throw new UnsupportedSettingException(settingName, Settings.names());
    }
}
