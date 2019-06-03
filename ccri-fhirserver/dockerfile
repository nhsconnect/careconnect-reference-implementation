FROM openjdk:11-slim
VOLUME /tmp

ENV JAVA_OPTS="-Xms128m -Xmx512m"


ADD target/ccri-fhirserver.jar ccri-fhirserver.jar


ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ccri-fhirserver.jar"]


