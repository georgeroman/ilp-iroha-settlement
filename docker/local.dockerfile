# Used for testing purposes to quickly build a docker image directly from a local build

# Usage:
# Run the following in the root directory of the repository:
# docker build -f docker/local.dockerfile -t groman99/ilp-iroha-settlement .

FROM openjdk:8-jre-alpine
WORKDIR /ilp
COPY ./target/ilp-iroha-settlement-master-SNAPSHOT.jar /ilp/ilp-iroha-settlement-master-SNAPSHOT.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "ilp-iroha-settlement-master-SNAPSHOT.jar"]
