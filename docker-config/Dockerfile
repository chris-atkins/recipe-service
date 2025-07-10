#FROM java:8-jdk
FROM amazoncorretto:18-alpine3.16-jdk

ADD recipe-service.jar recipe-service.jar
# ADD example.keystore /data/example.keystore
ADD config.yml config.yml

# RUN java -jar dropwizard-example-1.0.0.jar db migrate /data/example.yml

CMD java -jar recipe-service.jar server config.yml

EXPOSE 5555
EXPOSE 3555