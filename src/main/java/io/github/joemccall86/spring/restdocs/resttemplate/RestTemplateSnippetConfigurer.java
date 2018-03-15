package io.github.joemccall86.spring.restdocs.resttemplate;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.restdocs.config.SnippetConfigurer;

import java.io.IOException;

public class RestTemplateSnippetConfigurer extends
        SnippetConfigurer<RestTemplateRestDocumentationConfigurer, RestTemplateSnippetConfigurer>
    implements ClientHttpRequestInterceptor {
    /**
     * Creates a new {@code SnippetConfigurer} with the given {@code parent}.
     *
     * @param restTemplateRestDocumentationConfigurer the parent
     */
    protected RestTemplateSnippetConfigurer(RestTemplateRestDocumentationConfigurer restTemplateRestDocumentationConfigurer) {
        super(restTemplateRestDocumentationConfigurer);
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        return execution.execute(request, body);
    }
}
