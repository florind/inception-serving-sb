package com.newsplore.inception.api

import com.newsplore.inception.service.ClassifyImageService
import com.newsplore.inception.service.LabelWithProbability
import net.sf.jmimemagic.Magic
import net.sf.jmimemagic.MagicMatch
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

import java.io.IOException

@RestController
@RequestMapping("/api")
class AppController(private val classifyImageService: ClassifyImageService) {

    @PostMapping(value = ["/classify"])
    @CrossOrigin(origins = [("*")])
    @Throws(IOException::class)
    fun classifyImage(@RequestParam file: MultipartFile): LabelWithProbability {
        checkImageContents(file)
        return classifyImageService.classifyImage(file.bytes)
    }

    @RequestMapping(value = ["/"])
    fun index(): String {
        return "index"
    }

    private fun checkImageContents(file: MultipartFile) {
        val match: MagicMatch
        try {
            match = Magic.getMagicMatch(file.bytes)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        val mimeType = match.mimeType
        if (!mimeType.startsWith("image")) {
            throw IllegalArgumentException("Not an image type: " + mimeType)
        }
    }

}
