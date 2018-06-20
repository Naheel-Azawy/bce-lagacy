package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

    // TODO: client send console dimentions to server

    Socket socket;
    InputStreamReader in;
    PrintStream out;
    BufferedReader inCl;
    PrintStream outCl;
    boolean closed = false;

    public Client(String host, int port) {

        try {
            socket = new Socket(host, port);

            in = new InputStreamReader(socket.getInputStream());
            out = new PrintStream(socket.getOutputStream(), true);
            inCl = new BufferedReader(new InputStreamReader(System.in));
            outCl = System.out;

            new Thread(() -> {
                    char c;
                    try {
                        for (;;) {
                            c = (char) in.read();
                            if (c == '\0') {
                                outCl.println("Server closed");
                                closed = true;
                                break;
                            }
                            outCl.print(c);
                        }
                    } catch (IOException e) {
                        System.err.println(e);
                    }
            }).start();

            String line;
            while (!closed) {
                line = inCl.readLine();
              	if (line == null) break;
                out.println(line);
            }

        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                if (socket != null)
                    socket.close();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

}
