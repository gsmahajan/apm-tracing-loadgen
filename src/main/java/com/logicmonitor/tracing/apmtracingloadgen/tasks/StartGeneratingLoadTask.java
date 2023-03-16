package com.logicmonitor.tracing.apmtracingloadgen.tasks;

import com.logicmonitor.tracing.apmtracingloadgen.KubernetesOperatorService;
import com.logicmonitor.tracing.apmtracingloadgen.Util;
import com.logicmonitor.tracing.apmtracingloadgen.apps.AppRestRepository;
import com.logicmonitor.tracing.apmtracingloadgen.apps.MockApplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class StartGeneratingLoadTask implements Runnable {
    private static Logger log = LoggerFactory.getLogger(StartGeneratingLoadTask.class);

    private static boolean breakLoop = false;

    private static String companyName = System.getenv("OTEL_COMPANY_NAME");

    @Autowired
    AppRestRepository appRestRepository;

    @Autowired
    KubernetesOperatorService kubernetesService;

    @Override
    public void run() {
        while (true) {
            if (Files.exists(Paths.get(System.getProperty("breakLoopFilePath", "break.txt")))) {
                System.out.println("Break-loop file found on disk, stopping the loadgen " + Instant.now());
                breakLoop = true;
            }

            if (!breakLoop) {
                // List<String> servicesEnrolledList = kubernetesService.getServiceNamesInNamespace(System.getProperty("lmplatform_namespace", "punedev"));
                // if (servicesEnrolledList != null) {
                //     servicesEnrolledList.stream().forEach(service -> {
                List<MockApplication> appList = appRestRepository.findAll(); //appRestRepository.findByName(service);
                Collections.shuffle(appList);
                appList.stream().limit(3).forEach(service -> {  //FIXME girish - limit
                    String appName = service.getName();
                    try {
                        String root = Util.getRandomOperationName();
                        String url = "http://" + appName + ".punedev-localdev/apm/" + appName + "/apmloadgen/" + root + "?childop=false&derivedFrom=" + appName + "&companyName=" + companyName + "&requestId=" + UUID.randomUUID().toString();
                        int connects = Integer.parseInt(service.getLinks());
                        Request request = new Request.Builder()
                                .url(url + (url.contains("?") ? "&" : "?") + "date=" + Instant.now().toEpochMilli() + "&links=" + appList.stream()
                                        .limit(appList.size() < connects ? appList.size() - 1 : connects).filter(s -> !s.getName().equals(appName))
                                        .map(MockApplication::getName).collect(Collectors.joining(",")))
                                .build();
                        System.out.println("rootapp calling => " + request.url().url());

                        makeHttpCall(request);
                        Thread.sleep(1000);
//                  //FIXME enable this later on so program terminates in 90 minutes by itself.
//                        long awaitProgramTermination = Long.parseLong(System.getenv().getOrDefault("STOP_PROGRAM_AFTER_NB_MILLIS", "5400000"));
//                        if (ManagementFactory.getRuntimeMXBean().getUptime() > awaitProgramTermination) {
//                            System.out.println("Its been an one and half hour since loadgen started, stopping now");
//                            Files.createFile(Paths.get("break.txt"));
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void makeHttpCall(Request request) {
        try {
            HttpLoggingInterceptor debug = new HttpLoggingInterceptor(message -> log.info(message));
            debug.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(5, TimeUnit.SECONDS).addInterceptor(debug).build();

            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response) throws IOException {
                    System.out.println(response.body());
                }

                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
