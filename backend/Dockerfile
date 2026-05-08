FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -s /root/.m2/settings.xml dependency:go-offline -B
COPY src ./src
RUN mvn -s /root/.m2/settings.xml clean package -DskipTests

FROM tomcat:9.0-jdk17-temurin
WORKDIR /opt/tomcat
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /app/target/smart-community.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir -p /opt/tomcat/uploads
EXPOSE 8080
CMD ["catalina.sh", "run"]
