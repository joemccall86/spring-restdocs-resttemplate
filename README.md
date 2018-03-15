# Spring REST Docs RestTemplate [![CircleCI](https://circleci.com/gh/joemccall86/spring-restdocs-resttemplate/tree/master.svg?style=svg)](https://circleci.com/gh/joemccall86/spring-restdocs-resttemplate/tree/master)  [ ![Download](https://api.bintray.com/packages/joemccall86/jvm-libs/spring-restdocs-resttemplate/images/download.svg) ](https://bintray.com/joemccall86/jvm-libs/spring-restdocs-resttemplate/_latestVersion) 
 
Spring REST Docs extension to document APIs using Spring RestTemplate

## Overview

### Gradle usage

Inside your build.gradle, add the following dependency:

```groovy
repositories {
    maven { url "https://dl.bintray.com/joemccall/jvm-libs" }
}

dependencies {
    testCompile 'io.github.joemccall86:spring-restdocs-resttemplate:0.1' 
}
```

### Test usage

At this point you can utilize spring rest docs core documentation classes as interceptors. 
For example, if you wanted to document a standard OAuth 2.0 Password Grant flow (https://www.oauth.com/oauth2-servers/access-tokens/password-grant/) 
you can utilize Spock and RestBuilder (from Grails):


```groovy
    void 'can get access token'() {

        given: 'the fields are documented'
        def rest = new RestBuilder()
        
        // Register a form message converter so it converts application/x-www-form-urlencoded
        rest.restTemplate.messageConverters.add(0, new FormHttpMessageConverter())
        
        // Document the fields using normal Spring Restdocs Syntax
        rest.restTemplate.interceptors << document('get-access-token-example',

                requestParameters(
                        parameterWithName('grant_type').description('oauth grant_type'),
                        parameterWithName('username').description('oauth username'),
                        parameterWithName('password').description('password for the username'),
                        parameterWithName('client_id').description('oauth client id'),
                        parameterWithName('client_secret').description('client secret '),
                        parameterWithName('scope').description('space-separated list of scopes requested'),
                ),

                responseFields(
                        fieldWithPath('access_token').description('the value of the access token'),
                        fieldWithPath('expires_in').description('number of seconds for which the token is valid'),
                        fieldWithPath('refresh_token').description('the value of the refresh token'),
                        fieldWithPath('token_type').description('the type of the token (should always be \'bearer\')'),
                        fieldWithPath('scope').description('space-separated list of scopes for this token'),
                )
        )

        and: 'a valid request body'
        def oauthBody = new LinkedMultiValueMap()
        oauthBody.setAll([
                grant_type   : 'password',
                username     : 'foo@example.com',
                password     : 'password',
                scope        : 'testScope',
                client_id    : 'oauth_client_id',
                client_secret: 'oauth_client_secret'
        ])

        when: 'the request is posted'
        def response = rest.post("${urlBase}/oauth/token", {
            accept 'application/json'
            contentType 'application/x-www-form-urlencoded'
            body oauthBody
        })

        then: 'the response code is OK'
        response.statusCode == HttpStatus.OK

        and: 'the json contains the correct fields'
        response.json.access_token != null
    }
``` 

### Sample

See sample project in the samples directory. It is copied from the root spring-restdocs project and adapted to use RestBuilder instead of RestAssured.

## Known Issues

* This extension does not support concurrent tests. Tests must be run serially.
* If you want to document client error codes (like 404) you must use a non-default request factory (like OkHttp3ClientHttpRequestFactory) since the SimpleHttpRequestFactory throws an exception when it encounteres a non-2xx error code.

## TODO

* Document code better
* Remove compiler warnings
* Update to work with Spring REST Docs 2.x

## Building from source

To build run `./gradlew build` from the root directory.

## Contributing

Pull requests welcome

## License

This project is open-source under the MIT license.
