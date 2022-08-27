package com.huangydyn.reactor.multireactor;

import java.io.IOException;

public class Starter {

    // 启动
    public static void main(String[] args) throws IOException {
        MultiReactor mr = new MultiReactor(10393);
        mr.start();
    }
}
