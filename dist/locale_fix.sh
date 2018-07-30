#!/bin/bash
#
# Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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

#refer http://docs.transifex.com/formats/java-properties/
# remove all commented-out lines
find ./ -name mc_locale_java_*.properties -exec sed -i -e 's/^# //g' {} \;
echo "core java locale files commented-out lines removed.";
