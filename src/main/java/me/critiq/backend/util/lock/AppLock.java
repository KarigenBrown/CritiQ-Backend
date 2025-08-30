package me.critiq.backend.util.lock;

public interface AppLock {
    Boolean tryLock(Long seconds);

    void unlock();
}
