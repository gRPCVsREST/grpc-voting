package com.grpcvsrest.grpc.voting;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import com.grpcvsrest.grpc.LeaderboardServiceGrpc;
import com.grpcvsrest.grpc.LeaderboardServiceGrpc.LeaderboardServiceFutureStub;
import com.grpcvsrest.grpc.ResponseTypeServiceGrpc;
import com.grpcvsrest.grpc.ResponseTypeServiceGrpc.ResponseTypeServiceFutureStub;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import static brave.sampler.Sampler.ALWAYS_SAMPLE;

public class VotingServer {

    public static void main(String[] args) throws Exception {

        GrpcTracing grpcTracing = grpcTracing();

        String leaderboardHost = System.getenv("leaderboard_host");
        int leaderboardPort = Integer.valueOf(System.getenv("leaderboard_port"));

        Channel leaderboardChannel = NettyChannelBuilder
                .forAddress(leaderboardHost, leaderboardPort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext(true)
                .build();

        String responseTypeCheckHost = System.getenv("grpc_aggregator_host");
        int responseTypeCheckPort = Integer.valueOf(System.getenv("grpc_aggregator_port"));

        Channel responseTypeCheckChannel = NettyChannelBuilder
                .forAddress(responseTypeCheckHost, responseTypeCheckPort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext(true)
                .build();

        LeaderboardServiceFutureStub leaderboardClient = LeaderboardServiceGrpc.newFutureStub(leaderboardChannel);
        ResponseTypeServiceFutureStub responseTypeClient =
                ResponseTypeServiceGrpc.newFutureStub(responseTypeCheckChannel);

        Server grpcServer = NettyServerBuilder.forPort(8080)
                .addService(new VotingService(responseTypeClient, leaderboardClient))
                .intercept(grpcTracing.newServerInterceptor()).build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(grpcServer::shutdown));
        grpcServer.awaitTermination();
    }

    private static GrpcTracing grpcTracing() {

        String zipkinHost = System.getenv("ZIPKIN_SERVICE_HOST");
        int zipkinPort = Integer.valueOf(System.getenv("ZIPKIN_SERVICE_PORT"));

        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(String.format("http://%s:%s/api/v2/spans", zipkinHost, zipkinPort))
                .build();

        return GrpcTracing.create(Tracing.newBuilder()
                .sampler(ALWAYS_SAMPLE)
                .spanReporter(AsyncReporter.create(sender))
                .build());
    }

}
