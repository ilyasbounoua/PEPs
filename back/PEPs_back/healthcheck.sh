#!/bin/sh
# Simplified Health check for Spring + Tomcat
# Checks if the server is responding to requests on root
# IMPORTANT: This file MUST have LF line endings to work in Docker/Linux

if curl -f -s -X OPTIONS http://localhost:8080/ > /dev/null 2>&1; then
    exit 0
else
    exit 1
fi
