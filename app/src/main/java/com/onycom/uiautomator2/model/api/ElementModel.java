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

package com.onycom.uiautomator2.model.api;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.onycom.uiautomator2.utils.StringHelpers;
import com.onycom.uiautomator2.utils.w3c.ElementConstants;

import java.util.HashMap;
import java.util.Map;

import com.onycom.uiautomator2.model.AndroidElement;

public class ElementModel extends BaseModel {
    @SerializedName(value = ElementConstants.JWP_ELEMENT_ID_KEY_NAME, alternate = {"element"})
    public String jwpElementId;
    @SerializedName(ElementConstants.W3C_ELEMENT_ID_KEY_NAME)
    public String w3cElementId;

    public ElementModel() {}

    public ElementModel(AndroidElement source) {
        this.jwpElementId = source.getId();
        this.w3cElementId = source.getId();
    }

    public ElementModel(Map<String, Object> source) {
        this.jwpElementId = source.get(ElementConstants.JWP_ELEMENT_ID_KEY_NAME) instanceof String
                ? (String) source.get(ElementConstants.JWP_ELEMENT_ID_KEY_NAME)
                : null;
        this.w3cElementId = source.get(ElementConstants.W3C_ELEMENT_ID_KEY_NAME) instanceof String
                ? (String) source.get(ElementConstants.W3C_ELEMENT_ID_KEY_NAME)
                : null;
    }

    @Nullable
    public String getUnifiedId() {
        return StringHelpers.isBlank(w3cElementId) ? jwpElementId : w3cElementId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put(ElementConstants.JWP_ELEMENT_ID_KEY_NAME, jwpElementId);
        result.put(ElementConstants.W3C_ELEMENT_ID_KEY_NAME, w3cElementId);
        return result;
    }
}
