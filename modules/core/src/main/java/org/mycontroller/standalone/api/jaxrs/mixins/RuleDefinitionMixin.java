/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DATA_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.OPERATOR;
import org.mycontroller.standalone.rule.RuleUtils.STRING_OPERATOR;
import org.mycontroller.standalone.rule.model.Dampening;
import org.mycontroller.standalone.rule.model.DampeningActiveTime;
import org.mycontroller.standalone.rule.model.DampeningConsecutive;
import org.mycontroller.standalone.rule.model.DampeningLastNEvaluations;
import org.mycontroller.standalone.rule.model.DampeningNone;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.rule.model.RuleDefinitionCompare;
import org.mycontroller.standalone.rule.model.RuleDefinitionScript;
import org.mycontroller.standalone.rule.model.RuleDefinitionState;
import org.mycontroller.standalone.rule.model.RuleDefinitionString;
import org.mycontroller.standalone.rule.model.RuleDefinitionThreshold;
import org.mycontroller.standalone.rule.model.RuleDefinitionThresholdRange;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@JsonSerialize(using = RuleDefinitionSerializer.class)
@JsonDeserialize(using = RuleDefinitionDeserializer.class)
abstract class RuleDefinitionMixin {

}

class RuleDefinitionSerializer extends JsonSerializer<RuleDefinitionTable> {
    @Override
    public void serialize(RuleDefinitionTable ruleDefinitionTable, JsonGenerator jgen, SerializerProvider provider)
            throws IOException,
            JsonProcessingException {
        if (ruleDefinitionTable != null) {
            RestUtils.getObjectMapper().writeValue(jgen, RuleUtils.getRuleDefinition(ruleDefinitionTable));
        } else {
            jgen.writeNull();
        }

    }
}

class RuleDefinitionDeserializer extends JsonDeserializer<RuleDefinition> {

    @Override
    public RuleDefinition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        CONDITION_TYPE conditionType = CONDITION_TYPE.fromString(node.get("conditionType").asText());
        DAMPENING_TYPE dampeningType = DAMPENING_TYPE.fromString(node.get("dampeningType").asText());

