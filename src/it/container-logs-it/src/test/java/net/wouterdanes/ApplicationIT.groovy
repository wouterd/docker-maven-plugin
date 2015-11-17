package net.wouterdanes

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

import static javax.ws.rs.client.Entity.entity

class ApplicationIT extends Specification {

  static Logger log = LoggerFactory.getLogger(ApplicationIT)

  WebTarget app1;
  WebTarget app2;

  void setup() {

    def appBase1 = System.getProperty('app.base.1')
    app1 = ClientBuilder.newClient().target(appBase1)

    def appBase2 = System.getProperty('app.base.2')
    app2 = ClientBuilder.newClient().target(appBase2)

  }

  def "Posts can be added"() {

    when:
    def message = new JsonBuilder([
        body: 'TEST BODY'
    ])
    def response = app1.path('posts')
        .request().buildPost(entity(message.toString(), MediaType.APPLICATION_JSON_TYPE))
        .invoke()

    then:
    response.status == 201

    cleanup:
    response.close()

  }

  def "Posts can be looked up"() {

    when:
    def response = app1.path('posts').request(MediaType.APPLICATION_JSON_TYPE).get()

    then:
    response.status == 404 || response.status == 200

    cleanup:
    response.close()

  }

  def "The last post posted is the first post returned from another node"() {

    when:
    def messageBody = UUID.randomUUID().toString()

    def message = new JsonBuilder([
        body: messageBody
    ]).toString()

    def payload = entity(message, MediaType.APPLICATION_JSON_TYPE)

    app1.path('posts').request().post(payload).close()

    def response = app2.path('posts').request(MediaType.APPLICATION_JSON_TYPE).get()
    def result = response.readEntity(String)

    def json = new JsonSlurper().parseText(result)

    then:
    json[0].message.body == messageBody

  }

  def "The message service returns the APP_MESSAGE enviroment variable"() {

    when:
    def app1Response = app1.path('message').request().get(String)
    def app2Response = app2.path('message').request().get(String)

    then:
    app1Response == 'Hello, world!'
    app2Response == 'I am, so I message'

  }
}
