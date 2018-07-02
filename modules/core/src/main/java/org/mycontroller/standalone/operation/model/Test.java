/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.operation.model;

import java.nio.charset.StandardCharsets;

public class Test {

    public static void main(String[] args) {
        String textRaw = "Rule definition: test temperature rule \n"
                + "Condition: Threshold [ if {[G]:test >> [N]:1:node-1 >> [S]:21:tempsensor >> [SV]:Temperature} >= 20 ]\n"
                + "Present value: 31 °C\n"
                + "Triggered at: Jul 02, 2018 12:10:01 PM IST\n"
                + "--- www.mycontroller.org";
        String text = new String(textRaw.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        System.out.println(text);
    }

}
