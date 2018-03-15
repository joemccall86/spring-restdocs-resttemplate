package io.github.joemccall86.spring.restdocs.resttemplate;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.OperationResponseFactory;
import org.springframework.restdocs.operation.ResponseConverter;

import java.io.IOException;

public class RestTemplateResponseConverter implements ResponseConverter<ClientHttpResponse> {
    @Override
    public OperationResponse convert(ClientHttpResponse response) {


        HttpStatus statusCode = null;
        byte[] body = null;
        try {
            body = IOUtils.toByteArray(response.getBody());
            statusCode = response.getStatusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return new OperationResponseFactory().create(
                statusCode,
                response.getHeaders(),
                body
        );
    }

}
