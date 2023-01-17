FROM openjdk:16-jdk-slim
EXPOSE 3031

WORKDIR /app
COPY ./build/install/BookmarkSync/bin ./bin
COPY ./build/install/BookmarkSync/lib ./lib

WORKDIR /app/bin
ENTRYPOINT ["./BookmarkSync"]