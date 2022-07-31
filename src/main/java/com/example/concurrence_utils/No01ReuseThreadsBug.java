package com.example.concurrence_utils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * Bug01: 没有意识到线程重用导致用户信息错乱的 Bug
 */
@RestController
@RequestMapping("/No01ReuseThreadsBug")
public class No01ReuseThreadsBug {
    private static final ThreadLocal<Integer> currentUser = ThreadLocal.withInitial(() -> null);

    @GetMapping("wrong")
    public HashMap<String, String> wrong(@RequestParam("userId") Integer userId) {
        // 设置用户信息之前先查询一次ThreadLocal中的用户信息
        String before = Thread.currentThread().getName() + ":" + currentUser.get();
        // 设置用户信息到ThreadLocal
        currentUser.set(userId);
        // 设置用户信息之后再查询一次ThreadLocal中的用户信息
        String after = Thread.currentThread().getName() + ":" + currentUser.get();
        // 汇总输出两次查询结果
        HashMap<String, String> result = new HashMap<>();
        result.put("before", before);
        result.put("after", after);
        return result;
    }

    @GetMapping("right")
    public HashMap<String, String> right(@RequestParam("userId") Integer userId) {
        String before = Thread.currentThread().getName() + ":" + currentUser.get();
        currentUser.set(userId);
        try {
            String after = Thread.currentThread().getName() + ":" + currentUser.get();
            HashMap<String, String> result = new HashMap<>();
            result.put("before", before);
            result.put("after", after);
            return result;
        } finally {
            // 在finally代码块中删除ThreadLocal中的数据，确保数据不串
            currentUser.remove();
        }
    }

}
