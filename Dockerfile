FROM clojure AS cljContainer
RUN mkdir -p /usr/local/nvm
ENV NVM_DIR=/usr/local/nvm
ENV NODE_VERSION="20.11.1"
RUN apt-get update && apt-get install -y curl
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
RUN . "$NVM_DIR/nvm.sh" \
    && nvm install v$NODE_VERSION \
    && nvm use v$NODE_VERSION
ENV PATH ="$PATH:$NVM_DIR/versions/node/v$NODE_VERSION/bin"
RUN mkdir /app
WORKDIR /app
COPY *.json /app/
COPY *.js /app/
COPY *.clj /app/
COPY *.edn /app/
COPY src /app/src
COPY dev /app/dev
RUN npm install --verbose
RUN clojure -T:build uberjar
RUN mv /app/target/unabomber.jar /unabomber.jar
RUN rm -rf /app

FROM amazoncorretto
COPY --from=cljContainer /unabomber.jar /

EXPOSE 3000
CMD ["java", "-jar", "/unabomber.jar"]
