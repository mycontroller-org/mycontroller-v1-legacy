@REM
@REM Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
@REM and other contributors as indicated by the @author tags.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@ECHO OFF
SET HEAP_MIN=-Xms8m
SET HEAP_MAX=-Xmx100m

SET CONF_PROPERTIES_FILE=../conf/mycontroller.properties
SET CONF_LOG_FILE=../conf/logback.xml

@ECHO ON
java %HEAP_MIN% %HEAP_MAX% -Dlogback.configurationFile=%CONF_LOG_FILE% -Dmc.conf.file=%CONF_PROPERTIES_FILE% -cp "../lib/*" org.mycontroller.standalone.StartApp > ../logs/mycontroller_console.log 2>&1
