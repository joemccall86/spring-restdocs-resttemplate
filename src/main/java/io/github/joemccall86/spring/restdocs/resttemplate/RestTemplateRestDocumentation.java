package io.github.joemccall86.spring.restdocs.resttemplate;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;

public abstract class RestTemplateRestDocumentation {

    private static final RestTemplateRequestConverter REQUEST_CONVERTER = new RestTemplateRequestConverter();
    private static final RestTemplateResponseConverter RESPONSE_CONVERTER = new RestTemplateResponseConverter();

    private RestTemplateRestDocumentation() {
    }

    public static RestDocumentationInterceptor document(String identifier, Snippet... snippets) {
        return new RestDocumentationInterceptor(
                new RestDocumentationGenerator<>(
                        identifier,
                        REQUEST_CONVERTER, RESPONSE_CONVERTER,
                        snippets)
        );
    }

    public static RestDocumentationInterceptor document(String identifier,
                                                        OperationRequestPreprocessor requestPreprocessor,
                                                        Snippet... snippets) {
        return new RestDocumentationInterceptor(
                new RestDocumentationGenerator<>(
                        identifier,
                        REQUEST_CONVERTER, RESPONSE_CONVERTER,
                        requestPreprocessor,
                        snippets)
        );
    }

    public static RestDocumentationInterceptor document(String identifier,
                                                        OperationResponsePreprocessor responsePreprocessor, Snippet... snippets) {
        return new RestDocumentationInterceptor(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, responsePreprocessor, snippets));
    }

    public static RestDocumentationInterceptor document(String identifier,
                                                   OperationRequestPreprocessor requestPreprocessor,
                                                   OperationResponsePreprocessor responsePreprocessor, Snippet... snippets) {
        return new RestDocumentationInterceptor(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, requestPreprocessor,
                responsePreprocessor, snippets));
    }


    /**
     * Provides access to a {@link RestTemplateRestDocumentationConfigurer} that can be
     * used to configure Spring REST Docs using the given {@code contextProvider}.
     *
     * @param contextProvider the context provider
     * @return the configurer
     */
    public static RestTemplateRestDocumentationConfigurer documentationConfiguration(
            RestDocumentationContextProvider contextProvider) {
        return new RestTemplateRestDocumentationConfigurer(contextProvider);
    }
}
