package com.example.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 加锁要考虑锁的粒度和场景问题
 */
@Slf4j
public class No06ConsiderLockingGranularityBug {

    private final List<Integer> data = new ArrayList<>();

    // 不涉及共享资源的慢方法
    private void slow() {
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 错误的加锁方法
    public void wrong() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            // 加锁粒度太粗了
            synchronized (this) {
                slow();
                data.add(i);
            }
        });
        log.info("wrong took:{}", System.currentTimeMillis() - begin);
    }

    // 正确的加锁方法
    public void right() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            slow();
            // 只对List加锁
            synchronized (this) {
                data.add(i);
            }
        });
        log.info("right took:{}", System.currentTimeMillis() - begin);
    }

    @Test
    public void testAll() {
        /*
         * 16:53:33.305 [main] INFO com.example.lock.No06ConsiderLockingGranularityBug - wrong took:1266
         * 16:53:33.467 [main] INFO com.example.lock.No06ConsiderLockingGranularityBug - right took:158
         */
        new No06ConsiderLockingGranularityBug().wrong();
        new No06ConsiderLockingGranularityBug().right();
    }

}
