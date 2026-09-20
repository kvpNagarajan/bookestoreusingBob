# ============================================================
# Stage 1 — Build with Maven + JDK 21 (Red Hat UBI)
# ============================================================
FROM registry.access.redhat.com/ubi9/openjdk-21:latest AS builder

USER root
WORKDIR /build

# Install gzip + tar (needed to extract Maven tarball) and Maven 3.9.8
ENV MAVEN_VERSION=3.9.8
RUN microdnf install -y gzip tar --nodocs --setopt=install_weak_deps=0 && \
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar xz -C /opt \
    && ln -sf /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn \
    && microdnf clean all

# Copy pom first — download dependencies as a cached layer
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests)
COPY src/ src/
RUN mvn clean package -DskipTests -q

# ============================================================
# Stage 2 — Runtime (minimal Red Hat UBI JRE 21)
# Non-root, principle of least privilege
# ============================================================
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest

# UID 1001 is the default non-root user in UBI runtime images
USER 1001
WORKDIR /app

COPY --from=builder /build/target/ecom-backend-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD curl -sf http://127.0.0.1:8080/actuator/health 2>/dev/null || \
      curl -sf http://127.0.0.1:8080/v3/api-docs > /dev/null || exit 1

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
