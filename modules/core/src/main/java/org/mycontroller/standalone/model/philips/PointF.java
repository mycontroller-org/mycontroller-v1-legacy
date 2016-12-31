/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.model.philips;

/**
 * Decompiled from Philips Hue SDK resources, to be able to run without the jar dependency.
 */
public class PointF {
    public float x;
    public float y;

    public PointF() {
    }

    public PointF(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public final void set(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public final void set(final PointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public final void negate() {
        this.x = (-this.x);
        this.y = (-this.y);
    }

    public final void offset(final float dx, final float dy) {
        this.x += dx;
        this.y += dy;
    }

    public final boolean equals(final float x, final float y) {
        return ((this.x == x) && (this.y == y));
    }
}