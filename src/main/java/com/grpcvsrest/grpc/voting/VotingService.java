package com.grpcvsrest.grpc.voting;

import com.grpcvsrest.grpc.LeaderboardServiceGrpc.LeaderboardServiceFutureStub;
import com.grpcvsrest.grpc.RecordVoteRequest;
import com.grpcvsrest.grpc.VotingRequest;
import com.grpcvsrest.grpc.VotingResponse;
import com.grpcvsrest.grpc.VotingServiceGrpc.VotingServiceImplBase;
import io.grpc.stub.StreamObserver;

public class VotingService extends VotingServiceImplBase {

    private final LeaderboardServiceFutureStub leaderboardServiceClient;

    public VotingService(LeaderboardServiceFutureStub leaderboardServiceClient) {
        this.leaderboardServiceClient = leaderboardServiceClient;
    }

    @Override
    public void vote(VotingRequest request, StreamObserver<VotingResponse> responseObserver) {

        RecordVoteRequest recordVoteRequest = RecordVoteRequest.newBuilder()
                .setUsername(request.getUsername())
                .setVotedCategory(request.getVotedCategory())
                .setRightGuess(true)
                .build();

        leaderboardServiceClient.recordVote(recordVoteRequest);
    }
}
