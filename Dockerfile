FROM gcr.io/forgerock-io/java-17:latest

RUN mkdir /app
RUN groupadd -r rcs && useradd -r -s /bin/false -g rcs rcs

WORKDIR /app
COPY securebanking-openbanking-uk-rcs.jar /app
RUN chown -R rcs:rcs /app
USER rcs

CMD ["java", "-jar", "securebanking-openbanking-uk-rcs.jar"]