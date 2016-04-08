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

import org.easyrules.api.Rule;
import org.easyrules.api.RuleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McRuleListener implements RuleListener {
    protected static final Logger _logger = LoggerFactory.getLogger(McRuleListener.class);

    @Override
    public void beforeExecute(Rule rule) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("About to execute the rule:{}", rule);
        }
    }

    @Override
    public void onFailure(Rule rule, Exception exception) {
        _logger.error("Rule execution failed! rule:{}", rule, exception);

    }

    @Override
    public void onSuccess(Rule rule) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Rule executed successfully:{}", rule);
        }
    }

}
