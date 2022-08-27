package com.huangydyn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class BioServer {

    //默认的端口号
    private static int DEFAULT_PORT = 8000;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            System.out.println("监听来自于" + DEFAULT_PORT + "的端口信息");
            serverSocket = new ServerSocket(DEFAULT_PORT);
            while (true) {
                String id = UUID.randomUUID().toString();
                System.out.println("开始等待, 请求: " + id);
                Socket socket = serverSocket.accept();
                System.out.println("收到连接, 请求: " + id);
                Thread.sleep(10000);

                // worker线程处理IO读写，不阻塞读连接
                SocketServerThread socketServerThread = new SocketServerThread(socket);
                System.out.println("建立线程处理, 请求: " + id);
                new Thread(socketServerThread).start();
            }
        } catch (Exception e) {

        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        //这个wait不涉及到具体的实验逻辑，只是为了保证守护线程在启动所有线程后，进入等待状态
        synchronized (BioServer.class) {
            try {
                BioServer.class.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
