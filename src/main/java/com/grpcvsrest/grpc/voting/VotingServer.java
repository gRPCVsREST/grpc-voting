package com.grpcvsrest.grpc.voting;

import com.grpcvsrest.grpc.LeaderboardServiceGrpc;
import com.grpcvsrest.grpc.LeaderboardServiceGrpc.LeaderboardServiceFutureStub;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;

public class VotingServer {

    public static void main(String[] args) throws Exception {
        String leaderboardHost = System.getenv("leaderboard_host");
        int leaderboardPort = Integer.valueOf(System.getenv("leaderboard_port"));

        Channel leaderboardChannel = NettyChannelBuilder.forAddress(leaderboardHost, leaderboardPort)
                .usePlaintext(true)
                .build();

        LeaderboardServiceFutureStub leaderboardClient = LeaderboardServiceGrpc.newFutureStub(leaderboardChannel);

        Server grpcServer = NettyServerBuilder.forPort(8080)
                .addService(new VotingService(leaderboardClient)).build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(grpcServer::shutdown));
        grpcServer.awaitTermination();
    }

}
