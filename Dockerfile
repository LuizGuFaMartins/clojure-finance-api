FROM clojure:openjdk-11-tools-deps-1.10.3.855

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . .

RUN clojure -X:uberjar

CMD ["sh", "-c", "java -jar clj-app.jar"]