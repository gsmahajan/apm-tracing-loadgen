package com.logicmonitor.tracing.apmtracingloadgen;

import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarBuilder;
import io.micrometer.core.instrument.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Util {

    public static final String DEFAULT_CLUSTER_NAME = "lmapmloadgen";
    private static final String DEFAULT_NAMESPACE_NAME = "punedev";

    private static Set<Integer> portSet = new HashSet<>();
    private static List<String> operationsNames = Arrays.asList("doThis", "doThat", "deleteThis", "deleteThat", "updateThis", "updateThat",
            "getThis", "getThat", "fetchThis", "fetchThat", "compressThis", "compressThat", "pickpThis", "pickupThat", "removeThis", "removeThat",
            "lookThis", "lookThat", "handleThis", "handleThat", "notThis", "notThat", "shipThis", "shipThat", "callBack");

    public static String getRandomOperationName() {
        return operationsNames.get(ThreadLocalRandom.current().nextInt(operationsNames.size()));
    }

    public static String getNamespace(String companyName) {
        return DEFAULT_NAMESPACE_NAME + "-" + companyName.trim();
    }

    public static final String getOtelExporterEndpoint() {
        // priority to the environemnt first
        String exporter = System.getenv().get("OTEL_EXPORTER_OTLP_ENDPOINT");

        // if not found then the jvm properties otherwise will use the default kubernetes service endpoint
        if (StringUtils.isEmpty(exporter)) {
            exporter = System.getProperty("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317");
        }
        System.out.println("EXPORTER ENDPOINT => " + exporter);
        return exporter;
    }

    public static List<V1EnvVar> getOtelEnvironmentList(String companyName, String appName, String appNamespace, String port) {
        List<V1EnvVar> envList = new ArrayList<>();

        //FIXME - pod ip change
        envList.add(new V1EnvVarBuilder().withName("OTEL_RESOURCE_ATTRIBUTES").withValue(getResourceAttributes(companyName, appName, appNamespace, port, 5)).build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_METRICS_EXPORTER").withValue("otlp").build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_TRACES_EXPORTER").withValue("otlp").build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_LOGS_EXPORTER").withValue("otlp").build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_METRICS_EXEMPLAR_FILTER").withValue("all").build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_EXPORTER_OTLP_INSECURE").withValue("true").build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_SERVICE_NAME").withValue(appName).build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_EXPORTER_OTLP_PROTOCOL").withValue("grpc").build());

        envList.add(new V1EnvVarBuilder().withName("OTEL_COMPANY_NAME").withValue(companyName).build());
        envList.add(new V1EnvVarBuilder().withName("PAUSE_POD_LOGS_TO_REDUCE_SIZE_DEV_PSR").withValue("true").build());
        envList.add(new V1EnvVarBuilder().withName("SERVER_PORT").withValue("80").build());
        envList.add(new V1EnvVarBuilder().withName("OTEL_EXPORTER_OTLP_ENDPOINT").withValue(getOtelExporterEndpoint()).build());
        envList.add(new V1EnvVarBuilder().withName("SERVICE_NAME").withValue(appName).build());
        envList.add(new V1EnvVarBuilder().withName("SERVICE_GROUP").withValue("managed.services.punedev-" + companyName).build());
        try {
            envList.add(new V1EnvVarBuilder().withName("HOST_NAME").withValue(java.net.InetAddress.getLocalHost().getHostName()).build());
        } catch (UnknownHostException e) {
            envList.add(new V1EnvVarBuilder().withName("HOST_NAME").withValue(appName + "." + companyName + "-punedevtracing.logicmonitor.com").build());
        }
        envList.add(new V1EnvVarBuilder().withName("RUN_AT").withValue(Instant.now().toString()).build());

        return envList;
    }

    public static String getResourceAttributes(String companyName, String appName, String appNamespace, String port, Integer nbDatacenter) {
        String tempVar = String.valueOf(ThreadLocalRandom.current().nextInt(1, nbDatacenter));
        String datacenterName = System.getenv().getOrDefault("DCNAME", "dc" + tempVar + "_" + appNamespace);
        return String.format("host.name=%s,telemetry.sdk.language=%s,telemetry.sdk.version=%s,service.name=%s,source-version=%s,datacenter=%s,service.port=%s,service.namespace=%s,ip=%s,traces.namespace=%s",
                appName + "." + companyName + "-punedevtracing.logicmonitor.com",
                "java",
                "v1.18.0", //FIXME we need sdk to deliver version being use here,
                appName,
                ManagementFactory.getRuntimeMXBean().getVmVersion(),
                datacenterName,
                port,
                appNamespace + (appName.contains("generator") ? "_root" : ""),
                "192." + tempVar + ".49." + getUniqueIpEnd(), appNamespace);
    }

    private static Integer getUniqueIpEnd() {
        int res = -1;
        while (true) {
            res = ThreadLocalRandom.current().nextInt(1, 255);
            if (!portSet.contains(res)) {
                portSet.add(res);
                break;
            }
        }
        return res;
    }


    public static boolean scanImageNameLegacy(String image) {
        return image.contains("/name-service")
                || image.contains("/name-generator-service");
    }
}
