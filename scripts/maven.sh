#!/bin/bash

# check licenses
mvn verify
mvn package -Dmaven.test.skip=true