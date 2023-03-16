package com.logicmonitor.tracing.apmtracingloadgen;

import com.logicmonitor.tracing.apmtracingloadgen.config.ConfigurationRestRepository;
import com.logicmonitor.tracing.apmtracingloadgen.config.LoadGenToolConfiguration;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerBuilder;
import io.kubernetes.client.openapi.models.V1ContainerPortBuilder;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentBuilder;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarBuilder;
import io.kubernetes.client.openapi.models.V1EnvVarSourceBuilder;
import io.kubernetes.client.openapi.models.V1ExecActionBuilder;
import io.kubernetes.client.openapi.models.V1HandlerBuilder;
import io.kubernetes.client.openapi.models.V1LabelSelectorBuilder;
import io.kubernetes.client.openapi.models.V1Lifecycle;
import io.kubernetes.client.openapi.models.V1LifecycleBuilder;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceBuilder;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1ObjectFieldSelectorBuilder;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodSpecBuilder;
import io.kubernetes.client.openapi.models.V1PodTemplateSpecBuilder;
import io.kubernetes.client.openapi.models.V1ResourceRequirementsBuilder;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceBuilder;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePortBuilder;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1ServiceSpecBuilder;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetBuilder;
import io.kubernetes.client.openapi.models.V1StatefulSetSpecBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class KubernetesOperatorService {

    private static Logger log = LoggerFactory.getLogger(KubernetesOperatorService.class);
    ApiClient client;

    @Autowired
    ConfigurationRestRepository configurationRestRepository;

    public KubernetesOperatorService() {
        try {
            client = Config.defaultClient();

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> log.info(message));
            interceptor.setLevel(log.isDebugEnabled() ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
            OkHttpClient newClient = client.getHttpClient()
                    .newBuilder()
                    .addInterceptor(interceptor)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .build();
            client.setHttpClient(newClient);
            Configuration.setDefaultApiClient(client);

            //           ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
            //           forkJoinPool.execute(CreateJavaDockerImageTask);
            //           forkJoinPool.execute(WipeOldMockPodsTask);
            //           forkJoinPool.execute(MakeNewMockPodsTask);

        } catch (IOException e) {
            System.err.println("Error while creating Kubernetes APIClient");
            e.printStackTrace();
        }
    }

    public String createApplication(String companyName, String ns, String appName, String port, String image) {
        try {
            createDeployment(companyName, ns, appName, port, image);
            createService(ns, appName, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }


    public String createDeployment(String companyName, String ns, String appName, String port, String image) throws Exception {
        final String[] result = new String[]{"nok"};

        //Map<String, Quantity> limits = new HashMap<>();
        //limits.put("cpu", Quantity.fromString("0.2"));

        V1PodSpec v1PodSpec = new V1PodSpecBuilder().withContainers(Util.scanImageNameLegacy(image) ?
                        getContainerNameService(companyName, ns, appName, port, image) :
                        getContainerDefaultByImage(companyName, ns, appName, port, image))
                .withRestartPolicy("Always").build();

        V1Deployment v1Deployment = new V1DeploymentBuilder()
                .withApiVersion("apps/v1")
                .withKind("Deployment")
                .withNewMetadata()
                .withName(appName)
                .addToLabels("name", appName)
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withNewSelector()
                .withMatchLabels(Collections.singletonMap("name", appName))
                .endSelector()
                .withNewTemplate()
                .withNewMetadata().addToLabels("name", appName).endMetadata()
                .withNewSpecLike(v1PodSpec).endSpec().endTemplate().and().build();

        System.out.println("============== DEPLOYMENT " + appName + " ================");
        AppsV1Api appsV1Api = new AppsV1Api(client);
        System.out.println("============ yaml " + appName + "  ==============");
        System.out.println(Yaml.dump(v1Deployment));
        Files.deleteIfExists(Paths.get("/tmp/foo/" + appName + "-deployment.yaml"));
        Yaml.dump(v1Deployment, new FileWriter("/tmp/foo/" + appName + "-deployment.yaml"));

        appsV1Api.createNamespacedDeploymentAsync(ns, v1Deployment, "true", null, null, new ApiCallback<V1Deployment>() {
            @Override
            public void onFailure(ApiException e, int i, Map<String, List<String>> map) {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(V1Deployment v1Deployment, int i, Map<String, List<String>> map) {
                result[0] = "ok";
            }

            @Override
            public void onUploadProgress(long l, long l1, boolean b) {

            }

            @Override
            public void onDownloadProgress(long l, long l1, boolean b) {

            }
        });
        return result[0];
    }

    private V1Container getContainerNameService(String companyName, String ns, String appName, String port, String image) {
        Map<String, Quantity> limits = new HashMap<>();
        limits.put("memory", Quantity.fromString("128Mi"));
        limits.put("cpu", Quantity.fromString("500m"));

        Map<String, Quantity> requests = new HashMap<>();
        requests.put("memory", Quantity.fromString("96Mi"));
        requests.put("cpu", Quantity.fromString("250m"));


        return new V1ContainerBuilder()

                //   .withLivenessProbe(new V1ProbeBuilder().withHttpGet(new V1HTTPGetActionBuilder().withPath("/actuator/health/liveness").withPort(new IntOrString(80)).withScheme("HTTP")
                //           .withHttpHeaders(Arrays.asList(new V1HTTPHeaderBuilder().withName("X-Calling-By").withValue("aws-eks-kubernetes").build())).build()).build())
                .withName(appName).withImage(image).withImagePullPolicy("IfNotPresent")
                .withCommand("java")
                //.withCommand(Arrays.asList("cat /sys/fs/cgroup/memory/memory.limit_in_bytes", "sysctl -a", "cat /Dockerfile", "ls -l", "java -XX:+PrintFlagsFinal -version", "env", "cat /proc/version", "cat /proc/cpuinfo", "cat /proc/meminfo", "java"))
                .withArgs(Arrays.asList("-server", "-javaagent:opentelemetry-javaagent.jar", "-XX:MaxRAMPercentage=75.0", "-XX:+UseSerialGC", "-Xss256k", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=heapdumpoom.dump",
                        "-XX:MaxRAM=64m", "-Xmx64m", "-Xms32m", "-Djava.net.preferIPv4Stack=true", "-Djava.net.debug=false", "-Dotel.javaagent.debug=false",
                        "-Dserver.port=80 ", "-Dio.netty.tryReflectionSetAccessible=true ", "-Dotel.exporter.otlp.endpoint=http://apmcollector.punedev-localdev:4317", "-javaagent:opentelemetry-javaagent.jar", "-jar", image.contains("app.jar") ? "/app.jar" :
                                (image.contains("name-generator-service") ? "name-generator-service.jar" : "name-service.jar")))
                .withEnv(Util.getOtelEnvironmentList(companyName, appName, ns, port))
                .withPorts(Arrays.asList(new V1ContainerPortBuilder()
                        .withContainerPort(80)
                        .withProtocol("TCP").build()))
                .withLifecycle(getContainerLifecycle())
                .withResources(new V1ResourceRequirementsBuilder().withLimits(limits).withRequests(requests).build())
                .build();
    }

    private V1Lifecycle getContainerLifecycle() {
        return new V1LifecycleBuilder()
                .withPreStop(new V1HandlerBuilder(new V1HandlerBuilder()
                        .withExec(new V1ExecActionBuilder()
                                .withCommand(Arrays.asList("/bin/sh", "-c", "[ -f /heapdumpoom.dump ] && mv heapdumpoom.dump heapdumpoom-$(date +%d_%h_%Y_%H_%M_%S).dump && aws s3 cp heapdumpoom*.dump s3://lm-traces-v1/"))
                                .build())
                        .build())
                        .build())
                .build();
    }

    private V1Container getContainerDefaultByImage(String companyName, String ns, String appName, String port, String image) {
        return new V1ContainerBuilder()
                .withName(appName).withImage(image).withImagePullPolicy("IfNotPresent")
                .withEnv(Util.getOtelEnvironmentList(companyName, appName, ns, port))
                .withPorts(Arrays.asList(new V1ContainerPortBuilder().withContainerPort(80)
                        .withProtocol("TCP").build())).build();
    }

//    public void createIngress(String namespace, List<String> apps, String suffix) throws Exception {
//        NetworkingV1Api api = new NetworkingV1Api(client);
//
//        List<V1IngressRule> rules = new ArrayList<>(apps.size());
//        V1IngressRule rule = null;
//        for (String app : apps) {
//            rule = new V1IngressRuleBuilder().withHost(app + suffix).withHttp(new V1HTTPIngressRuleValueBuilder().withPaths(new V1HTTPIngressPathBuilder().withPathType("Prefix").withPath("/")
//                    .withBackend(new V1IngressBackendBuilder().withService(new V1IngressServiceBackendBuilder().withName(app).withPort(new V1ServiceBackendPortBuilder().withNumber(80).build()).build()).build()).build()).build()).build();
//            rules.add(rule);
//        }
//        V1Ingress ingress = new V1IngressBuilder().withApiVersion("networking.k8s.io/v1").withKind("Ingress").withNewMetadata().withName("name-virtual-host-ingress").endMetadata().withNewSpec().withRules(rules).endSpec().build();
//        Yaml.dump(ingress, new FileWriter("/tmp/foo/ingress.yaml"));
//
//        api.createNamespacedIngress(namespace, ingress, "true", null, null);
//    }

    public String createOTELService(String ns, String appName) {
        try {
            final String[] result = new String[]{"nok"};

            Map<String, String> selector = new HashMap<>();
            selector.put("name", appName);

            Map<String, String> labels = new HashMap<>();
            labels.put("name", appName);

            CoreV1Api api = new CoreV1Api(client);
            V1ServiceSpec spec = new V1ServiceSpecBuilder().withType("ClusterIP").withPorts(Arrays.asList(
                            new V1ServicePortBuilder().withName("grpc").withProtocol("TCP").withPort(4317).withNewTargetPort(4317).build(),
                            new V1ServicePortBuilder().withName("healthcheck").withProtocol("TCP").withPort(13133).withNewTargetPort(13133).build(),
                            new V1ServicePortBuilder().withName("http").withProtocol("TCP").withPort(4318).withNewTargetPort(4318).build(),
                            new V1ServicePortBuilder().withName("pprof").withProtocol("TCP").withPort(1777).withNewTargetPort(1777).build()))
                    .withSelector(selector).build();    // port ??
            V1Service service = new V1ServiceBuilder().withApiVersion("v1").withKind("Service").withNewMetadata().withLabels(labels).withName(appName).withNewCreationTimestamp(System.currentTimeMillis()).endMetadata().withSpec(spec).build();

            System.out.println("============== OTEL-SERVICE " + appName + " ================");

            Files.deleteIfExists(Paths.get("/tmp/foo/" + "collector-" + appName + "-service.yaml"));
            Yaml.dump(service, new FileWriter("/tmp/foo/" + "collector-" + appName + "-service.yaml"));

            api.createNamespacedServiceAsync(ns, service, "true", null, null, new ApiCallback<V1Service>() {
                @Override
                public void onFailure(ApiException e, int i, Map<String, List<String>> map) {
                    e.printStackTrace();
                    e.getCause().printStackTrace();
                }

                @Override
                public void onSuccess(V1Service v1Service, int i, Map<String, List<String>> map) {
                    result[0] = "ok";
                }

                @Override
                public void onUploadProgress(long l, long l1, boolean b) {

                }

                @Override
                public void onDownloadProgress(long l, long l1, boolean b) {

                }
            });
            return result[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createService(String ns, String appName, String port) {
        try {
            final String[] result = new String[]{"nok"};

            Map<String, String> selector = new HashMap<>();
            selector.put("name", appName);

            Map<String, String> labels = new HashMap<>();
            labels.put("name", appName);

            CoreV1Api api = new CoreV1Api(client);
            V1ServiceSpec spec = new V1ServiceSpecBuilder().withType("ClusterIP").withPorts(new V1ServicePortBuilder().withName(port).withProtocol("TCP").withPort(80).withNewTargetPort(80).build()).withSelector(selector).build();    // port ??
            V1Service service = new V1ServiceBuilder().withApiVersion("v1").withKind("Service").withNewMetadata().withLabels(labels).withName(appName).withNewCreationTimestamp(System.currentTimeMillis()).endMetadata().withSpec(spec).build();

            System.out.println("============== SERVICE " + appName + " ================");

            Files.deleteIfExists(Paths.get("/tmp/foo/" + appName + "-service.yaml"));
            Yaml.dump(service, new FileWriter("/tmp/foo/" + appName + "-service.yaml"));

            api.createNamespacedServiceAsync(ns, service, "true", null, null, new ApiCallback<V1Service>() {
                @Override
                public void onFailure(ApiException e, int i, Map<String, List<String>> map) {
                    e.printStackTrace();
                    e.getCause().printStackTrace();
                }

                @Override
                public void onSuccess(V1Service v1Service, int i, Map<String, List<String>> map) {
                    result[0] = "ok";
                }

                @Override
                public void onUploadProgress(long l, long l1, boolean b) {

                }

                @Override
                public void onDownloadProgress(long l, long l1, boolean b) {

                }
            });
            return result[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public String createServiceByYamlResource(String ns, String appName, String port) {
//        // Load Service YAML manifest into object
//        try {
//            File file = new File("/Users/girishmahajan/Downloads/apm-tracing-loadgen/src/main/resources/service_template.yaml");
//            V1Service yamlSvc = (V1Service) Yaml.load(file);
//
//            CoreV1Api api = new CoreV1Api(client);
//            V1Service createResult = null;
//
//            System.out.println("============== SERVICE " + appName + " ================");
//            createResult = api.createNamespacedService(ns, yamlSvc, "true", null, null);
//            return createResult.getStatus().toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public List<String> getServiceNamesInNamespace(String namespace) throws ApiException {

        CoreV1Api api = new CoreV1Api(client);
        V1ServiceList serviceList = api.listNamespacedService(namespace == null ? "default" : namespace, "false", false, null, null, null, -1, null, null, 60, false);
        return serviceList.getItems().stream().map(V1Service::getMetadata).collect(Collectors.toList()).stream().map(V1ObjectMeta::getName).collect(Collectors.toList());
    }

    public boolean deleteAllKubernetesResources(String kubenamespace, boolean isCleanSlateFully, String... appList) {
        CoreV1Api api = new CoreV1Api(client);
        try {
            V1NodeList nodeList = api.listNode("false", false, null, null, null, 1000, null, null, 10, false);
            nodeList.getItems()
                    .stream()
                    .forEach((node) -> System.out.println(node));

            if (isCleanSlateFully) {
                cleanUpNamespacedPods(api, kubenamespace);
                return true;
            } else {
                //skip
                log.info("skipping delete by purpose");
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createStatefulSetForClientOtel(String namespace, String companyName) throws IOException, ApiException {

        if (CollectionUtils.isEmpty(configurationRestRepository.findByName("default"))) {
            LoadGenToolConfiguration configuration = new LoadGenToolConfiguration();
            configurationRestRepository.save(configuration);
        }

        String nbCollector = System.getenv().get("OTEL_COLLECTORS_TO_SPIN");
        if (StringUtils.isEmpty(nbCollector)) {
            nbCollector = System.getProperty("OTEL_COLLECTORS_TO_SPIN", "1");
        }

        Map<String, String> labels = new HashMap<>();
        labels.put("app", companyName);

        if (Integer.parseInt(nbCollector) > 0) {
            V1StatefulSet statefulSet = new V1StatefulSetBuilder()
                    .withApiVersion("apps/v1")
                    .withKind("StatefulSet")
                    .withMetadata(new V1ObjectMetaBuilder()
                            .withName(labels.get("app"))
                            .withNamespace(namespace)
                            .withLabels(labels).build())
                    .withSpec(new V1StatefulSetSpecBuilder()
                            .withPodManagementPolicy("OrderedReady")
                            .withReplicas(Integer.parseInt(nbCollector))
                            .withSelector(new V1LabelSelectorBuilder().withMatchLabels(labels).build())
                            .withTemplate(new V1PodTemplateSpecBuilder()
                                    .withSpec(new V1PodSpecBuilder()
                                            .withContainers(new V1ContainerBuilder()
                                                    .withImagePullPolicy("IfNotPresent")
                                                    .withName(labels.get("app"))
                                                    .withImage("logicmonitor/lmotel:latest")
                                                    .withEnv(getOTELEnv(labels.get("app"), namespace)).build()).withDnsPolicy("ClusterFirst").withRestartPolicy("Always").withSchedulerName("default-scheduler").withTerminationGracePeriodSeconds(30L).withTerminationGracePeriodSeconds(30L).build())
                                    .withMetadata(new V1ObjectMetaBuilder().withLabels(labels).build())
                                    .build())
                            .build())
                    .build();

            System.out.println("============== STEATEFULSET - LMAPMOTEL " + companyName + " ================");
            AppsV1Api appsV1Api = new AppsV1Api(client);
            System.out.println("============ yaml " + companyName + "  ==============");
            System.out.println(Yaml.dump(statefulSet));
            Files.deleteIfExists(Paths.get("/tmp/foo/" + companyName + "-statefulset.yaml"));
            Yaml.dump(statefulSet, new FileWriter("/tmp/foo/" + companyName + "-statefulset.yaml"));

            appsV1Api.createNamespacedStatefulSet(namespace, statefulSet, "true", null, null);
        }
    }

    public List<V1EnvVar> getOTELEnv(String otelName, String namespace) {

        LoadGenToolConfiguration configuration = configurationRestRepository.findByName("default").get(0);

        // (; only for tracing pipeline - priority is for the SYS env rather the one coming from config or db or json ;)
        String _accountName = System.getenv().getOrDefault("LOGICMONITOR_ACCOUNT", configuration.getAccountName());
        String _bearerToken = System.getenv().getOrDefault("LOGICMONITOR_BEARER_TOKEN", configuration.getBearerToken());

        return Arrays.asList(new V1EnvVarBuilder().withName("LOGICMONITOR_ACCOUNT").withValue(_accountName).build()
                , new V1EnvVarBuilder().withName("LOGICMONITOR_BEARER_TOKEN").withValue(_bearerToken).build()
                , new V1EnvVarBuilder().withName("LOGICMONITOR_ACCESS_ID").withValue(configuration.getAccessId()).build()
                , new V1EnvVarBuilder().withName("LOGICMONITOR_ACCESS_KEY").withValue(configuration.getAccessKey()).build()
                , new V1EnvVarBuilder().withName("LOGICMONITOR_OTEL_NAME").withValue(otelName).build()
                , new V1EnvVarBuilder().withName("LOGICMONITOR_OTEL_CREATED_AT").withValue(Instant.now().toString()).build()
                , new V1EnvVarBuilder().withName("LOGICMONITOR_OTEL_TENANT").withValue(otelName + "." + namespace + "." + _accountName).build() // FIXME Note that for route =>  *.qauattraces01 redirects to =>  qauattraces01.logicmonitor.com
                , new V1EnvVarBuilder().withName("LOGICMONITOR_OTEL_NAMESPACE").withValue(namespace).build()
                , new V1EnvVarBuilder().withName("OTEL_EXPORTER_OTLP_ENDPOINT").withValue(Util.getOtelExporterEndpoint()).build()
                , new V1EnvVarBuilder().withName("OTEL_LOG_LEVEL").withValue(configuration.getOtelRunInDebug() ? "debug" : "info").build()
                , new V1EnvVarBuilder().withName("HOST_IP").withValueFrom(new V1EnvVarSourceBuilder().withFieldRef(new V1ObjectFieldSelectorBuilder().withFieldPath("status.podIP").build()).build()).build());
    }

    public String createNamespace(String namespace) throws IOException, ApiException {
        CoreV1Api api = new CoreV1Api(client);
        V1Namespace kubens = api.createNamespace(new V1NamespaceBuilder().withKind("Namespace").withApiVersion("v1").withNewMetadata().withName(namespace).withClusterName(Util.DEFAULT_CLUSTER_NAME).endMetadata().build(), "true", null, null);
        return kubens.getStatus().toString();
    }

    private CompletableFuture<Void> cleanUpNamespacedPods(CoreV1Api coreV1Api, String namespace) {
        return CompletableFuture.runAsync(() -> {
            final V1DeleteOptions options = new V1DeleteOptions();
            options.setGracePeriodSeconds(0L);
            log.info("Starting clean up of namespaced pods for namespace {}", namespace);
            try {
                // coreV1Api.deleteCollectionNamespacedPod(namespace, "false", null, null, null, 3, null, 1000, false, null, null, null, 30000, options);
                coreV1Api.deleteCollectionNamespacedPodAsync(namespace, "false", null, null, null, 0, null, 1000, false, null, null, null, 10, options, null);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        });
    }
}