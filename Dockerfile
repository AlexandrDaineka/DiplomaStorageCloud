FROM openjdk:21-jdk-alpine

EXPOSE 8080

COPY target/DiplomaStorageCloud-0.0.1-SNAPSHOT.jar app.jar

ADD src/main/resources/application.properties src/main/resources/application.properties

CMD ["java","-jar","app.jar"]