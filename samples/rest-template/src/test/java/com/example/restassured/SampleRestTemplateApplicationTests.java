/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.restassured;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static io.github.joemccall86.spring.restdocs.resttemplate.RestTemplateRestDocumentation.document;
import static io.github.joemccall86.spring.restdocs.resttemplate.RestTemplateRestDocumentation.documentationConfiguration;
import static org.junit.Assert.assertEquals;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class SampleRestTemplateApplicationTests {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	private RestTemplate restTemplate;

	@LocalServerPort
	private int port;

	@Before
	public void setUp() {
		restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(documentationConfiguration(restDocumentation));
	}

	@Test
	public void sample() throws Exception {

	    // Set up the document
		restTemplate.getInterceptors().add(
				document("sample",
				preprocessRequest(
						removeHeaders("Accept-Charset")
				))
		);

		// Set the headers
		RequestEntity requestEntity = RequestEntity.get(
				UriComponentsBuilder.newInstance()
						.scheme("http")
						.host("localhost")
						.port(port).build().toUri())
				.accept(MediaType.TEXT_PLAIN)
				.build();

		// Perform the request
		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

		// Ensure it responds with OK
		assertEquals(response.getStatusCode(), HttpStatus.OK);
	}

}
