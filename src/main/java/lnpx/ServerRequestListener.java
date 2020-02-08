package lnpx;

import java.io.*;
import java.net.*;

public class ServerRequestListener extends Thread {

    private final static int port = 7799;
    private static ServerSocket socketListener;

    @Override
    public void run() {
        System.out.println("Server starting now...");
        try {
            socketListener = new ServerSocket(port, 10);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        System.out.println("Ready.");
        while (true) {
            try {
                Socket socket = socketListener.accept();
                ServerWorker worker = new ServerWorker(socket);
                worker.setDaemon(true);
                worker.start();
                System.out.println("Accepted new request from " + socket.getInetAddress().getHostAddress());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }
}
