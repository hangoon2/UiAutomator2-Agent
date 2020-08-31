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

package com.onycom.uiautomator2.utils.w3c;

import android.view.MotionEvent;

public class MotionInputEventParams extends InputEventParams {
    public MotionEvent.PointerProperties properties;
    public MotionEvent.PointerCoords coordinates;
    public int actionCode;
    public int button;

    public MotionInputEventParams(long startDelta, int actionCode, MotionEvent.PointerCoords coordinates,
                                  int button, MotionEvent.PointerProperties properties) {
        super();
        this.startDelta = startDelta;
        this.actionCode = actionCode;
        this.coordinates = coordinates;
        this.button = button;
        this.properties = properties;
    }

    @Override
    public String toString() {
        return String.format("%s; startDelta=%s; actionCode=%s; coordinates=%s; button=%s; properties=%s",
                super.toString(), startDelta, actionCode, coordinates, button, properties);
    }
}