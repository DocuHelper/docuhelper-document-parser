FROM openjdk:17-jdk-slim-buster
LABEL maintainer=docuhelper-document-parser

ENV KAFKA_HOST=192.168.0.7
ENV KAFKA_PORT=9092
ENV DOCUHELPER_FILE_ENDPOINT=http://192.168.0.77:8082
ENV OPENAI_API_KEY \
    OLLAMA_ENDPOINT

COPY . /app

WORKDIR app

RUN chmod +x gradlew

RUN sh gradlew build --warning-mode all

ENTRYPOINT sh -c 'java -jar $(find build/libs -name "*.jar" | grep -v "plain" | head -n 1)'