        RuleDefinition ruleDefinition = null;
        switch (conditionType) {
            case THRESHOLD:
                RuleDefinitionThreshold ruleDefinitionThreshold = new RuleDefinitionThreshold();
                ruleDefinitionThreshold.setOperator(OPERATOR.fromString(node.get("operator").asText()));
                ruleDefinitionThreshold.setDataType(DATA_TYPE.fromString(node.get("dataType").asText()));
                ruleDefinitionThreshold.setData(node.get("data").asText());
                ruleDefinition = ruleDefinitionThreshold;
                break;
            case THRESHOLD_RANGE:
                RuleDefinitionThresholdRange thresholdRange = new RuleDefinitionThresholdRange();
                thresholdRange.setInRange(node.get("inRange").booleanValue());
                thresholdRange.setIncludeOperatorLow(node.get("includeOperatorLow").asBoolean());
                thresholdRange.setIncludeOperatorHigh(node.get("includeOperatorHigh").asBoolean());
                thresholdRange.setThresholdLow(node.get("thresholdLow").asDouble());
                thresholdRange.setThresholdHigh(node.get("thresholdHigh").asDouble());
                ruleDefinition = thresholdRange;
                break;
            case COMPARE:
                RuleDefinitionCompare definitionCompare = new RuleDefinitionCompare();
                definitionCompare.setOperator(OPERATOR.fromString(node.get("operator").asText()));
                definitionCompare.setData2ResourceType(
                        RESOURCE_TYPE.fromString(node.get("data2ResourceType").asText()));
                definitionCompare.setData2ResourceId(node.get("data2ResourceId").asInt());
                definitionCompare.setData2Multiplier(node.get("data2Multiplier").asDouble());
                ruleDefinition = definitionCompare;
                break;
            case STRING:
                RuleDefinitionString ruleDefinitionString = new RuleDefinitionString();
                ruleDefinitionString.setOperator(STRING_OPERATOR.fromString(node.get("operator").asText()));
                ruleDefinitionString.setPattern(node.get("pattern").asText());
                ruleDefinitionString.setIgnoreCase(node.get("ignoreCase").asBoolean());
                ruleDefinition = ruleDefinitionString;
                break;
            case STATE:
                RuleDefinitionState ruleDefinitionState = new RuleDefinitionState();
                ruleDefinitionState.setOperator(OPERATOR.fromString(node.get("operator").asText()));
                ruleDefinitionState.setState(STATE.fromString(node.get("state").asText()));
                ruleDefinition = ruleDefinitionState;
                break;
            case SCRIPT:
                RuleDefinitionScript ruleDefinitionScript = new RuleDefinitionScript();
                ruleDefinitionScript.setScriptFile(node.get("scriptFile").asText());
                if (node.get("scriptBindings") != null) {
                    ruleDefinitionScript.setScriptBindings(RestUtils.getObjectMapper().convertValue(
                            node.get("scriptBindings"), new TypeReference<HashMap<String, Object>>() {
                            }));
                }
                ruleDefinition = ruleDefinitionScript;
                break;
            default:

                break;
        }
        //Update RuleDefinition details
        if (node.get("id") != null) {
            ruleDefinition.setId(node.get("id").asInt());
        }
        ruleDefinition.setEnabled(node.get("enabled").asBoolean());
        ruleDefinition.setDisableWhenTrigger(node.get("disableWhenTrigger").asBoolean());
        if (node.get("reEnable") != null && node.get("reEnable").asBoolean()) {
            ruleDefinition.setReEnable(true);
            ruleDefinition.setReEnableDelay(node.get("reEnableDelay").asLong());
        } else {
            ruleDefinition.setReEnable(false);
            ruleDefinition.setReEnableDelay(null);
        }
        ruleDefinition.setName(node.get("name").asText());
        ruleDefinition.setResourceType(RESOURCE_TYPE.fromString(node.get("resourceType").asText()));
        if (conditionType != CONDITION_TYPE.SCRIPT) {
            ruleDefinition.setResourceId(node.get("resourceId").asInt());
        } else {
            //For resource script file name will be reference. keep resourceId as -1
            ruleDefinition.setResourceId(-1);
        }
        ruleDefinition.setConditionType(conditionType);
        ruleDefinition.setIgnoreDuplicate(node.get("ignoreDuplicate").asBoolean());
        //ruleDefinition.setTriggered(node.get("triggered").booleanValue());
        List<Integer> operationIds = new ArrayList<>();
        if (node.get("operationIds") != null) {
            if (node.get("operationIds").isArray()) {
                for (final JsonNode nodeOperationId : node.get("operationIds")) {
                    operationIds.add(nodeOperationId.asInt());
                }
            }
        }
        ruleDefinition.setOperationIds(operationIds);
        ruleDefinition.setDampeningType(DAMPENING_TYPE.fromString(node.get("dampeningType").asText()));

        Dampening dampening = null;

        JsonNode dampeningNode = node.get("dampening");

        switch (dampeningType) {
            case CONSECUTIVE:
                DampeningConsecutive dampeningConsecutive = new DampeningConsecutive();
                dampeningConsecutive.setConsecutiveMax(dampeningNode.get("consecutiveMax").asInt());
                dampening = dampeningConsecutive;
                break;
            case ACTIVE_TIME:
                DampeningActiveTime dampeningActiveTime = new DampeningActiveTime();
                dampeningActiveTime.setActiveTime(dampeningNode.get("activeTime").asLong());
                dampening = dampeningActiveTime;
                break;
            case LAST_N_EVALUATIONS:
                DampeningLastNEvaluations lastNEvaluations = new DampeningLastNEvaluations();
                lastNEvaluations.setOccurrencesMax(dampeningNode.get("occurrencesMax").asInt());
                lastNEvaluations.setEvaluationsMax(dampeningNode.get("evaluationsMax").asInt());
                dampening = lastNEvaluations;
                break;
            case NONE:
                DampeningNone dampeningNone = new DampeningNone();
                dampening = dampeningNone;
            default:
                break;
        }
        if (dampening != null) {
            dampening.setType(dampeningType);
            ruleDefinition.setDampening(dampening);
        }
        return ruleDefinition;
    }
}