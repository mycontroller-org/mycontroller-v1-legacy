#!/bin/bash

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
