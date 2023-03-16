#! /bin/bash

java -javaagent:opentelemetry-javaagent-all.jar -Dotel.javaagent.debug=true -Dlmapmloadge.manufmockapps.skip=true -Dotel.metrics.exporter=none -Dotel.traces.exporter=otlp -Dotel.resource.attributes=host.name=loadgenrootapp,service.name=rootapp,service.namespace=default,ip=192.168.13.233 -Dotel.exporter.otlp.insecure=true -Dotel.exporter.otlp.endpoint=http://collector:4317 -jar -Dserver.port=80 /app.jar

