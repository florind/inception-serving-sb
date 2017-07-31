package com.newsplore.inception.api;

import com.newsplore.inception.service.ClassifyImageService;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AppController {

    private ClassifyImageService classifyImageService;

    public AppController(ClassifyImageService classifyImageService) {
        this.classifyImageService = classifyImageService;
    }

    @PostMapping(value = "/classify")
    @CrossOrigin(origins = "*")
    public ClassifyImageService.LabelWithProbability classifyImage(@RequestParam MultipartFile file) throws IOException {
        checkImageContents(file);
        return classifyImageService.classifyImage(file.getBytes());
    }

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    private void checkImageContents(MultipartFile file) {
        MagicMatch match;
        try {
            match = Magic.getMagicMatch(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String mimeType = match.getMimeType();
        if (!mimeType.startsWith("image")) {
            throw new IllegalArgumentException("Not an image type: " + mimeType);
        }
    }

}
