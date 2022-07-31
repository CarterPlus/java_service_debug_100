package com.example.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 在一个类里有两个 int 类型的字段 a 和 b，有一个 add 方法循环 1 万次对 a 和 b 进行 ++ 操作，
 * 有另一个 compare 方法，同样循环 1 万次判断 a 是否小于 b，条件成立就打印 a 和 b 的值，并判断 a>b 是否成立。
 */
@Slf4j
public class Interesting {
    volatile int a = 1;
    volatile int b = 1;

    public void add() {
        log.info("add start");
        for (int i = 0; i < 10000; i++) {
            a++;
            b++;
        }
        log.info("add done");
    }

    public void compare() {
        log.info("compare start");
        for (int i = 0; i < 10000; i++) {
            // a始终等于b吗？
            if (a < b) {
                log.info("a:{}, b:{}, {}", a, b, a > b);
                // 最后的a>b应该始终是false吗？
            }
        }
        log.info("compare done");
    }

    @Test
    public void test() {
        Interesting bug = new Interesting();
        new Thread(bug::add).start();
        new Thread(bug::compare).start();
    }
}
