package cn.showcodes.raft;

public interface RaftLogService {
    RaftLog get(long index);
    RaftLog[] fetch(long start, int max);
    RaftLog append(RaftLogType type, long term, byte[] data);
    RaftLog append(RaftLog logItem);
    RaftLog commit(RaftLog logItem);
    long getCommitIndex();
    long commitTo(long leaderCommit);
}
