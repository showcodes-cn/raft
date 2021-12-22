package cn.showcodes.raft.impl.frame;

import org.junit.Assert;
import org.junit.Test;

public class RaftFrameVoteRequestTest {

    @Test
    public void testCodec() {
        RaftFrameVoteRequest request = new RaftFrameVoteRequest();
        request.setTerm(100);
        request.setLastLogTerm(101);
        request.setLastLogIndex(102);
        request.setCandidateId("testCandidate");

        byte[] bytes = request.toBytes();
        RaftFrameVoteRequest clone = RaftFrameVoteRequest.from(bytes);
        Assert.assertEquals(request.term, clone.term);
        Assert.assertEquals(request.lastLogTerm, clone.lastLogTerm);
        Assert.assertEquals(request.lastLogIndex, clone.lastLogIndex);
        Assert.assertEquals(request.getCandidateId(), clone.getCandidateId());
    }
}
