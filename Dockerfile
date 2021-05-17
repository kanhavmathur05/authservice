FROM openjdk:8
ADD target/authservice-0.0.1-SNAPSHOT.jar auth-service-image
EXPOSE 8080
ENTRYPOINT ["java","-jar","auth-service-image"]