FROM maven:3.6.3-jdk-11-openj9
WORKDIR /app
COPY . /app
RUN mvn package
EXPOSE 80
CMD java -jar target/app.jar
