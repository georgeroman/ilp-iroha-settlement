# Usage:
# Run the following in the root directory of the repository:
# docker build -t groman99/ilp-iroha-settlement .

FROM alpine/git as clone
WORKDIR /ilp
RUN git clone https://github.com/georgeroman/ilp-iroha-settlement.git

FROM maven:3.6-jdk-8-alpine as build
WORKDIR /ilp
COPY --from=clone /ilp/ilp-iroha-settlement /ilp
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /ilp
COPY --from=build /ilp/target/ilp-iroha-settlement-master-SNAPSHOT.jar /ilp
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "ilp-iroha-settlement-master-SNAPSHOT.jar"]
