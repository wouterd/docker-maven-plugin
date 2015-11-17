FROM wouterd/java8

ADD discuss-jar-with-dependencies.jar /discuss.jar

ENTRYPOINT ["java", "-jar", "-Dmongo.host=mongo", "/discuss.jar"]

EXPOSE 8080
