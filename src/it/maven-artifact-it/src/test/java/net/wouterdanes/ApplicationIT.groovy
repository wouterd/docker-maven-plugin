package net.wouterdanes

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import spock.lang.Specification

class ApplicationIT extends Specification {

    def "Check that WAR pulled down from Maven Central is deployed and working"() {

        setup:
        String baseUrl = System.getProperty("app.base.url");
        def client = new RESTClient(baseUrl)

        when: "we attempt to hit the hello endpoint"
        def resp = client.get(path: "/rest/hello")

        then: "we should get a valid 200 response"
        with(resp) {
            status == 200
            contentType == ContentType.HTML.toString()
        }
    }
}