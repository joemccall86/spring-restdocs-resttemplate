package io.github.joemccall86.spring.restdocs.resttemplate;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.restdocs.operation.*;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RestTemplateRequestConverter implements RequestConverter<RequestEntity> {

    private FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();


    @Override
    public OperationRequest convert(RequestEntity request) {
        final String bodyAsString = new String((byte[]) request.getBody());

        return new OperationRequestFactory().create(
                request.getUrl(),
                request.getMethod(),
                bodyAsString.getBytes(),
                request.getHeaders(),
                extractParameters(request),
                extractParts(request)
        );
    }

    private Parameters extractParameters(RequestEntity request) {
        Parameters parameters = new Parameters();

        if (MediaType.APPLICATION_FORM_URLENCODED.includes(request.getHeaders().getContentType()) &&
                request.getBody() != null) {

            // Parameters come from the body

            try {                                          
                HttpInputMessage inputMessage = new HttpInputMessage() {
                    @Override
                    public InputStream getBody() {
                        return new ByteArrayInputStream((byte[])request.getBody());
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return request.getHeaders();
                    }
                };

                MultiValueMap<String, String> converted = formHttpMessageConverter.read(null, inputMessage);
                parameters.putAll(converted);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (!StringUtils.isEmpty(request.getUrl().getQuery())) {

            // Parameters come from the request

            String body = request.getUrl().getQuery();
            Charset charset = Charset.defaultCharset();

            // Adapted from FormHttpMessageConverter
            String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
            try {
                for (String pair : pairs) {
                    int idx = pair.indexOf('=');
                    if (idx == -1) {
                        parameters.add(URLDecoder.decode(pair, charset.name()), null);
                    }
                    else {
                        String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                        String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                        parameters.add(name, value);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return parameters;
    }

    private Collection<OperationRequestPart> extractParts(RequestEntity request) {

        ArrayList<OperationRequestPart> parts = new ArrayList<>();

        if (!MediaType.MULTIPART_FORM_DATA.includes(request.getHeaders().getContentType())) {
            return parts;
        }

        try {
            RequestContext ctx = new RequestEntityRequestContext(request);

            DiskFileItemFactory factory = new DiskFileItemFactory();
            File repository = new File(System.getProperty("java.io.tmpdir"));
            factory.setRepository(repository);
            ServletFileUpload servletFileUpload = new ServletFileUpload(factory);

            List<FileItem> items = servletFileUpload.parseRequest(ctx);

            List<OperationRequestPart> parsed = items.stream().map(item -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(item.getContentType()));

                byte[] content = new byte[0];
                try {
                    content = IOUtils.toByteArray(item.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new OperationRequestPartFactory().create(
                        item.getFieldName(),
                        item.getName(),
                        content,
                        headers
                );
            }).collect(Collectors.toList());

            parts.addAll(parsed);


        } catch (FileUploadException e) {
            e.printStackTrace();
        }

        return parts;
    }

    private static class RequestEntityRequestContext implements RequestContext {
        private final RequestEntity request;

        public RequestEntityRequestContext(RequestEntity request) {
            this.request = request;
        }

        @Override
        public String getCharacterEncoding() {
            return Charset.defaultCharset().name();
        }

        @Override
        public String getContentType() {
            if (request.getHeaders().getContentType() != null) {
                return request.getHeaders().getContentType().toString();
            }

            return null;
        }

        @Override
        public int getContentLength() {
            return (int) request.getHeaders().getContentLength();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream((byte[]) request.getBody());
        }
    }
}
