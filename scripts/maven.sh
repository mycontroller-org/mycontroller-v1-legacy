#!/bin/bash
#
# Copyright 2015-2021 Jeeva Kandasamy (jkandasa@gmail.com)
# and other contributors as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# include local jar files to maven location
# These jar files were removed from the remote location
# compiled it from the source and included on the mentioned path
mvn install:install-file \
  -Dfile=./jars/moquette-broker-0.10.jar \
  -DgroupId=io.moquette \
  -DartifactId=moquette-broker \
  -Dversion=0.10 \
  -Dpackaging=jar

mvn install:install-file \
  -Dfile=./jars/moquette-mapdb-storage-0.10.jar \
  -DgroupId=io.moquette \
  -DartifactId=moquette-mapdb-storage \
  -Dversion=0.10 \
  -Dpackaging=jar

# check licenses
mvn clean
mvn verify
mvn package -Dmaven.test.skip=true