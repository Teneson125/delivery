# Use the official Maven image as a base
FROM maven:3.9.5 AS build

# Set the working directory in the container
WORKDIR /app

ARG GITHUB_ACTOR=Teneson125
ARG PACKAGES_TOKEN=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
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV PACKAGES_TOKEN=${PACKAGES_TOKEN}

# Copy Maven settings for GitHub Packages
RUN mkdir -p /root/.m2
COPY settings.xml /root/.m2/settings.xml

# Copy your Spring Boot application source code into the container
COPY . .

# Build the Spring Boot application using Maven
RUN mvn clean package -DskipTests

# Use the official Amazon Corretto image for Java 21
FROM amazoncorretto:21

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar ./app.jar

EXPOSE 8080

# Specify the command to run on container start
CMD ["java", "-jar", "app.jar"]
