package ui;

import java.awt.EventQueue;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.UIManager;

import app.Info;
import assembler.Assembler;

import computers.ComputerAC;
import computers.ComputerBen;

import gui.MainFrame;

import simulator.Computer;

import utils.Logger;
import utils.Utils;

public class Console {

    private static final String CLI_HELP = Info.NAME + " Help\n" + "Enter:\t\tnext clock\n" + "h,?:\t\thelp\n" + "q:\t\tquit\n" + "savelog:\tsave log file\n";

    private static final String CMD_HELP = "Usage: java -jar scs.jar [options] [file]\n" + "Options:\n"
        + "  -d: file is decimal text file\n" + "  -x: file is hexadecimal text file\n"
        + "  -b: file is binary text file\n" + "  -nogui: use command line interface\n"
        + "  -m: quit after finishing execution and only print that memory location\n"
        + "  -tick: Press 'Enter' to move to next clock cycle (only with -nogui)\n"
        + "  -q: quit after finishing execution\n" + "  -v: output version information and exit\n"
        + "  -h,?: display this help and exit\n";

    Computer c;
    Logger logger;
    Computer.Formatter formatter;
    int[] m;
    int memSize;
    int lines = 10;
    int showMem = -1;

    int width;
    int height;
    Scanner in;
    PrintWriter out;

    public Console(String[] args, Scanner in, PrintWriter out) {
        this.width = Utils.getTerminalCols();
        this.height = Utils.getTerminalLines();
        this.in = in;
        this.out = out;

        logger = new Logger();
        c = new ComputerBen(logger); // TODO
        m = c.getMemory();
        memSize = m.length;

        String filePath = null;
        char fileType = 'a';
        boolean gui = true;
        boolean tick = false;
        boolean q = false;
        boolean ben = false;

        String o;
        for (int i = 0; i < args.length; ++i) {
            o = args[i];
            if (o.charAt(0) == '-') {
                o = o.substring(1);
                switch (o) {
                case "ben":
                    ben = true;
                    break;
                case "d":
                case "x":
                case "b":
                    if (fileType != 'a')
                        wrongInput();
                    fileType = o.charAt(0);
                    break;
                case "nogui":
                    gui = false;
                    break;
                case "m":
                    try {
                        showMem = Integer.parseInt(args[++i]);
                        gui = false;
                        q = true;
                        tick = false;
                        if (showMem < 0 || showMem >= memSize) {
                            out.println("Memory location should be between 0 and " + (memSize - 1));
                            System.exit(1);
                        }
                    } catch (Exception e) {
                        wrongInput();
                    }
                    break;
                case "tick":
                    tick = true;
                    break;
                case "q":
                    q = true;
                    break;
                case "v":
                    out.println(Info.NAME + " version " + Info.VERSION);
                    System.exit(0);
                    break;
                case "h":
                case "?":
                    out.println(CMD_HELP);
                    System.exit(0);
                    break;
                default:
                    wrongInput();
                    break;
                }
            } else {
                filePath = o;
            }
        }

        if (ben) {
            int[] arr = Assembler.assemble(ComputerBen.INSTURCTION_SET, Utils.readFile(filePath).split("\n"));
            for (int w : arr)
                out.println(Integer.toBinaryString(w));
            return;
        }

        if (filePath != null) {
            switch (fileType) {
            case 'a':
                c.loadProgramFile(Computer.TYPE_ASM, filePath);
                break;
            case 'd':
                c.loadProgramFile(Computer.TYPE_DEC, filePath);
                break;
            case 'x':
                c.loadProgramFile(Computer.TYPE_HEX, filePath);
                break;
            case 'b':
                c.loadProgramFile(Computer.TYPE_BIN, filePath);
                break;
            }
        }
        if (gui) {
            startGui();
        } else {
            if (!tick)
                c.start();
            if (showMem != -1)
                out.println(m[showMem]);
            else {
                c.startEnable();
                runEveryClock(!q);
            }
        }
    }

    private void wrongInput() {
        out.println(CMD_HELP);
        System.exit(1);
    }

    private void runEveryClock(boolean keepRunning) {
        c.tick();
        int mStart = 0;
        displayState(mStart);
        String input;
        loop: while (c.isRunning() || keepRunning) {
            switch (input = in.nextLine()) {
            case "":
                c.tick();
                displayState(mStart);
                break;
            case "h":
            case "?":
                out.println(CLI_HELP);
                break;
            case "savelog":
                out.print("save path: ");
                String path = in.nextLine();
                try {
                    logger.save(path);
                    out.println("Saved");
                } catch (Exception e) {
                    out.println("Error saving log file");
                }
                break;
            case "q":
                break loop;
            default:
                try {
                    mStart = Integer.parseInt(input);
                    displayState(mStart);
                } catch (NumberFormatException e) {
                    mStart = 0;
                    out.println(CLI_HELP);
                }
            }
        }
        in.close();
    }

    private void displayState(int mStart) {
        try {
            out.print(formatter.getAll(mStart, -1, lines = Utils.getTerminalLines()));
        } catch (IndexOutOfBoundsException ie) {
            int max = memSize - lines + 4;
            mStart = mStart > max ? max : 0;
            displayState(mStart);
        }
    }

    public void startGui() {
        EventQueue.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    new MainFrame(c).setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

}
