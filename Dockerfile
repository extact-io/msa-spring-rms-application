FROM docker.io/eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.source=https://github.com/extact-io/msa-rms-service-item

WORKDIR /msa-service-item

# Install packages
RUN apk update && apk add curl

# Copy the binary built in the 1st stage
COPY ./target/msa-rms-service-item.jar ./
COPY ./target/libs ./libs

CMD ["java", "-jar", "msa-rms-service-item.jar"]

EXPOSE 7002
