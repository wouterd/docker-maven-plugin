package net.wouterdanes

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import javax.ws.rs.ProcessingException
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

    /*
      Very convoluted way to check if the app has started :-)
     */
    for (; ;) {
      try {
        app1.request().get().close()
        app2.request().get().close()
        break;
      } catch (ProcessingException e) {
        if (e.getCause() instanceof ConnectException) {
          log.info("Polling the app nodes..")
          Thread.sleep(1000)
          continue;
        }
        throw new IllegalStateException("Can't start test, app not reachable: ${e}")
      }
    }

  }

  def "Posts can be added"() {

    given:
    def message = new JsonBuilder([
      body: 'TEST BODY'
    ])
    def response = app1.path('posts')
            .request().buildPost(entity(message.toString(), MediaType.APPLICATION_JSON_TYPE))
            .invoke()

    expect:
    assert response.status == 201

    cleanup:
    response.close()

  }

  def "Posts can be looked up"() {

    given:
    def response = app1.path('posts').request(MediaType.APPLICATION_JSON_TYPE).get()

    expect:
    assert response.status == 404 || response.status == 200

    cleanup:
    response.close()

  }

  def "The last post posted is the first post returned from another node"() {

    given:
    def messageBody = UUID.randomUUID().toString()

    def message = new JsonBuilder([
      body: messageBody
    ]).toString()

    def payload = entity(message, MediaType.APPLICATION_JSON_TYPE)

    app1.path('posts').request().post(payload).close()

    def response = app2.path('posts').request(MediaType.APPLICATION_JSON_TYPE).get()
    def result = response.readEntity(String)

    def json = new JsonSlurper().parseText(result)

    expect:
    json[0].message.body == messageBody

  }
}
