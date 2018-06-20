package app;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import computers.ComputerAC;
import simulator.Computer;
import ui.Console;

import utils.Logger;

public class Server {

    static final String[] ARGS = { "-nw" };

    int port;
    ServerSocket socket;
    Computer c;

    public Server(int port) {
        this.port = port;
        this.c = new ComputerAC(new Logger()); // TODO
        try {
            socket = new ServerSocket(port);
            for (;;) {
                Socket client = socket.accept();
                new Thread(() -> {
                        try {
                            new Console(c, ARGS, new Scanner(client.getInputStream()), new PrintStream(client.getOutputStream()));
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                }).start();
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }
}






