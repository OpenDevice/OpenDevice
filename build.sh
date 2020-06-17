#!/bin/bash
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
mvn -o clean package -P standalone -DskipTests
