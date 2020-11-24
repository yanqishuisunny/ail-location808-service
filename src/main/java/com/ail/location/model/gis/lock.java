//package com.ail.location.model.gis;
//
//import java.util.concurrent.locks.ReentrantLock;
//
//public class lock extends  Thread{
//
//    // 通过构造方法给线程名字赋值
//    public lock(String name) {
//        super(name);// 给线程名字赋值
//    }
//
//    // 为了保持票数的一致，票数要静态
//    static int tick = 20;
//
//    // 创建一个静态钥匙
//    static Object ob = "aa";//值是任意的
//
//    // 重写run方法，实现买票操作
//
//    final ReentrantLock lock = new ReentrantLock(true);
//
//    @Override
//    public void run() {
//        while (tick > 0) {
//            lock.lock();
//            // 进去的人会把钥匙拿在手上，出来后才把钥匙拿让出来
//            if (tick > 0) {
//                System.out.println(getName() + "卖出了第" + tick + "张票");
//                tick--;
//            } else {
//                System.out.println("票卖完了");
//            }
//            lock.unlock();
//            try {
//                sleep(10000);//休息一秒
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//
//    public static void main(String[] args) {
//        //实例化站台对象，并为每一个站台取名字
//        lock station1 = new lock("窗口1");
//        lock station2 = new lock("窗口2");
//        lock station3 = new lock("窗口3");
//
//        // 让每一个站台对象各自开始工作
//        station1.start();
//        station2.start();
//        station3.start();
//
//    }
//}
