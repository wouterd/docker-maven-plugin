FROM tomcat:8.0.30-jre8

RUN rm -rf /usr/local/tomcat/webapps/*

ADD test/rest.war /usr/local/tomcat/webapps/rest.war

EXPOSE 8080
