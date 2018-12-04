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
package org.mycontroller.standalone.settings;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.jobs.MidNightJobs;
import org.mycontroller.standalone.jobs.ResourcesLogsAggregationJob;
import org.mycontroller.standalone.jobs.RuleDefinitionsReEnableJob;
import org.mycontroller.standalone.metrics.jobs.MetricsAggregationJob;
import org.mycontroller.standalone.model.SystemJob;
import org.mycontroller.standalone.rule.McRuleEngine;
import org.quartz.triggers.CronExpression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.4.0
 */
@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SystemJobsSettings {
    public static final String KEY_SYSTEM_JOBS = "myControllerSystemJobs";
    public static final String SKEY_METRICS_AGGREGATION = "metricsAggregationJob";
    public static final String SKEY_RESOURCES_LOGS_AGGREGATION = "resourcesLogsAggregationJob";
    public static final String SKEY_DAILY_ONCE = "dailyOnceJob";
    public static final String SKEY_RULE_DEFINITION_ENGINE = "ruleDefinitionEngineJob";
    public static final String SKEY_RULE_DEFINITION_RE_ENABLE = "ruleDefinitionReEnableJob";

    private String metricsAggregation;
    private String resourcesLogsAggregation;
    private String dailyOnce;
    private String ruleDefinitionEngine;
    private String ruleDefinitionReEnable;

    public static SystemJobsSettings get() {
        return SystemJobsSettings
                .builder()
                .metricsAggregation(getValue(SKEY_METRICS_AGGREGATION, "05 * * * * ? *"))
                .resourcesLogsAggregation(getValue(SKEY_RESOURCES_LOGS_AGGREGATION, "45 * * * * ? *"))
                .dailyOnce(getValue(SKEY_DAILY_ONCE, "30 3 0 * * ? *"))
                .ruleDefinitionEngine(getValue(SKEY_RULE_DEFINITION_ENGINE, "*/5 * * * * ? *"))
                .ruleDefinitionReEnable(getValue(SKEY_RULE_DEFINITION_RE_ENABLE, "10,40 * * * * ? *"))
                .build();
    }

    public static List<SystemJob> listAllJobs() {
        ArrayList<SystemJob> jobs = new ArrayList<SystemJob>();
        SystemJobsSettings jobsSettings = SystemJobsSettings.get();

        // update aggregation job
        jobs.add(getSystemJob(1, true, "Metrics aggregate job", jobsSettings.getMetricsAggregation(),
                MetricsAggregationJob.class.getName()));

        // update ResourcesLogs aggregation job
        jobs.add(getSystemJob(2, true, "ResourcesLogs aggregation job", jobsSettings.getResourcesLogsAggregation(),
                ResourcesLogsAggregationJob.class.getName()));

        // update Daily once job
        jobs.add(getSystemJob(3, true, "Daily once job", jobsSettings.getDailyOnce(),
                MidNightJobs.class.getName()));

        // update Rule definition engine
        jobs.add(getSystemJob(4, true, "Rule definition engine job", jobsSettings.getRuleDefinitionEngine(),
                McRuleEngine.class.getName()));

        // update Rule definition re-enable job
        jobs.add(getSystemJob(5, true, "Rule definition re-enable job", jobsSettings.getRuleDefinitionReEnable(),
                RuleDefinitionsReEnableJob.class.getName()));

        return jobs;
    }

    private static SystemJob getSystemJob(Integer id, boolean isEnabled, String name, String cronExpression,
            String className) {
        return SystemJob.builder()
                .id(id)
                .enabled(isEnabled)
                .name(name)
                .cron(cronExpression)
                .className(className)
                .build();
    }

    public void save() throws ParseException {
        if (metricsAggregation != null) {
            validateExpression(metricsAggregation);
            updateValue(SKEY_METRICS_AGGREGATION, metricsAggregation);
        }
        if (resourcesLogsAggregation != null) {
            validateExpression(resourcesLogsAggregation);
            updateValue(SKEY_RESOURCES_LOGS_AGGREGATION, resourcesLogsAggregation);
        }
        if (dailyOnce != null) {
            validateExpression(dailyOnce);
            updateValue(SKEY_DAILY_ONCE, dailyOnce);
        }
        if (ruleDefinitionEngine != null) {
            validateExpression(ruleDefinitionEngine);
            updateValue(SKEY_RULE_DEFINITION_ENGINE, ruleDefinitionEngine);
        }
        if (ruleDefinitionReEnable != null) {
            validateExpression(ruleDefinitionReEnable);
            updateValue(SKEY_RULE_DEFINITION_RE_ENABLE, ruleDefinitionReEnable);
        }
    }

    private void validateExpression(String cronExpression) throws ParseException {
        CronExpression.validateExpression(cronExpression);
    }

    private static String getValue(String subKey, String defaultValue) {
        String _value = SettingsUtils.getValue(KEY_SYSTEM_JOBS, subKey);
        if (_value == null) {
            return defaultValue;
        }
        return _value;
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_SYSTEM_JOBS, subKey, value);
    }
}