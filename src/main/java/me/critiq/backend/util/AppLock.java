package me.critiq.backend.util;

public interface AppLock {
    Boolean tryLock(Long seconds);

    void unlock();
}
