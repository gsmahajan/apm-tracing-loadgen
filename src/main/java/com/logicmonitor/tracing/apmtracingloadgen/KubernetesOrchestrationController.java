package com.logicmonitor.tracing.apmtracingloadgen;

import com.logicmonitor.tracing.apmtracingloadgen.config.ConfigurationRestRepository;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(path = "/orchestration/")
public class KubernetesOrchestrationController {

    @Autowired
    KubernetesOperatorService kubernetesOperatorService;

    @Autowired
    ConfigurationRestRepository configurationRestRepository;

    public KubernetesOrchestrationController() {
    }


    @RequestMapping(method = RequestMethod.GET, path = "/87a96e6fe51c0dcc8068f3cec879141a6219c988ede07eaf7291b53fcf78f2bf/getKubeServiceNames")
    public @ResponseBody
    ResponseEntity<List<String>> getKubernetesServiceRunning
            (@RequestParam(name = "kubenamespace", required = false) String namespace) throws ApiException {
        return ResponseEntity.ok(kubernetesOperatorService.getServiceNamesInNamespace(namespace));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/87a96e6fe51c0dcc8068f3cec879141a6219c988ede07eaf7291b53fcf78f2bf/create")
    public @ResponseBody
    ResponseEntity<String> createApplication(@RequestParam(name = "company") String
                                                     companyName, @RequestParam(name = "ns") String
                                                     namespace, @RequestParam(name = "appName") String appName, @RequestParam(name = "port") String
                                                     port, @RequestParam(name = "image", required = false, defaultValue = "") String image) throws Exception {
        return ResponseEntity.ok(kubernetesOperatorService.createApplication(companyName, namespace, appName.toLowerCase(), port, image));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/87a96e6fe51c0dcc8068f3cec879141a6219c988ede07eaf7291b53fcf78f2bf/lmotel")
    public @ResponseBody
    ResponseEntity<String> createStatefulset(@RequestParam(name = "company") String
                                                     companyName, @RequestParam(name = "ns") String
                                                     namespace) throws Exception {
        kubernetesOperatorService.createStatefulSetForClientOtel(namespace, companyName);
        return ResponseEntity.ok().build();
    }


    @RequestMapping(method = RequestMethod.GET, path = "/87a96e6fe51c0dcc8068f3cec879141a6219c988ede07eaf7291b53fcf78f2bf/saveTags")
    public @ResponseBody
    ResponseEntity<String> saveTags(@RequestParam(name = "path") String
                                            s3Path) throws Exception {
        // Praveen's parquet code goes here for quick type-ahead tag search capability cross-check
        return ResponseEntity.ok().build();
    }
}
