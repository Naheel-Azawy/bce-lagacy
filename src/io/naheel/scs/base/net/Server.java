package io.naheel.scs.base.net;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import io.naheel.scs.base.Console;
import io.naheel.scs.base.simulator.Computer;

public class Server {

    int port;
    ServerSocket socket;
    Computer[] cPtr = new Computer[1];

    public Server(int port) {
        this.port = port;
        try {
            socket = new ServerSocket(port);
            for (;;) {
                Socket client = socket.accept();
                new Thread(() -> {
                        try {
                            Scanner s = new Scanner(client.getInputStream());
                            PrintStream p = new PrintStream(client.getOutputStream());
                            String[] args = s.nextLine().split("`");
                            Console con = new Console(false, cPtr, args, s, p);
                            con.run();
                        } catch (IOException e) {
                            System.err.println(e.toString());
                        }
                }).start();
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    System.err.println(e.toString());
                }
            }
        }
    }

}






