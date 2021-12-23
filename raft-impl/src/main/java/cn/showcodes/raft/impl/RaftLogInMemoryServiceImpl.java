package cn.showcodes.raft.impl;

import cn.showcodes.raft.RaftLog;
import cn.showcodes.raft.RaftLogService;
import cn.showcodes.raft.RaftLogType;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RaftLogInMemoryServiceImpl implements RaftLogService {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    LinkedList<RaftLog> data = new LinkedList<>();

    AtomicLong commitPos = new AtomicLong(0);

    @Override
    public RaftLog get(long index) {
        lock.writeLock().lock();
        RaftLog logItem = data.size() > index ? data.get((int) index) : null;
        lock.writeLock().unlock();
        return logItem;
    }

    @Override
    public RaftLog[] fetch(long start, int max) {
        lock.writeLock().lock();
        RaftLog[] items =  data.subList((int)start, (int)Math.min(start + max, data.size())).toArray(new RaftLog[0]);
        lock.writeLock().unlock();
        return items;
    }

    @Override
    public RaftLog append(RaftLogType type, long term, byte[] data) {
        RaftLog raftLog = new RaftLog();
        raftLog.setData(data);
        raftLog.setType(type);
        raftLog.setTerm(term);
        return append(raftLog);
    }

    @Override
    public RaftLog append(RaftLog logItem) {
        logItem.setIndex(this.data.size());
        lock.writeLock().lock();
        this.data.add(logItem);
        lock.writeLock().unlock();
        return logItem;
    }

    @Override
    public RaftLog commit(RaftLog logItem) {
        commitPos.set(logItem.getIndex());
        return logItem;
    }

    @Override
    public long getCommitIndex() {
        return commitPos.get();
    }

    @Override
    public long commitTo(long leaderCommit) {
        return commit(get(leaderCommit)).getIndex();
    }

    @Override
    public RaftLog lastAvailableItem() {
        return data.peekLast();
    }

    @Override
    public RaftLog lastCommitItem() {
        return get(getCommitIndex());
    }

    @Override
    public int removeTo(RaftLog raftLog) {
        int total = 0;
        while(true) {
            RaftLog last = data.peekLast();
            if (last.equals(raftLog)) {
                break;
            } else {
                total++;
                data.removeLast();
            }
        }
        return total;
    }


}
