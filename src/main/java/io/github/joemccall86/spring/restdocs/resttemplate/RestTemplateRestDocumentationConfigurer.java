package io.github.joemccall86.spring.restdocs.resttemplate;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.config.RestDocumentationConfigurer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RestTemplateRestDocumentationConfigurer extends
        RestDocumentationConfigurer<RestTemplateSnippetConfigurer,
                RestTemplateOperationPreprocessorsConfigurer,
                RestTemplateRestDocumentationConfigurer>
        implements ClientHttpRequestInterceptor {

    private final RestTemplateSnippetConfigurer snippetConfigurer = new RestTemplateSnippetConfigurer(this);

    private final RestTemplateOperationPreprocessorsConfigurer operationPreprocessorsConfigurer = new RestTemplateOperationPreprocessorsConfigurer(this);

    private RestDocumentationContextProvider contextProvider;

    RestTemplateRestDocumentationConfigurer(RestDocumentationContextProvider contextProvider) {

        this.contextProvider = contextProvider;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        RestDocumentationContext context = this.contextProvider.beforeOperation();
        Map<String, Object> configuration = new HashMap<>();
        apply(configuration, context);
        ContextHolder.configuration = configuration;
        ContextHolder.context = context;
        return execution.execute(request, body);
    }

    @Override
    public RestTemplateSnippetConfigurer snippets() {
        return snippetConfigurer;
    }

    @Override
    public RestTemplateOperationPreprocessorsConfigurer operationPreprocessors() {
        return this.operationPreprocessorsConfigurer;
    }
}
