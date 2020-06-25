FROM openjdk:8-jdk-alpine

WORKDIR /opt/ilp-iroha-settlement
COPY target/ilp-iroha-settlement-master-SNAPSHOT.jar /opt/ilp-iroha-settlement/settlement-engine.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "settlement-engine.jar"]
