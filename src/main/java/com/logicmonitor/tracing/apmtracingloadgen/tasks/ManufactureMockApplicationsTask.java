package com.logicmonitor.tracing.apmtracingloadgen.tasks;

import com.logicmonitor.tracing.apmtracingloadgen.KubernetesOperatorService;
import com.logicmonitor.tracing.apmtracingloadgen.Util;
import com.logicmonitor.tracing.apmtracingloadgen.apps.AppRestRepository;
import com.logicmonitor.tracing.apmtracingloadgen.apps.MockApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

@Component
public class ManufactureMockApplicationsTask extends ForkJoinTask<Boolean> {
    private static String companyName = System.getProperty("MANUFACTURE_COMPANY_NAME", "localdev");

    static {
        System.out.println("============================ WELCOME ==============================");
        System.out.println("companyName=" + companyName);
        System.out.println("===================================================================");

    }

    @Autowired
    KubernetesOperatorService kubernetesOperatorService;
    @Autowired
    AppRestRepository appRestRepository;
    private Boolean result = Boolean.FALSE;

    public ManufactureMockApplicationsTask() {
    }

    public ManufactureMockApplicationsTask(KubernetesOperatorService kubernetesOperatorService) {
        this.kubernetesOperatorService = kubernetesOperatorService;
    }

    public static void cleanup(String dirpath) throws IOException {
        Path path = Paths.get(dirpath);
        Files.list(path).forEach(file -> {
            try {  //FIXME logging
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Files.delete(path);
    }

    @Override
    public Boolean getRawResult() {
        return result;
    }

    @Override
    protected void setRawResult(Boolean value) {
        this.result = value;
    }

    @Override
    protected boolean exec() {
        try {
            try {
                //kubernetesOperatorService.deleteAllKubernetesResources(Util.getNamespace(companyName), true);
                //kubernetesOperatorService.createNamespace(Util.getNamespace(companyName));
                cleanup("/tmp/foo");
                Files.createDirectory(Paths.get("/tmp/foo"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // create otel collectors required for the company tenant
            try {
                //kubernetesOperatorService.createOTELService(Util.getNamespace(companyName), "apmcollector");
                //kubernetesOperatorService.createStatefulSetForClientOtel(Util.getNamespace(companyName), "apmcollector-" + companyName);  // create opentelemetry client collector
                Thread.currentThread().sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // FIXME In the development approach the time taken for collector itself to download need to consider here.
            //  so a pause of say 15 sec is ok before spinning the actual customers apps in his preffered environment.
            //

            // spin up the applications that are in tenant custody
            List<MockApplication> appsToCreate = appRestRepository.findAll();
            for (MockApplication mockapp : appsToCreate) {
                try {
                    System.out.println("CREATING MOCKAPP IN KUBERNETES- " + mockapp); //FIXME
                    kubernetesOperatorService.createApplication(companyName, Util.getNamespace(companyName), mockapp.getName().toLowerCase(), mockapp.getPort() /*80*/, mockapp.getImage());
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();        //FIXME
                }
            }


//        try {
//            kubernetesOperatorService.createIngress(Util.DEFAULT_NAMESPACE_NAME, appsToCreate.stream().map(MockApplication::getName).collect(Collectors.toList()), ".punedev.logicmonitor.com" );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


            setRawResult(true);
            return result;
        } finally {
            try {
                Files.createFile(Paths.get("/tmp/foo/break.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
