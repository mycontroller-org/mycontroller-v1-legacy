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
package org.mycontroller.standalone.rule;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.model.RuleDefinitionScript;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McConditionScript extends McRuleBase {
    public RuleDefinitionScript rdScript; //data to operate on

    public McConditionScript(RuleDefinitionTable ruleDefinitionTable) {
        rdScript = new RuleDefinitionScript();
        rdScript.updateRuleDefinition(ruleDefinitionTable);
        setRuleDefinitionBase(rdScript);
    }

    public McConditionScript(RuleDefinitionScript rdScript) {
        this.rdScript = rdScript;
        setRuleDefinitionBase(this.rdScript);
    }

    @Override
    public boolean evaluate() {
        Boolean triggerOperation = false;

        //execute script
        try {
            File scriptFile = FileUtils.getFile(
                    AppProperties.getInstance().getScriptLocation() + rdScript.getScriptFile());
            McScript mcScript = McScript.builder()
                    .name(scriptFile.getCanonicalPath())
                    .extension(FilenameUtils.getExtension(scriptFile.getCanonicalPath()))
                    .build();
            McScriptEngine mcScriptEngine = new McScriptEngine(mcScript);
            Object result = mcScriptEngine.executeScript();
            triggerOperation = McUtils.getBoolean(result);
            if (triggerOperation == null) {
                _logger.warn("Looks like script does not return result! {}", mcScript);
            }
        } catch (Exception ex) {
            _logger.error("Exception occurred. This rule will be disabled. Fix this error and enable this rule{}",
                    rdScript, ex);
            //Disable this rule, when there is an exception on script
            rdScript.setEnabled(false);
        }

        _logger.debug("Rule evaluate result:{}", triggerOperation);
        return executeDampening(triggerOperation);
    }
}
