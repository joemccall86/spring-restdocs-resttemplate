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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.restdocs.JUnitRestDocumentation;
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

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class GettingStartedDocumentation {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

	@Before
	public void setUp() {
        // To successfully test error responses, use a non-default request factory
        restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());

        // Set up the initial interceptors
        restTemplate.getInterceptors().add(documentationConfiguration(restDocumentation));
        restTemplate.getInterceptors().add(document("{method-name}/{step}/"));
	}

	@Test
	public void index() throws Exception {
        RequestEntity requestEntity = RequestEntity.get(
                new URI("http://localhost:" + port + "/"))
                .accept(MediaTypes.HAL_JSON)
                .build();

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody().path("_links").path("notes"));
        assertNotNull(responseEntity.getBody().path("_links").path("tags"));
	}

	@Test
	public void creatingANote() throws JsonProcessingException, Exception {
		String noteLocation = createNote();
		JsonNode note = getNote(noteLocation);

		String tagLocation = createTag();
		getTag(tagLocation);

		String taggedNoteLocation = createTaggedNote(tagLocation);
		JsonNode taggedNote = getNote(taggedNoteLocation);
		getTags(getLink(taggedNote, "tags"));

		tagExistingNote(noteLocation, tagLocation);
		getTags(getLink(note, "tags"));
	}


	String createNote() throws Exception {
		Map<String, String> note = new HashMap<String, String>();
		note.put("title", "Note creation with cURL");
		note.put("body", "An example of how to create a note using cURL");

		URI noteLocation = restTemplate.postForLocation("http://localhost:" + port + "/notes", note);

		return noteLocation.toString();
	}


	JsonNode getNote(String noteLocation) throws Exception {
	    return restTemplate.getForObject(noteLocation, JsonNode.class);
	}


	String createTag() throws Exception, JsonProcessingException {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "getting-started");

        URI tagLocation = restTemplate.postForLocation("http://localhost:" + port + "/tags", tag);

        return tagLocation.toString();
	}

	void getTag(String tagLocation) throws Exception {
	    restTemplate.getForEntity(tagLocation, JsonNode.class);
	}

	String createTaggedNote(String tag) throws Exception {
		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "Tagged note creation with cURL");
		note.put("body", "An example of how to create a tagged note using cURL");
		note.put("tags", Arrays.asList(tag));

		URI noteLocation = restTemplate.postForLocation("http://localhost:" + port + "/notes", note);

		return noteLocation.toString();
	}

	void getTags(String noteTagsLocation) throws Exception {
	    restTemplate.getForEntity(noteTagsLocation, JsonNode.class);
	}

	void tagExistingNote(String noteLocation, String tagLocation) throws Exception {
		Map<String, Object> update = new HashMap<String, Object>();
		update.put("tags", Arrays.asList(tagLocation));

		restTemplate.patchForObject(noteLocation, update, JsonNode.class);
	}

	JsonNode getTaggedExistingNote(String noteLocation) throws Exception {
		return restTemplate.getForObject(noteLocation, JsonNode.class);
	}

	void getTagsForExistingNote(String noteTagsLocation) throws Exception {
		restTemplate.getForObject(noteTagsLocation, JsonNode.class);
	}

	private String getLink(JsonNode result, String rel) {
	    return result.path("_links").path(rel).path("href").textValue();
	}
}
