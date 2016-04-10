FROM java:8-jdk

ADD recipe-service.jar recipe-service.jar
# ADD example.keystore /data/example.keystore
ADD docker.config.yml config.yml

# RUN java -jar dropwizard-example-1.0.0.jar db migrate /data/example.yml

CMD java -jar recipe-service.jar server config.yml

EXPOSE 8080