package com.newsplore.inception.service

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.junit4.SpringRunner

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Arrays

@RunWith(SpringRunner::class)
@SpringBootTest
class ClassifyImageServiceTest {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired private val resourceLoader: ResourceLoader? = null
    @Autowired private val classifyImageService: ClassifyImageService? = null

    @Test
    @Throws(Exception::class)
    fun classifyServiceMutlipleImages() {
        val images = arrayOf("car.png", "mule.jpg", "Blowfish.jpg", "boat.jpg", "castle.jpg", "peach.jpg")
        val classifs = Arrays.stream(images).map<LabelWithProbability> { image ->
            try {
                val bytes = Files.readAllBytes(Paths.get(resourceLoader!!.getResource("classpath:" + image).uri))
                val labelWithProbability = classifyImageService!!.classifyImage(bytes)
                Assert.assertNotNull(labelWithProbability.label)
                labelWithProbability
            } catch (e: IOException) {
                throw IllegalArgumentException(e)
            }
        }

        classifs.forEach { cl -> log.info("Labelled {} in {} ms", cl.label, cl.elapsed) }
    }

}