package io.github.joemccall86.spring.restdocs.resttemplate;

import org.springframework.restdocs.config.OperationPreprocessorsConfigurer;

public class RestTemplateOperationPreprocessorsConfigurer extends
        OperationPreprocessorsConfigurer<RestTemplateRestDocumentationConfigurer, RestTemplateOperationPreprocessorsConfigurer> {
    /**
     * Creates a new {@code OperationPreprocessorConfigurer} with the given
     * {@code parent}.
     *
     * @param restTemplateRestDocumentationConfigurer the parent
     */
    protected RestTemplateOperationPreprocessorsConfigurer(RestTemplateRestDocumentationConfigurer restTemplateRestDocumentationConfigurer) {
        super(restTemplateRestDocumentationConfigurer);
    }
}
