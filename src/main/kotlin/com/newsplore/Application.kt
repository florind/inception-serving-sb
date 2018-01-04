package com.newsplore

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.tensorflow.Graph

import java.io.IOException
import java.nio.charset.Charset

@SpringBootApplication
open class Application {

    private val log = LoggerFactory.getLogger(Application::class.java)

    @Bean
    @Throws(IOException::class)
    open fun tfModelGraph(@Value("\${tf.frozenModelPath}") tfFrozenModelPath: String): Graph {
        val graphResource = getResource(tfFrozenModelPath)

        val graph = Graph()
        graph.importGraphDef(IOUtils.toByteArray(graphResource.inputStream))
        log.info("Loaded Tensorflow model")
        return graph
    }

    private fun getResource(tfPath: String): Resource {
        var resource: Resource = FileSystemResource(tfPath)
        if (!resource.exists()) {
            resource = ClassPathResource(tfPath)
        }
        if (!resource.exists()) {
            throw IllegalArgumentException(String.format("File %s does not exist", tfPath))
        }
        return resource
    }

    @Bean
    @Throws(IOException::class)
    open fun tfModelLabels(@Value("\${tf.labelsPath}") labelsPath: String): List<String> {
        val labelsRes = getResource(labelsPath)
        log.info("Loaded model labels")
        return IOUtils.readLines(labelsRes.inputStream, Charset.forName("UTF-8"))
                .map { label -> label.substring(if (label.contains(":")) label.indexOf(":") + 1 else 0) }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}
