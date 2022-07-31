package com.example.concurrence_utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * 没有认清并发工具的使用场景，因而导致性能问题
 * <p>
 * 除了 ConcurrentHashMap 这样通用的并发工具类之外，我们的工具包中还有些针对特殊场景实现的生面孔。
 * 一般来说，针对通用场景的通用解决方案，在所有场景下性能都还可以，属于“万金油”；
 * 而针对特殊场景的特殊实现，会有比通用解决方案更高的性能，但一定要在它针对的场景下使用，否则可能会产生性能问题甚至是 Bug。
 * <p>
 * 之前在排查一个生产性能问题时，我们发现一段简单的非数据库操作的业务逻辑，消耗了超出预期的时间，在修改数据时操作本地缓存比回写数据库慢许多。
 * 查看代码发现，开发同学使用了 CopyOnWriteArrayList 来缓存大量的数据，而数据变化又比较频繁。
 * CopyOnWrite 是一个时髦的技术，不管是 Linux 还是 Redis 都会用到。
 * <p>
 * 在 Java 中，CopyOnWriteArrayList 虽然是一个线程安全的
 * ArrayList，但因为其实现方式是，每次修改数据时都会复制一份数据出来，
 * 所以有明显的适用场景，即读多写少或者说希望无锁读的场景。如果我们要使用 CopyOnWriteArrayList，那一定是因为场景需要而不是因为足够酷炫。
 * 如果读写比例均衡或者有大量写操作的话，使用 CopyOnWriteArrayList 的性能会非常糟糕。
 * <p>
 * 我们写一段测试代码，来比较下使用 CopyOnWriteArrayList 和普通加锁方式 ArrayList 的读写性能吧。
 * 在这段代码中我们针对并发读和并发写分别写了一个测试方法，测试两者一定次数的写或读操作的耗时。
 */
@RestController
@RequestMapping("/No04MisunderstandConcurrencyToolsUsageSceneBug")
public class No04MisunderstandConcurrencyToolsUsageSceneBug {
    Logger log = LoggerFactory.getLogger(getClass());

    // 测试并发写的性能
    @GetMapping("write")
    public String testWrite() {
        CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());

        StopWatch stopWatch = new StopWatch();
        int loopCount = 100000;

        stopWatch.start("Write: copyOnWriteArrayList");
        // 循环100000次并发往CopyOnWriteArrayList写入随机元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(
                __ -> copyOnWriteArrayList.add(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();

        stopWatch.start("Write: synchronizedList");
        // 循环100000次并发往加锁的ArrayList写入随机元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(
                __ -> synchronizedList.add(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();

        log.info(stopWatch.prettyPrint());
        return stopWatch.prettyPrint();
    }

    // 帮助方法，用来填充List
    private void addAll(List<Integer> list, int size) {
        list.addAll(IntStream.rangeClosed(1, size).boxed().toList());
    }

    // 测试并发读的性能
    @GetMapping("read")
    public String testRead() {
        // 建两个测试对象
        CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());

        // 填充数据
        int loopCount = 1000000;
        addAll(copyOnWriteArrayList, loopCount);
        addAll(synchronizedList, loopCount);

        StopWatch stopWatch = new StopWatch();

        stopWatch.start("Read: copyOnWriteArrayList");
        // 循环1000000次并发从CopyOnWriteArrayList随机查询元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(
                __ -> copyOnWriteArrayList.get(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();

        stopWatch.start("Read: synchronizedList");
        // 循环1000000次并发从加锁的ArrayList随机查询元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(
                __ -> synchronizedList.get(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();

        log.info(stopWatch.prettyPrint());
        return stopWatch.prettyPrint();
    }
}
