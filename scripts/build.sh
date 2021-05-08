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


# build images
podman run --rm --name jdk \
  --volume $PWD:/source \
  --workdir /source \
  docker.io/library/maven:3.6.3-openjdk-8 ./scripts/maven.sh

# container registry
# REGISTRY='quay.io/mycontroller-org'
# IMAGE_NAME="${REGISTRY}/mycontroller"
# IMAGE_TAG=`git rev-parse --abbrev-ref HEAD`
# 
# # debug lines
# echo $PWD
# ls -alh
# git branch
# 
# # build image
# docker buildx build \
#   --push \
#   --progress=plain \
#   --platform linux/arm/v6,linux/arm/v7,linux/arm64,linux/amd64 \
#   --file docker/Dockerfile \
#   --tag ${IMAGE_NAME}:${IMAGE_TAG} .
