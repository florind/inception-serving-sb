package com.newsplore.inception.api

import com.newsplore.DocumentationConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

import org.hamcrest.Matchers.notNullValue
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@AutoConfigureRestDocs(value = DocumentationConfig.GENERATED_SNIPPETS_DIR, uriHost = DocumentationConfig.API_HOST, uriPort = 80)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@SpringBootTest
class AppControllerTest {
    @Autowired private val mockMvc: MockMvc? = null

    @Autowired private val resourceLoader: ResourceLoader? = null

    @Test
    @Throws(Exception::class)
    fun classifyImageOk() {
        val bytes = Files.readAllBytes(Paths.get(resourceLoader!!.getResource("classpath:boat.jpg").uri))
        val file = MockMultipartFile("file", "boat.jpg", "image/jpeg", bytes)

        //val blowfish
        this.mockMvc!!.perform(
                fileUpload("/api/classify")
                        .file(file))
                .andDo(document("classify-image-ok/{step}",
                        preprocessRequest(prettyPrint(), replacePattern(Pattern.compile(".*"), "...boat.jpg multipart binary contents...")),
                        preprocessResponse(prettyPrint())))
                .andExpect(status().isOk)
                .andExpect(jsonPath("label", notNullValue()))
                .andExpect(jsonPath("probability", notNullValue()))
                .andReturn().response.contentAsString
    }
}