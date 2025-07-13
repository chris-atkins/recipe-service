FROM amazoncorretto:18

ADD recipe-service.jar recipe-service.jar
ADD config.yml config.yml

CMD java -jar recipe-service.jar server config.yml

EXPOSE 5555
EXPOSE 3555
