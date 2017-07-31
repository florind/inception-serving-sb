package com.newsplore.inception.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ClassifyImageServiceTest {
    @Autowired private ResourceLoader resourceLoader;
    @Autowired private ClassifyImageService classifyImageService;

    @Test
    public void classifyServiceMutipleImages() throws Exception {
        String[] images = new String[]{"car.png", "mule.jpg", "Blowfish.jpg", "boat.jpg", "castle.jpg", "peach.jpg"};
        List<ClassifyImageService.LabelWithProbability> classifs = Arrays.stream(images).map(image -> {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:" + image).getURI()));
                ClassifyImageService.LabelWithProbability labelWithProbability = classifyImageService.classifyImage(bytes);
                Assert.assertNotNull(labelWithProbability.getLabel());
                return labelWithProbability;
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(Collectors.toList());

        classifs.forEach(cl -> log.info("Labelled {} in {} ms", cl.getLabel(), cl.getElapsed()));
    }
}