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

package com.example

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.junit.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import spock.lang.Shared
import spock.lang.Specification

import static io.github.joemccall86.spring.restdocs.resttemplate.RestTemplateRestDocumentation.document
import static io.github.joemccall86.spring.restdocs.resttemplate.RestTemplateRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.*

@Integration
@Rollback
class ApiDocumentationSpec extends Specification {

	@Rule
	JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation()

	@Value('${local.server.port}')
	Integer serverPort

	// We do not wish to register the default converters as they interfere with
	// the json rendering somehow.
	RestBuilder rest = new RestBuilder(registerConverters: false)
	@Shared String urlBase

	void setup() {
		urlBase = "http://localhost:${serverPort}"

		rest.restTemplate.interceptors << documentationConfiguration(restDocumentation)
	}

	void 'test and document notes list request'() {
		given:
		rest.restTemplate.interceptors << document('notes-list-example',
				preprocessResponse(prettyPrint()),
				responseFields(
						fieldWithPath('[].id').description('the id of the note'),
						fieldWithPath('[].title').description('the title of the note'),
						fieldWithPath('[].body').description('the body of the note'),
						fieldWithPath('[].tags').type(JsonFieldType.ARRAY).description('the list of tags associated with the note'),
				))

		when:
		def response = rest.get("${urlBase}/notes", {
			header 'Accept', MediaType.APPLICATION_JSON.toString()
		})

		then:
		response.statusCode == HttpStatus.OK
	}

	void 'test and document create new note'() {
		given:
		rest.restTemplate.interceptors << document('notes-create-example',
				preprocessResponse(prettyPrint()),
				requestFields(
						fieldWithPath('title').description('the title of the note'),
						fieldWithPath('body').description('the body of the note'),
						subsectionWithPath('tags').type(JsonFieldType.ARRAY).description('a list of tags associated to the note')
				),
				responseFields(
						fieldWithPath('id').description('the id of the note'),
						fieldWithPath('title').description('the title of the note'),
						fieldWithPath('body').description('the body of the note'),
						subsectionWithPath('tags').type(JsonFieldType.ARRAY).description('the list of tags associated with the note')
				))

		when:
		def response = rest.post("${urlBase}/notes", {
			header 'Accept', MediaType.APPLICATION_JSON.toString()
			json '{ "body": "My test example", "title": "Eureka!", "tags": [{"name": "testing123"}] }'
		})

		then:
		response.statusCode == HttpStatus.CREATED
	}

	void 'test and document getting specific note'() {
		given:
		rest.restTemplate.interceptors << document('note-get-example',
				preprocessResponse(prettyPrint()),
				responseFields(
						fieldWithPath('id').description('the id of the note'),
						fieldWithPath('title').description('the title of the note'),
						fieldWithPath('body').description('the body of the note'),
						fieldWithPath('tags').type(JsonFieldType.ARRAY).description('the list of tags associated with the note'),
				))

		when:
		def response = rest.get("${urlBase}/notes/1", {
			header 'Accept', MediaType.APPLICATION_JSON.toString()
		})

		then:
		response.statusCode == HttpStatus.OK
	}

}
