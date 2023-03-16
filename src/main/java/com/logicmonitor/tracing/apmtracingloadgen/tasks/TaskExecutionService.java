package com.logicmonitor.tracing.apmtracingloadgen.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Service
public class TaskExecutionService {

    @Autowired
    StartGeneratingLoadTask startGeneratingLoadTask;

    @Autowired
    ManufactureMockApplicationsTask manufactureMockApplicationsTask;


    public TaskExecutionService() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    ForkJoinPool.commonPool().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws Exception {
        boolean manufacturePodsFlag = Boolean.parseBoolean(System.getProperty("lmapmloadge.manufmockapps.skip", "false"));
        long delay = 5000L;

        Thread.sleep(4000L);

        if (!manufacturePodsFlag) {
            ForkJoinPool.commonPool().execute(manufactureMockApplicationsTask);
            delay = 10000L;
        } else {
            System.out.println("MANUFACTURING MOCK APP IN KUBERNETES SKIPPING");
        }
        Thread.sleep(delay);

        if (!Boolean.parseBoolean(System.getProperty("lmapmloadge.load.skip", "true"))) {
            System.out.println("Starting loadgen now");
            ForkJoinPool.commonPool().execute(startGeneratingLoadTask);
        } else {
            System.out.println("LOADGEN SKIPPING");
        }
    }
}
