package com.logicmonitor.tracing.apmtracingloadgen;

import com.google.common.io.Files;
import com.logicmonitor.tracing.apmtracingloadgen.apps.AppRestRepository;
import com.logicmonitor.tracing.apmtracingloadgen.apps.MockApplication;
import com.logicmonitor.tracing.apmtracingloadgen.config.ConfigurationRestRepository;
import com.logicmonitor.tracing.apmtracingloadgen.tasks.DummyTagsForTypeAhead;
import io.micrometer.core.instrument.util.StringUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Configuration
@EnableAutoConfiguration
@SpringBootApplication
@RequestMapping(path = "/apm")
public class ApmTracingLoadgenApplication {

    static Tracer tracer;
    private static Integer myPort = 0;
    private static String appName;
    private static String companyName = System.getenv().get("MANUFACTURE_COMPANY_NAME");

    static {
        if (StringUtils.isEmpty(companyName)) {
            System.getProperty("MANUFACTURE_COMPANY_NAME", "localdev");
        }

        AttributesBuilder attrBuilders = Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, appName)
                .put(ResourceAttributes.SERVICE_NAMESPACE, "punedev")
                .put("poweredby", "apmtracingloadgentool")
                .put(ResourceAttributes.HOST_NAME, System.getProperty("HOST_IP", "fppbar"));

        Resource serviceResource = Resource.create(attrBuilders.build());
        String exporter = Util.getOtelExporterEndpoint();
        System.out.println("EXPORTER ENDPOINT => " + exporter);
        OpenTelemetry openTelemetry = null;
        try {
            OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(exporter)
                    .build();

            SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                            .setScheduleDelay(100, TimeUnit.MILLISECONDS).build())
                    .setResource(serviceResource)
                    .build();

            openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider)
                    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();

        } catch (Exception e) {
            e.printStackTrace();
            SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                    .build();
            openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider)
                    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();
        }
        tracer = openTelemetry.getTracer(System.getProperty("host.name", "localhost") + "-instrumentation");

    }

    @Autowired
    AppRestRepository appRestRepository;
    @Autowired
    DummyTagsForTypeAhead dummyTagsForTypeAhead;
    @Autowired
    ConfigurationRestRepository configurationRestRepository;

    public static void main(String[] args) {
        SpringApplication.run(ApmTracingLoadgenApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        appName = event.getApplicationContext().getDisplayName();
        myPort = event.getWebServer().getPort();
    }


    @RequestMapping("{appName}/apmloadgen/{operationName}")
    public @ResponseBody
    ResponseEntity<String> getRandom(HttpServletRequest request, @RequestParam(name = "companyName") String companyName, @PathVariable(name = "derivedFrom", required = false) String mockAppName, @PathVariable(name = "operationName") String operationName, @RequestParam(name = "links") String links) {
        System.out.println("My Port => " + myPort);
        List<MockApplication> mimicDbOperation = appRestRepository.findByName(appName);    // dummy db operation
        if (tracer != null) {
            Span span = tracer.spanBuilder(mockAppName + "_" + operationName + "_" + myPort).startSpan();
            Boolean isChildSpan = "true".equals(request.getParameter("childop"));
            try (Scope scope = span.makeCurrent()) {
                try {
                    span.setAttribute("appName", appName);
                    span.setAttribute("http.method", "GET");
                    span.setAttribute("parent tag", isChildSpan ? "NO" : "YES");
                    span.setAttribute("child-tag", isChildSpan ? "YES" : "NO");
                    span.setAttribute("duplicate tag", "tags with space");
                    span.setAttribute("tag", "hyphen-seperated-data");
                    span.setAttribute("sign tag", "test_with_underscore");
                    span.setAttribute("operationName", "-name-operation");
                    span.setAttribute("serviceName", "name-generator-service");
                    span.setAttribute("numeric root tag", "1");
                    span.setAttribute("tracing.loadgen.company.name", companyName);
                    span.setAttribute("numeric sibling tag", "0");
                    span.setAttribute("numeric negative", "-100");
                    span.setAttribute("Operation", "Dummy Topology Linkage");
                    span.setAttribute("spanType", "Original");
                    span.setAttribute("status.code", "202");

                    span.setAttribute("test numeric static-auto_version", "1.12");
                    span.setAttribute("test numeric static-code", "1.13");
                    span.setAttribute("os.type", ManagementFactory.getRuntimeMXBean().getSystemProperties().get("os.name"));

                    span.setAttribute("process.pid", "1");
                    span.setAttribute("process.runtime.name", "OpenJDK Runtime Environment");
                    span.setAttribute("process.runtime.description", ManagementFactory.getRuntimeMXBean().getVmVersion());
                    span.setAttribute("process.uptime", ManagementFactory.getRuntimeMXBean().getUptime());

                    // type aheads
                    List<String> dummyTags = dummyTagsForTypeAhead.getDummyTagsForTypeAhead();

                    for (String typeaheadTagDummy : dummyTags) {
                        System.out.println(typeaheadTagDummy);
                        span.setAttribute(typeaheadTagDummy, "thisIsADummyTagFixedValue1kForPSR");
                    }

                    Map<String, String> urls = new HashMap<>();

                    for (String link : links.split(",")) {
                        urls.put(link, "http://" + link.trim().toLowerCase() + ".punedev-localdev" + "/apm/apmloadgen/" + Util.getRandomOperationName() + "?ischildoperation=true&derivedFrom=" + mockAppName + "&companyName=" + companyName + "&request=" + UUID.randomUUID().toString());
                        urls.put(link, "http://" + link.trim().toLowerCase() + ".punedev-localdev/actuator/health");

                        if (Boolean.parseBoolean(System.getProperty("use_db_operation_within_childtraces", "false"))) {
                            List<MockApplication> application = appRestRepository.findByName(link);    // dummy db operation
                        }
                    }
                    // messing up to topology connections for spans to be visible randomly
                    runFewChildSpan(companyName, urls, span);
                    Thread.sleep(100);
                } catch (Exception g) {
                    span.setStatus(StatusCode.ERROR, "error calling internal APIs");
                }
                return ResponseEntity.ok("{\"message\":\"Hello from " + mockAppName + " - " + String.valueOf(ThreadLocalRandom.current().nextInt(10, 2000)) + "\"}");
            } catch (Exception e) {
                span.setStatus(StatusCode.ERROR, "Error in calling service root call demo_x");
            } finally {
                span.end();
            }
        }
        return ResponseEntity.ok("tracing setup error");
    }

    public Span runFewChildSpan(String companyName, Map<String, String> urls, Span span) {
        int count = 1;
        Span childSpan = tracer.spanBuilder(companyName + "_childd_" + span.getSpanContext().getTraceId()).startSpan();
        for (String url : urls.keySet()) {
            childSpan.setAttribute("linkage", appName + " => (" + (++count) + ") " + url);
            childSpan = tracer.spanBuilder(companyName + "-" + span.getSpanContext().getTraceId() + "_" + "child_" + url.lastIndexOf("/"))
                    .setParent(Context.current().with(span))
                    .startSpan();
            try {
                childSpan.setStatus(StatusCode.OK);
                // self loop topology hiding
                RestTemplate restTemplate = new RestTemplate();
                String output = restTemplate.getForObject(url, String.class);
            } catch (Exception g) {
                childSpan.setStatus(StatusCode.ERROR, "error calling internal APIs");
            } finally {
                childSpan.end();
            }
        }
        return childSpan;
    }
}