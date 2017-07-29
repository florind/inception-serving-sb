package com.newsplore.api;

import com.newsplore.service.ClassifyImageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tensorflow.Graph;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AppController {

    private ClassifyImageService classifyImageService;

    public AppController(ClassifyImageService classifyImageService) {
        this.classifyImageService = classifyImageService;
    }

    @PostMapping(value = "/classify")
    //@CrossOrigin(origins = "*")
    public ClassifyImageService.LabelWithProbability classifyImage(@RequestParam MultipartFile file) throws IOException {
        return classifyImageService.classifyImage(file.getBytes());
    }

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

}
