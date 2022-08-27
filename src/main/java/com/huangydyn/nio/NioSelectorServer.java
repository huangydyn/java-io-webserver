package com.huangydyn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.UUID;

public class NioSelectorServer {

    public static void main(String[] args) throws IOException {
        // 1、获取Selector选择器
        Selector selector = Selector.open();

        // 2、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 4、绑定连接
        serverSocketChannel.bind(new InetSocketAddress(8001));

        // 5、将通道注册到选择器上,并注册的操作为：“接收”操作
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 6、采用轮询的方式，查询获取“准备就绪”的注册过的操作
        while (true) {
            // 阻塞
            if (selector.select() == 0) {
                continue;
            }

            // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的操作”）
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {

                // 8、获取“准备就绪”的时间
                SelectionKey selectedKey = selectedKeys.next();

                // 9、判断key是具体的什么事件
                if (selectedKey.isAcceptable()) {
                    accept(selector, serverSocketChannel);
                } else if (selectedKey.isReadable()) {
                    process(selectedKey);
                }

                // 16、移除选择键
                selectedKeys.remove();
            }
        }
    }

    private static void process(SelectionKey selectedKey) throws IOException {
        String id = UUID.randomUUID().toString();
        System.out.println("收到连接, 请求: " + id);

        System.out.println("开始处理, 请求: " + id);

        // 13、获取该选择器上的“读就绪”状态的通道
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

        // 14、读取数据
        ByteBuffer readBuffer = ByteBuffer.allocate(256);
        socketChannel.read(readBuffer);
        String result = new String(readBuffer.array()).trim();
        System.out.println("from 客户端: 读取数据: " + result);

        // 15. 写入数据
        ByteBuffer writeBuffer = ByteBuffer.wrap("hello".getBytes());
        socketChannel.write(writeBuffer);
        writeBuffer.rewind();
        socketChannel.close();
    }

    private static void accept(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        // 10、若接受的事件是“接收就绪” 操作,就获取客户端连接
        SocketChannel socketChannel = serverSocketChannel.accept();
        // 11、切换为非阻塞模式
        socketChannel.configureBlocking(false);
        // 12、将该通道注册到selector选择器上
        socketChannel.register(selector, SelectionKey.OP_READ);

        System.out.println("from 客户端: 接收连接");
    }
}
