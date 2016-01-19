/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.api.jaxrs.mapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SensorsGuiButton {
    private ButtonStatus graph = new ButtonStatus();
    private ButtonStatus onOff = new ButtonStatus();
    private ButtonStatus armed = new ButtonStatus();
    private ButtonStatus tripped = new ButtonStatus();
    private ButtonStatus increaseDecrease = new ButtonStatus();
    private ButtonStatus hvacFlowState = new ButtonStatus();
    private ButtonStatus hvacSpeed = new ButtonStatus();
    private ButtonStatus hvacFlowMode = new ButtonStatus();
    private ButtonStatus lockStatus = new ButtonStatus();
    private ButtonStatus cover = new ButtonStatus();
    private ButtonStatus rgb = new ButtonStatus();
    private ButtonStatus rgbw = new ButtonStatus();

    public ButtonStatus getOnOff() {
        return onOff;
    }

    public ButtonStatus getArmed() {
        return armed;
    }

    public ButtonStatus getTripped() {
        return tripped;
    }

    public ButtonStatus getIncreaseDecrease() {
        return increaseDecrease;
    }

    public ButtonStatus getHvacFlowState() {
        return hvacFlowState;
    }

    public ButtonStatus getHvacSpeed() {
        return hvacSpeed;
    }

    public ButtonStatus getHvacFlowMode() {
        return hvacFlowMode;
    }

    public ButtonStatus getLockStatus() {
        return lockStatus;
    }

    public ButtonStatus getCover() {
        return cover;
    }

    public ButtonStatus getGraph() {
        return graph;
    }

    public ButtonStatus getRgb() {
        return rgb;
    }

    public ButtonStatus getRgbw() {
        return rgbw;
    }
}
