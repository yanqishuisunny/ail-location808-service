package com.ail.location.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j(topic = "c.TestParkUnpark")
public class TestParkUnpark {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.info("start...");
            try {
                TimeUnit.SECONDS.sleep(1);//t1睡眠了一秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("park...");
            LockSupport.park();//t1线程一秒后暂停
            log.info("resume...");
        }, "t1");
        t1.start();

        try {
            TimeUnit.SECONDS.sleep(2);//主线程睡眠二秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("unpark...");
        LockSupport.unpark(t1);//二秒后由主线程恢复t1线程的运行
    }
}