package app;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import computers.ComputerAC;
import simulator.Computer;

import utils.Logger;

public class Server {

    int port;
    ServerSocket socket;
    Computer c;

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
                            Console con = new Console(false, c, args, s, p);
                            if (c == null)
                                c = con.getComputer();
                            con.run();
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






