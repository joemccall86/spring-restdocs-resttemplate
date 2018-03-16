/*
 * Copyright 2014-2016 the original author or authors.
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

package com.example.notes;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.github.joemccall86.spring.restdocs.resttemplate.RestTemplateRestDocumentation.document;
import static io.github.joemccall86.spring.restdocs.resttemplate.RestTemplateRestDocumentation.documentationConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

@SpringBootTest(webEnvironment= WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiDocumentation {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

	private RestTemplate restTemplate;

	@LocalServerPort
	private int port;

	@Before
	public void setUp() {
        // To successfully test error responses, use a non-default request factory
		restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());

		// Set up the initial interceptors
		restTemplate.getInterceptors().add(documentationConfiguration(restDocumentation));
	}

//	/*
	@Test
    @Ignore // Until we can properly simulate an error
	public void errorExample() throws Exception {

	    // Set up the documentation
        restTemplate.getInterceptors().add(document("error-example",
						responseFields(
								fieldWithPath("error").description("The HTTP error that occurred, e.g. `Bad Request`"),
								fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made"),
								fieldWithPath("status").description("The HTTP status code, e.g. `400`"),
								fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred")
                        ))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.get(
                new URI("http://localhost:" + port + "/error"))
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Perform the request
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestEntity, Map.class);

        // Test the responses
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	public void indexExample() throws Exception {

        // Set up the documentation
        restTemplate.getInterceptors().add(document("index-example",
					links(
                            halLinks(),
							linkWithRel("notes").description("The <<resources-notes,Notes resource>>"),
							linkWithRel("tags").description("The <<resources-tags,Tags resource>>"),
							linkWithRel("profile").description("The ALPS profile for the service")),
					responseFields(
							subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")))
        );


        // Set up the request
        RequestEntity requestEntity = RequestEntity.get(
                new URI("http://localhost:" + port + "/"))
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Perform the request
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestEntity, Map.class);

        // Test the responses
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void notesListExample() throws Exception {
		this.noteRepository.deleteAll();

		createNote("REST maturity model",
				"http://martinfowler.com/articles/richardsonMaturityModel.html");
		createNote("Hypertext Application Language (HAL)",
				"http://stateless.co/hal_specification.html");
		createNote("Application-Level Profile Semantics (ALPS)", "http://alps.io/spec/");

        // Set up the documentation
        restTemplate.getInterceptors().add(document("notes-list-example",
					links(
					        halLinks(),
							linkWithRel("self").description("Canonical link for this resource"),
							linkWithRel("profile").description("The ALPS profile for this resource")),
					responseFields(
							subsectionWithPath("_embedded.notes").description("An array of <<resources-note, Note resources>>"),
							subsectionWithPath("_links").description("<<resources-tags-list-links, Links>> to other resources")))
        );


        // Set up the request
        RequestEntity requestEntity = RequestEntity.get(
                new URI("http://localhost:" + port + "/notes"))
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Perform the request
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestEntity, Map.class);

        // Test the responses
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void notesCreateExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

        URI tagLocation = restTemplate.postForLocation("http://localhost:" + port + "/tags", tag);

		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		note.put("tags", Arrays.asList(tagLocation));

        // Set up the documentation
        restTemplate.getInterceptors().add(document("notes-create-example",
						requestFields(
									fieldWithPath("title").description("The title of the note"),
									fieldWithPath("body").description("The body of the note"),
									fieldWithPath("tags").description("An array of tag resource URIs")))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.post(
                new URI("http://localhost:" + port + "/notes"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaTypes.HAL_JSON)
                .body(note);

        // Perform the request
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestEntity, Map.class);

        // Test the responses
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	}

	@Test
	public void noteGetExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

        URI tagLocation = restTemplate.postForLocation("http://localhost:" + port + "/tags", tag);
        Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		note.put("tags", Arrays.asList(tagLocation));

		URI noteLocation = restTemplate.postForLocation("http://localhost:" + port + "/notes", note);

        // Set up the documentation
        restTemplate.getInterceptors().add(document("note-get-example",
					links(
					        halLinks(),
							linkWithRel("self").description("Canonical link for this <<resources-note,note>>"),
							linkWithRel("note").description("This <<resources-note,note>>"),
							linkWithRel("tags").description("This note's tags")),
					responseFields(
							fieldWithPath("title").description("The title of the note"),
							fieldWithPath("body").description("The body of the note"),
							subsectionWithPath("_links").description("<<resources-note-links,Links>> to other resources")))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.get(noteLocation)
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Perform the request
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        // Test the responses
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(note.get("title"), responseEntity.getBody().get("title").textValue());
        assertEquals(note.get("body"), responseEntity.getBody().get("body").textValue());
        assertEquals(noteLocation.toString(), responseEntity.getBody().path("_links").path("self").path("href").textValue());
        assertNotNull(responseEntity.getBody().path("_links").path("tags"));
	}

	@Test
	public void tagsListExample() throws Exception {
		this.noteRepository.deleteAll();
		this.tagRepository.deleteAll();

		createTag("REST");
		createTag("Hypermedia");
		createTag("HTTP");

        // Set up the documentation
        restTemplate.getInterceptors().add(document("tags-list-example",
					links(
					        halLinks(),
							linkWithRel("self").description("Canonical link for this resource"),
							linkWithRel("profile").description("The ALPS profile for this resource")),
					responseFields(
							subsectionWithPath("_embedded.tags").description("An array of <<resources-tag,Tag resources>>"),
							subsectionWithPath("_links").description("<<resources-tags-list-links, Links>> to other resources")))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.get(
                new URI("http://localhost:" + port + "/tags"))
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Perform the request
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        // Test the responses
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void tagsCreateExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");


        // Set up the documentation
        restTemplate.getInterceptors().add(document("tags-create-example",
						requestFields(
								fieldWithPath("name").description("The name of the tag")))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.post(
                new URI("http://localhost:" + port + "/tags"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaTypes.HAL_JSON)
                .body(tag);

        // Perform the request
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        // Test the responses
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	}

	@Test
	public void noteUpdateExample() throws Exception {
		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");

		URI noteLocation = restTemplate.postForLocation("http://localhost:" + port + "/notes", note);

        Map<String, String> tag = new HashMap<String, String>();
        tag.put("name", "REST");

        URI tagLocation = restTemplate.postForLocation("http://localhost:" + port + "/tags", tag);

        Map<String, Object> noteUpdate = new HashMap<String, Object>();
		noteUpdate.put("tags", Arrays.asList(tagLocation));

        // Set up the documentation
        restTemplate.getInterceptors().add(document("note-update-example",
						requestFields(
								fieldWithPath("title").description("The title of the note").type(JsonFieldType.STRING).optional(),
								fieldWithPath("body").description("The body of the note").type(JsonFieldType.STRING).optional(),
								fieldWithPath("tags").description("An array of tag resource URIs").optional()))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.patch(noteLocation)
                .contentType(MediaTypes.HAL_JSON)
                .accept()
                .body(noteUpdate);

        // Perform the request
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        // Test the responses
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}

	@Test
	public void tagGetExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		URI tagLocation = restTemplate.postForLocation("http://localhost:" + port + "/tags", tag);

        // Set up the documentation
        restTemplate.getInterceptors().add(document("tag-get-example",
					links(
					        halLinks(),
							linkWithRel("self").description("Canonical link for this <<resources-tag,tag>>"),
							linkWithRel("tag").description("This <<resources-tag,tag>>"),
							linkWithRel("notes").description("The <<resources-tagged-notes,notes>> that have this tag")),
					responseFields(
							fieldWithPath("name").description("The name of the tag"),
							subsectionWithPath("_links").description("<<resources-tag-links,Links>> to other resources")))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.get(tagLocation)
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // Perform the request
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        // Test the responses
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(tag.get("name"), responseEntity.getBody().get("name").textValue());
	}

	@Test
	public void tagUpdateExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

        URI tagLocation = restTemplate.postForLocation("http://localhost:" + port + "/tags", tag);

		Map<String, Object> tagUpdate = new HashMap<String, Object>();
		tagUpdate.put("name", "RESTful");

        // Set up the documentation
        restTemplate.getInterceptors().add(document("tag-update-example",
						requestFields(
								fieldWithPath("name").description("The name of the tag")))
        );

        // Set up the request
        RequestEntity requestEntity = RequestEntity.patch(tagLocation)
                .contentType(MediaTypes.HAL_JSON)
                .accept()
                .body(tagUpdate);

        // Perform the request
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        // Test the responses
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}

	private void createNote(String title, String body) {
		Note note = new Note();
		note.setTitle(title);
		note.setBody(body);

		this.noteRepository.save(note);
	}

	private void createTag(String name) {
		Tag tag = new Tag();
		tag.setName(name);
		this.tagRepository.save(tag);
	}
}
