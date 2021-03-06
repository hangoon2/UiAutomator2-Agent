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

package com.onycom.uiautomator2.model.settings;

import com.onycom.uiautomator2.model.AppiumUIA2Driver;

public class ShouldUseCompactResponses extends AbstractSetting<Boolean> {

    private static final String SETTING_NAME = "shouldUseCompactResponses";

    public ShouldUseCompactResponses() {
        super(Boolean.class, SETTING_NAME);
    }

    @Override
    public Boolean getValue() {
        return AppiumUIA2Driver
                .getInstance()
                .getSessionOrThrow()
                .shouldUseCompactResponses();
    }

    @Override
    protected void apply(Boolean shouldUseCompactResponses) {
        AppiumUIA2Driver
                .getInstance()
                .getSessionOrThrow()
                .setCapability(SETTING_NAME, shouldUseCompactResponses);
    }

}
