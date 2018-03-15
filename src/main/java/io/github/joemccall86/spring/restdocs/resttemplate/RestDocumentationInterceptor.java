package io.github.joemccall86.spring.restdocs.resttemplate;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class RestDocumentationInterceptor implements ClientHttpRequestInterceptor {

    private final RestDocumentationGenerator<RequestEntity, ClientHttpResponse> delegate;

    RestDocumentationInterceptor(RestDocumentationGenerator<RequestEntity, ClientHttpResponse> delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        RequestEntity requestEntity = new RequestEntity(body, request.getHeaders(), request.getMethod(), request.getURI());
        ClientHttpResponse response = execution.execute(request, body);

        byte[] responseBody = IOUtils.toByteArray(response.getBody());
        ClientHttpResponse copyForDocument = new WrappedHttpResponse(response, responseBody);
        ClientHttpResponse copyForTest = new WrappedHttpResponse(response, responseBody);

        Map<String, Object> configuration = ContextHolder.configuration;
        configuration.put(RestDocumentationContext.class.getName(), ContextHolder.context);
        delegate.handle(requestEntity, copyForDocument, configuration);

        return copyForTest;
    }

    class WrappedHttpResponse implements ClientHttpResponse {

        InputStream bodyData;
        ClientHttpResponse delegate;

        WrappedHttpResponse(ClientHttpResponse delegate, byte[] rawData) {
            this.delegate = delegate;
            this.bodyData = new ByteArrayInputStream(rawData);
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return delegate.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            try {
                bodyData.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public InputStream getBody() throws IOException {
            return bodyData;
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }
    }
}
