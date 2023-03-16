package com.logicmonitor.tracing.apmtracingloadgen.tasks;


import com.google.common.io.Files;
import com.logicmonitor.tracing.apmtracingloadgen.config.ConfigurationRestRepository;
import com.logicmonitor.tracing.apmtracingloadgen.config.LoadGenToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DummyTagsForTypeAhead {


    @Autowired
    ConfigurationRestRepository configurationRestRepository;

    private static Integer getRandomIndex(int upper) {
        return ThreadLocalRandom.current().nextInt(1, upper / 3);
    }
    // FIXME cleanup - for the fun
//    private static List<String> getDummyTagsForTypeAhead() throws IOException {
//        List<String> result = new ArrayList<>();
//        final List<String> words = Files.readLines(new File("/Users/girishmahajan/Desktop/girishdev/apm-tracing-loadgen/src/main/resources/tags.txt"), Charset.defaultCharset());
//        int randomIndex;
//        for (int i = 0; i < words.size(); ++i) {
//            result.add(words.get(getRandomIndex(words.size())) + "." + words.get(getRandomIndex(words.size())));
//        }
//        result.stream().forEach(s -> {
//            System.out.println(s);
//        });
//        return result;
//    }

    public List<String> getDummyTagsForTypeAhead() {
        LoadGenToolConfiguration loadGenToolConfiguration = configurationRestRepository.findByName("default").get(0);
        try {
            URL resource = getClass().getClassLoader().getResource("tags-combined-http.txt");
            if (Objects.nonNull(resource)) {
                File file = new File(resource.toURI());
                if (file.exists()) {
                    List<String> shuffledTags = Files.readLines(file, Charset.defaultCharset());
                    Collections.shuffle(shuffledTags);
                    return shuffledTags.stream().limit(loadGenToolConfiguration.getNbMaxTracingTagsNumber()).collect(Collectors.toList());
                } else {
                    return Arrays.asList("tags.source.file.not.exists");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList("tags.source.not.found.in.loadgen.tool");

    }
}
