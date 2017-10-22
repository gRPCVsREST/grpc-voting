FROM java:8
WORKDIR /
ADD build/libs/grpc-voting-1.0-SNAPSHOT.jar grpc-voting.jar
EXPOSE 8080
CMD java -jar grpc-voting.jar
