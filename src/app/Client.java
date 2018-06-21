package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import utils.Utils;

public class Client {

    Socket socket;
    InputStreamReader in;
    PrintStream out;
    BufferedReader inCl;
    PrintStream outCl;

    public Client(String host, int port, String exec, String[] args) {

        try {
            socket = new Socket(host, port);

            in = new InputStreamReader(socket.getInputStream());
            out = new PrintStream(socket.getOutputStream(), true);
            inCl = new BufferedReader(new InputStreamReader(System.in));
            outCl = System.out;

            new Thread(() -> {
                    int c;
                    try {
                        for (;;) {
                            c = in.read();
                            if (c == 0xFFFFFFFF) {
                                outCl.println("Server closed");
                                System.exit(0);
                            }
                            outCl.print((char) c);
                        }
                    } catch (IOException e) {
                        System.err.println(e);
                    }
            }).start();

            String argsStr = "";
            if (args.length != 0) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append('`');
                }
                argsStr = sb.toString();
            }
            out.println(argsStr);

            sendDim();

            if (exec != null)
                out.println(exec);

            String line;
            while (true) {
                line = inCl.readLine();
              	if (line == null) break;
                sendDim();
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

    private void sendDim() {
        out.printf("__DIM %d %d\n", Utils.getTerminalCols(), Utils.getTerminalLines());
    }

}
