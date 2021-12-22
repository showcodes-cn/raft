package cn.showcodes.raft.impl;

import cn.showcodes.raft.*;
import cn.showcodes.raft.impl.frame.RaftFrameServiceImpl;
import cn.showcodes.raft.impl.frame.RaftFrameVoteRequest;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class RaftFSMTest {

    RaftFSM testFSM() {
        RaftFSM raftFSM = new RaftFSM();
        RaftConfig raftConfig = new RaftConfig();
        raftConfig.nodeId = "nodeA";
        raftFSM.raftConfig = raftConfig;
        RaftRequestSupplier supplier = new RaftRequestInMemorySupplier();
        raftFSM.requestSupplier = supplier;
        raftFSM.raftFrameService = new RaftFrameServiceImpl();
        raftFSM.communicationService = new FakeCommunicationService();
        return raftFSM;
    }

    @Test
    public void testElectionTimeout() throws InterruptedException {
        RaftFSM raftFSM = testFSM();
        Assert.assertEquals(RaftRole.follower, raftFSM.role);

        CountDownLatch countDownLatch = new CountDownLatch(2);

        raftFSM.addHandler((request, context) -> {
            if (request.getFrame().getType() == RaftFrameType.roleChange) {
                RaftFrame raftFrame = request.getFrame();
                if (countDownLatch.getCount() == 2) {
                    Assert.assertEquals(RaftRole.follower, raftFrame.getAttributes().get("role"));
                } else {
                    Assert.assertEquals(RaftRole.candidate, raftFrame.getAttributes().get("role"));
                }
                countDownLatch.countDown();
            }
        });

        raftFSM.start();
        countDownLatch.await();
        Assert.assertEquals(RaftRole.candidate, raftFSM.role);
    }

    @Test
    public void testRequestVote() throws InterruptedException {
        RaftFSM raftFSM = testFSM();
        raftFSM.raftConfig.peers = Arrays.asList(
            new CommunicationNode("nodeB", 101),
            new CommunicationNode("nodeC", 102)
        );
        raftFSM.currentTerm = 100;
        CountDownLatch countDownLatch = new CountDownLatch(raftFSM.raftConfig.peers.size());

        raftFSM.start();

        FakeCommunicationService fakeCommunicationService = (FakeCommunicationService)raftFSM.communicationService;

        for(int i = 0; i < raftFSM.raftConfig.peers.size(); i++) {
            RaftProtocol protocol = fakeCommunicationService.outgoing.take();
            RaftFrameVoteRequest voteRequest = raftFSM.raftFrameService.voteRequest((RaftFrame) protocol);
            Assert.assertEquals(raftFSM.raftConfig.nodeId, voteRequest.getCandidateId());
            countDownLatch.countDown();
        }
        countDownLatch.await();
    }
}
