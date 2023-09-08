package com.newsplore.inception.api;

import com.newsplore.inception.service.ClassifyImageService;
import java.io.IOException;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AppController {

    private final ClassifyImageService classifyImageService;

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
