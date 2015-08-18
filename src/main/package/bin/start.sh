#!/bin/bash
#
# Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


#Java Heap settings
HEAP_MIN=-Xms8m
HEAP_MAX=-Xmx40m

JAVA_VERSION="1.7"
MC_VERSION="0.0.1"

#configuration file location
CONF_PROPERTIES_FILE=../conf/mycontroller.properties
CONF_LOG_FILE=../conf/logback.xml

if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    echo "java is not installed in our machine"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "java version: $version"
    if [[ "$version" > "$JAVA_VERSION" ]]; then
        MC_PID=`ps -ef | grep mycontroller-standalone | grep -v grep | awk '{ print $2 }'`
        if [ ! -z "$MC_PID" ]
        then
          echo "Mycontroller.org server is already running on pid[${MC_PID}]"
        else
          java ${HEAP_MIN} ${HEAP_MAX} -Dlogback.configurationFile=${CONF_LOG_FILE} -Dmc.conf.file=${CONF_PROPERTIES_FILE} -jar ../lib/mycontroller-standalone-${MC_VERSION}-single.jar > ../logs/mycontroller.log 2>&1 &
          echo 'Start issued for Mycontroller'
        fi
    else         
      echo "Mycontroller.org server required java version $JAVA_VERSION or later"
    fi
fi
