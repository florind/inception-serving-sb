package com.newsplore

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.restdocs.constraints.ConstraintDescriptions
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.util.StringUtils

import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Attributes.key

@Configuration
open class DocumentationConfig {

    @Bean
    open fun restDocumentation(): RestDocumentationResultHandler {
        return MockMvcRestDocumentation.document("{method-name}/{step}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
    }

    class ConstrainedFields(input: Class<*>) {
        private val constraintDescriptions: ConstraintDescriptions = ConstraintDescriptions(input)

        fun withPath(path: String): FieldDescriptor {
            //for nesting we need to get the last element of the dot-separated path
            var nestedField = path
            if (path.contains(".")) {
                nestedField = path.substring(path.lastIndexOf(".") + 1)
            }
            return fieldWithPath(path)
                    .attributes(key("constraints")
                            .value(StringUtils.collectionToDelimitedString(
                                    this.constraintDescriptions.descriptionsForProperty(nestedField), ". ")))
        }
    }

    companion object {
        const val API_HOST = "api"
        const val GENERATED_SNIPPETS_DIR = "build/generated-snippets"
    }
}
