# ============================================
# Stage 1: Build Stage
# ============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

# Set working directory
WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime Stage
# ============================================
FROM tomcat:9.0-jdk17-openjdk-slim

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Create non-root user for security
RUN groupadd -r tomcat-user && \
    useradd -r -g tomcat-user -u 1001 tomcat-user && \
    chown -R tomcat-user:tomcat-user /usr/local/tomcat

# Copy WAR from builder stage
COPY --from=builder --chown=tomcat-user:tomcat-user /build/target/PEPs_back-0.1.war /usr/local/tomcat/webapps/ROOT.war

# Copy health check script
COPY --chown=tomcat-user:tomcat-user healthcheck.sh /usr/local/bin/healthcheck.sh
RUN chmod +x /usr/local/bin/healthcheck.sh

# Set environment variables
ENV CATALINA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Dorg.apache.catalina.startup.TldConfig.jarsToSkip=*.jar -Dorg.apache.catalina.startup.ContextConfig.jarsToSkip=*.jar"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /usr/local/bin/healthcheck.sh

# Switch to non-root user
USER tomcat-user

# Start Tomcat
CMD ["catalina.sh", "run"]