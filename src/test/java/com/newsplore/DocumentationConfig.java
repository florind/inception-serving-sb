package com.newsplore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.util.StringUtils;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

@Configuration
public class DocumentationConfig {
    public static final String API_HOST = "api";
    public static final String GENERATED_SNIPPETS_DIR = "build/generated-snippets";

    @Bean
    public RestDocumentationResultHandler restDocumentation() {
        return MockMvcRestDocumentation.document("{method-name}/{step}",
                                                 preprocessRequest(prettyPrint()),
                                                 preprocessResponse(prettyPrint()));
    }

    public static class ConstrainedFields {
        private final ConstraintDescriptions constraintDescriptions;

        public ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        public FieldDescriptor withPath(String path) {
            //for nesting we need to get the last element of the dot-separated path
            String nestedField = path;
            if (path.contains(".")) {
                nestedField = path.substring(path.lastIndexOf(".") + 1);
            }
            return fieldWithPath(path)
                    .attributes(key("constraints")
                                .value(StringUtils.collectionToDelimitedString(
                                      this.constraintDescriptions.descriptionsForProperty(nestedField),". ")));
        }
    }
}
