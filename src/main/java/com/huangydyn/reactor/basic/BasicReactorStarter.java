package com.huangydyn.reactor.basic;

public class BasicReactorStarter {
    public static void main(String[] args) {
        try {
            Thread th = new Thread(new Reactor(10393));
            th.setName("Reactor");
            th.start();
            th.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
