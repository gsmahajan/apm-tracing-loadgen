FROM openjdk:8-jre-slim
#FROM amazoncorretto:11

WORKDIR /

COPY "target/apm-tracing-loadgen-0.0.1-SNAPSHOT.jar" "/app.jar"

RUN apt-get update

# RUN apt-get -y install curl

# RUN curl -LO "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.23.0/opentelemetry-javaagent.jar" > opentelemetry-javaagent-all.jar

COPY opentelemetry-javaagent.jar /

ENV CATALINA_OPTS="-Xms64m -Xmx72m"

ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://host.docker.internal:4317"

ENV STOP_PROGRAM_AFTER_NB_MILLIS="5400000";

CMD [ "java", "-javaagent:opentelemetry-javaagent.jar" , "-server", "-XX:MaxRAMPercentage=75.0", "-XX:+UseSerialGC", "-Xss256k", "-XX:MaxRAM=64m", "-Xmx64m", "-Xms32m", "-Djava.net.preferIPv4Stack=true", "-Djava.net.debug=false", "-Dotel.javaagent.debug=false", "-Dserver.port=80", "-Dlmapmloadge.load.skip=false" , "-Dotel.exporter.otlp.endpoint=http://host.docker.internal:4317", "-Dlmapmloadge.manufmockapps.skip=true" , "-Dio.netty.tryReflectionSetAccessible=true" , "-Dotel.metrics.exporter=none" , "-Dotel.traces.exporter=otlp" , "-Dotel.resource.attributes=host.name=loadgenrootapp,service.name=rootapp,service.namespace=punedev,ip=192.168.13.233" , "-Dotel.exporter.otlp.insecure=true" , "-jar" , "-Dserver.port=80" , "/app.jar" ]
