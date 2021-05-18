#FROM openjdk:8
#ADD target/authservice-0.0.1-SNAPSHOT.jar auth-service-image
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","auth-service-image"]

#### Stage 1: Build the application
FROM openjdk:8-jdk-alpine as build

# Set the current working directory inside the image
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# Build all the dependencies in preparation to go offline. 
# This is a separate step so the dependencies will be cached unless 
# the pom.xml file has changed.
RUN chmod +x ./mvnw
# RUN mvn --version
# RUN mvn install:install-file -Dfile="./stocknote-bridge-java-1.0.1.jar" -DgroupId=io.samco -DartifactId=stocknote-bridge-java -Dversion=1.0.1 -Dpackaging=jar
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

#### Stage 2: A minimal docker image with command to run the app 
FROM openjdk:8-jre-alpine

ARG DEPENDENCY=/app/target/dependency

# Copy project dependencies from the build stage
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.authservice"]