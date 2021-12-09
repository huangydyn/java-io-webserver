package com.huangydyn.nio.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class Handler implements Runnable {
    final SocketChannel channel;

    final SelectionKey selKey;

    static final int READ_BUF_SIZE = 1024;

    static final int WRiTE_BUF_SIZE = 1024;

    ByteBuffer readBuf = ByteBuffer.allocate(READ_BUF_SIZE);

    ByteBuffer writeBuf = ByteBuffer.allocate(WRiTE_BUF_SIZE);

    Handler(Selector sel, SocketChannel sc) throws IOException {
        System.out.println("[Handler] created");
        channel = sc;
        channel.configureBlocking(false);

        // Register the socket channel with interest-set set to READ operation
        selKey = channel.register(sel, SelectionKey.OP_READ);
        selKey.attach(this);
        selKey.interestOps(SelectionKey.OP_READ);
        sel.wakeup();
    }

    public void run() {
        try {
            if (selKey.isReadable())
                read();
            else if (selKey.isWritable())
                write();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    synchronized void read() throws IOException {
        int numBytes;

        try {
            numBytes = channel.read(readBuf);
            System.out.println("[Handler] read(): #bytes read into 'readBuf' buffer = " + numBytes + ", threadId" + Thread.currentThread().getId());

            if (numBytes == -1) {
                selKey.cancel();
                channel.close();
                System.out.println("[Handler] read(): client connection might have been dropped!");
            } else {
                Future<String> responseFuture = Reactor.workerPool.submit(() -> {
                    System.out.println("[Handler] worker thread execute, " + ", threadId" + Thread.currentThread().getId());
                    return "hello";
                });
                selKey.attach(this);
                selKey.interestOps(SelectionKey.OP_WRITE);
                selKey.selector().wakeup();
                // write response
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }

    void write() throws IOException {
        int numBytes = 0;

        try {
            numBytes = channel.write(writeBuf);
            System.out.println("[Handler] write(): #bytes read from 'writeBuf' buffer = " + numBytes);

            if (numBytes > 0) {
                readBuf.clear();
                writeBuf.clear();

                // Set the key's interest-set back to READ operation
                selKey.interestOps(SelectionKey.OP_READ);
                selKey.selector().wakeup();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}