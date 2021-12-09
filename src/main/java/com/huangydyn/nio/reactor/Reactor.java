package com.huangydyn.nio.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Reactor implements Runnable {
    final Selector selector;

    final ServerSocketChannel serverChannel;

    static final int WORKER_POOL_SIZE = 10;

    static ExecutorService workerPool;

    Reactor(int port) throws IOException {
        System.out.println("[Reactor] created");
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        // Register the server socket channel with interest-set set to ACCEPT operation
        SelectionKey sk = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    public void run() {
        try {
            while (true) {
                selector.select();
                Iterator it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    // 若是连接事件获取是acceptor
                    // 若是IO读写事件获取是handler
                    dispatch(it);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void dispatch(Iterator it) {
        SelectionKey sk = (SelectionKey) it.next();
        it.remove();
        Runnable r = (Runnable) sk.attachment();
        if (r != null) {
            System.out.println("[Reactor] dispatch " + r.getClass() + ", threadId" + Thread.currentThread().getId());
            r.run();
        }
    }

    class Acceptor implements Runnable {
        public void run() {
            try {
                System.out.println("[Acceptor] created , threadId " + Thread.currentThread().getId());
                SocketChannel channel = serverChannel.accept();
                if (channel != null)
                    new Handler(selector, channel);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        workerPool = Executors.newFixedThreadPool(WORKER_POOL_SIZE);

        try {
            new Thread(new Reactor(8003)).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}