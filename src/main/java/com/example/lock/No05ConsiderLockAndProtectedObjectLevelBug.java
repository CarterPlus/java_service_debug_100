package com.example.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * Bug05: 加锁前要清楚锁和被保护的对象是不是一个层面的
 * 除了没有分析清线程、业务逻辑和锁三者之间的关系随意添加无效的方法锁外，还有一种比较常见的错误是，没有理清楚锁和要保护的对象是否是一个层面的。
 * 静态字段属于类，类级别的锁才能保护；而非静态字段属于类实例，实例级别的锁就可以保护。
 */
@Slf4j
public class No05ConsiderLockAndProtectedObjectLevelBug {
    @Getter
    private static int counter = 0;
    private static final int count = 1000000;
    private static final Object locker = new Object();

    public static int reset() {
        counter = 0;
        return counter;
    }

    /**
     * 在非静态的 wrong 方法上加锁，只能确保多个线程无法执行同一个实例的 wrong 方法，却不能保证不会执行不同实例的 wrong 方法。
     * 而静态的 counter 在多个实例中共享，所以必然会出现线程安全问题。
     */
    public synchronized void wrong() {
        counter++;
    }

    @Test
    public void testWrong() {
        No05ConsiderLockAndProtectedObjectLevelBug.reset();
        // 多线程循环一定次数调用Data类不同实例的wrong方法
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new No05ConsiderLockAndProtectedObjectLevelBug().wrong());
        System.out.println(No05ConsiderLockAndProtectedObjectLevelBug.getCounter());
        // 因为默认运行 100 万次，所以执行后应该输出 100 万，但页面输出的是178975
    }

    /**
     * 理清思路后，修正方法就很清晰了：同样在类中定义一个 Object 类型的静态字段，在操作 counter 之前对这个字段加锁。
     */
    public void right() {
        synchronized (locker) {
            counter++;
        }
    }

    @Test
    public void testRight() {
        No05ConsiderLockAndProtectedObjectLevelBug.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new No05ConsiderLockAndProtectedObjectLevelBug().right());
        log.info("result: {}", No05ConsiderLockAndProtectedObjectLevelBug.getCounter());
    }
}
