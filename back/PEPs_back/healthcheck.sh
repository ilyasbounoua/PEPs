#!/bin/sh
# Health check script for backend service

# Check if Tomcat is responding
if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Backend is healthy"
    exit 0
else
    # Fallback: check if Tomcat is at least running
    if curl -f -s http://localhost:8080/ > /dev/null 2>&1; then
        echo "Backend is responding"
        exit 0
    else
        echo "Backend is not responding"
        exit 1
    fi
fi
