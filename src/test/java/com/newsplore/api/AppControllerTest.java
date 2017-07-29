package com.newsplore.api;

import com.newsplore.service.ClassifyImageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.newsplore.DocumentationConfig.API_HOST;
import static com.newsplore.DocumentationConfig.GENERATED_SNIPPETS_DIR;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureRestDocs(value = GENERATED_SNIPPETS_DIR, uriHost = API_HOST, uriPort = 80)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@SpringBootTest
public class AppControllerTest {
    @Autowired private MockMvc mockMvc;

    @Autowired private ResourceLoader resourceLoader;
    @Autowired private ClassifyImageService classifyImageService;

    @Test
    public void classifyImageApiOk() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:Blowfish.jpg").getURI()));
        MockMultipartFile file = new MockMultipartFile("file", "boat.png", "image/jpeg", bytes);

        String blowfish = this.mockMvc.perform(
            fileUpload("/api/classify")
                .file(file))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void classifyServiceMutipleImages() throws Exception {
        String[] images = new String[]{"car.png", "mule.jpg", "Blowfish.jpg", "boat.jpg", "castle.jpg", "peach.jpg"};
        List<ClassifyImageService.LabelWithProbability> classifs = Arrays.stream(images).map(image -> {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:" + image).getURI()));
                return classifyImageService.classifyImage(bytes);
            } catch (IOException e) {
               throw new IllegalArgumentException(e);
            }
        }).collect(Collectors.toList());

    }
}