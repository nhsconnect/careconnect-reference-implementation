FROM openjdk:8-jdk-alpine
VOLUME /tmp

ADD target/cc-cli.jar cc-cli.jar

# Copy the EntryPoint
COPY ./entryPoint.sh /
RUN chmod +x /entryPoint.sh

ENTRYPOINT ["/entryPoint.sh"]


