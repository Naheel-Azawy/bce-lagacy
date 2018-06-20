package ui;

import java.awt.EventQueue;
import java.io.PrintStream;
import java.util.Scanner;

import javax.swing.UIManager;

import app.Client;
import app.Info;
import app.Server;
import app.Updater;

import assembler.Assembler;

import computers.ComputerAC;
import computers.ComputerBen;

import gui.MainFrame;

import simulator.Computer;

import utils.Logger;
import utils.Utils;

public class Console {

    private static final String CLI_HELP = Info.NAME + " Help\n" + "Enter:\t\tnext clock\n" + "h,?:\t\thelp\n" + "q:\t\tquit\n" + "savelog:\tsave log file\n"; // TODO: update this help

    private static final String CMD_HELP = "Usage: java -jar scs.jar [options] [file]\n"
        + "Options:\n"
        + "  -a, --architecture        selects the computer archetecture [AC, BEN]\n"
        + "  -t, --file-type           selects the input file type [ASM (default), HEX, BIN, DEC]\n"
        + "  -asm, --assemble          only assemble and output the result\n"
        + "  -asmf, --assembly-format  selects the assembly output format [HEX (default), BIN, DEC]\n"
        + "  -nw, --no-window-system   use command line interface\n"
        + "  -m, --memory-location     quit after finishing execution and only print that memory location\n"
        + "  -q, --quiet               quit after finishing execution\n"
        + "  -v, --version             output version information and exit\n"
        + "  -h, -?, --help            display this help and exit\n"; // TODO: update for server and client

    Computer c;
    Logger logger;
    Computer.Formatter formatter;
    int[] m;
    int mStart = 0;
    int trmI = 0;
    boolean trmLock = false;

    int width;
    int height;
    Scanner in;
    PrintStream out;

    public Console(String[] args, Scanner in, PrintStream out) {
        this(null, args, in, out);
    }

    public Console(Computer computer, String[] args, Scanner in, PrintStream out) {

        setSize();
        this.c = computer;
        this.in = in;
        this.out = out;
        this.logger = c == null ? new Logger() : c.getLogger();

        String filePath = null;
        char fileType = 'a';
        boolean gui = true;
        boolean q = false;
        int showMem = -1;
        char asm = 'n';

        String o;
        for (int i = 0; i < args.length; ++i) {
            o = args[i];
            if (o.charAt(0) == '-') {
                o = o.substring(1);
                switch (o) {
                case "-server":
                    try {
                        new Server(Integer.parseInt(args[++i]));
                    } catch (Exception e) {
                        wrongInput();
                    }
                    return;
                case "-client":
                    try { // TODO: receive arguments for the client here
                        new Client("localhost", Integer.parseInt(args[++i]));
                    } catch (Exception e) {
                        wrongInput();
                    }
                    return;
                case "-architecture":
                case "a":
                    if (computer != null) break;
                    switch (args[++i].toUpperCase()) {
                    case "AC":
                        c = new ComputerAC(logger);
                        break;
                    case "BEN":
                        c = new ComputerBen(logger);
                        break;
                    }
                    break;
                case "-file-type":
                case "t":
                    switch (args[++i].toUpperCase()) {
                    case "ASM":
                        fileType = 'a';
                        break;
                    case "HEX":
                        fileType = 'x';
                        break;
                    case "BIN":
                        fileType = 'b';
                        break;
                    case "DEC":
                        fileType = 'd';
                        break;
                    default:
                        wrongInput();
                    }
                    break;
                case "-assemble":
                case "asm":
                    asm = 'x';
                    break;
                case "-assembly-format":
                case "asmf":
                    switch (args[++i].toUpperCase()) {
                    case "HEX":
                        asm = 'x';
                        break;
                    case "BIN":
                        asm = 'b';
                        break;
                    case "DEC":
                        asm = 'd';
                        break;
                    default:
                        wrongInput();
                    }
                    break;
                case "-no-window-system":
                case "nw":
                    gui = false;
                    break;
                case "-memory-location":
                case "m":
                    try {
                        showMem = Integer.parseInt(args[++i]);
                        gui = false;
                        q = true;
                    } catch (Exception e) {
                        wrongInput();
                    }
                    break;
                case "-quiet":
                case "q":
                    q = true;
                    break;
                case "-version":
                case "v":
                    out.println(Info.NAME + " version " + Info.VERSION);
                    System.exit(0);
                    break;
                case "-help":
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
                if (filePath != null) {
                    wrongInput("Multiple files not allowed");
                }
                filePath = o;
            }
        }

        if (c == null)
            c = new ComputerAC(logger);
        m = c.getMemory();
        formatter = c.getFormatter();

        if (asm != 'n') {
            int[] arr = Assembler.assemble(c.getInstructionSet(), Utils.readFile(filePath).split("\n"));
            switch (asm) {
            case 'x':
                for (int w : arr)
                    out.println(Integer.toHexString(w));
                break;
            case 'b':
                for (int w : arr)
                    out.println(Integer.toBinaryString(w));
                break;
            case 'd':
                for (int w : arr)
                    out.println(w);
                break;
            }
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
            if (showMem != -1) {
                if (showMem >= m.length) {
                    out.println("Memory location should be between 0 and " + (m.length - 1));
                    System.exit(1);
                }
                out.println(m[showMem]);
            } else {
                c.startEnable();
                cli(null, !q);
            }
        }
    }

    public void setSize() {
        this.width = Utils.getTerminalCols();
        this.height = Utils.getTerminalLines();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private void wrongInput(String msg) {
        out.println(msg);
        out.println(CMD_HELP);
        System.exit(1);
    }

    private void wrongInput() {
        out.println(CMD_HELP);
        System.exit(1);
    }

    private void cli(String initCommand, boolean keepRunning) {
        boolean promp = true;
        String[] input;
        loop: while (c.isRunning() || keepRunning) {
            if (promp) out.print("> ");
            promp = true;
            if (initCommand == null) {
                input = in.nextLine().trim().split(" ");
            } else {
                input = initCommand.trim().split(" ");
                initCommand = null;
            }
            if (input.length == 0) continue;
            switch (input[0]) {
            case "":
                break;
            case "set-mem-start":
                try {
                    mStart = Integer.parseInt(input[0]);
                    int max = m.length - height + 4;
                    if (mStart < 0) {
                        mStart = 0;
                    } else if (mStart > max) {
                        mStart = max;
                    }
                } catch (NumberFormatException e) {
                    mStart = 0;
                    out.println(CLI_HELP);
                }
                break;
            case "mem":
                out.print(formatter.getMemory(mStart, width, height));
                if (input.length == 2 && input[1].equals("connect")) {
                    promp = false;
                    c.connectOnUpdate(f -> out.print(formatter.getMemory(mStart, width, height)));
                }
                break;
            case "reg":
                out.print(formatter.getRegisters(width, height));
                if (input.length == 2 && input[1].equals("connect")) {
                    promp = false;
                    c.connectOnUpdate(f -> out.print(formatter.getRegisters(width, height)));
                }
                break;
            case "all":
                out.print(formatter.getAll(mStart, width, height));
                if (input.length == 2 && input[1].equals("connect")) {
                    promp = false;
                    c.connectOnUpdate(f -> out.print(formatter.getAll(mStart, width, height)));
                }
                break;
            case "log-connect":
                promp = false;
                logger.connect(log -> out.println(log));
                break;
            case "arch-help":
                out.print(c.toString());
                break;
            case "set-arch":
                if (input.length != 2) {
                    out.println("Please specify one architecture [AC, BEN]");
                    break;
                }
                switch (input[1].toUpperCase()) {
                case "AC":
                    c = new ComputerAC(logger);
                    break;
                case "BEN":
                    c = new ComputerBen(logger);
                    break;
                }
                m = c.getMemory();
                formatter = c.getFormatter();
                break;
            case "load":
                if (input.length == 2) {
                    c.loadProgramFile(Computer.TYPE_ASM, input[1]);
                } else {
                    out.println("Enter a valid file");
                }
                break;
            case "load-type":
                if (input.length == 3) {
                    int fileType = -1;
                    switch (input[1]) {
                    case "ASM":
                        fileType = Computer.TYPE_ASM;
                        break;
                    case "HEX":
                        fileType = Computer.TYPE_HEX;
                        break;
                    case "BIN":
                        fileType = Computer.TYPE_BIN;
                        break;
                    case "DEC":
                        fileType = Computer.TYPE_DEC;
                        break;
                    default:
                        out.println("Unknown file type");
                    }
                    if (fileType != -1)
                        c.loadProgramFile(fileType, input[2]);
                } else {
                    out.println("Invalid input");
                }
                break;
            case "reload":
                c.loadProgramFile(c.getSourceType(), c.getSourcePath());
                break;
            case "clear":
                c.stop();
                c.clear();
                logger.clear();
                c.clearIO();
                break;
            case "clear-mem":
                c.clearMem();
                break;
            case "clear-reg":
                c.clearReg();
                break;
            case "clear-io":
                c.clearIO();
                break;
            case "run":
                c.clearReg();
                c.clearIO();
                c.startAsync();
                break;
            case "tick":
            case "t":
                c.startEnable();
                c.tickAsync();
                break;
            case "stop":
            case "halt":
            case "hlt":
                c.stop();
                break;
            case "set-freq":
                try {
                    c.setFrequency(Integer.parseInt(input[1]));
                } catch (Exception e) {
                    out.println("Enter a valid frequency");
                }
                break;
            case "avg-freq":
                out.println(c.getAvgFrequency());
                break;
            case "save-log":
                try {
                    logger.save(input[1]);
                    out.println("Saved");
                } catch (Exception e) {
                    out.println("Error saving log file");
                }
                break;
            case "about":
                out.println(Info.ABOUT);
                break;
            case "update":
                double latest = Updater.getLatestVersion();
                if (latest < 0) {
                    out.println("Checking for updates failed!");
                } else {
                    if (latest > Double.parseDouble(Info.VERSION)) {
                        out.print("Would like to update to the latest version (" + latest + ")?");
                    } else {
                        out.print("Already up to date at version (" + latest + ")!\nWould you like to force update?");
                    }
                    out.print(" (y/n): ");
                    String i = in.nextLine().toLowerCase();
                    if (i.equals("y")) {
                        out.println("Downloading..");
                        Updater.download();
                        out.println("Finished downloading!");
                    } else if (!i.equals("n")) {
                        out.println("Invalid input");
                    }
                }
                break;
            case "terminal":
            case "trm":
                c.connectOnOut(ch -> {
                        trmLock = true;
                        if (c.isIoCleared()) {
                            for (int i = 0; i < height; ++i)
                                out.println();
                            trmI = 0;
                        } else if (ch != '\0') {
                            out.print(ch);
                            if (ch == '\n')
                                trmI = 0;
                            else
                                ++trmI;
                        }
                        trmLock = false;
                    });
                String line;
                while ((line = in.nextLine()) != null) {
                    line = line.substring(trmI); // TODO: out of index
                    if (trmLock) continue;
                    c.putInpStr(line);
                    trmI = 0;
                }
                break;
            case "help":
            case "h":
            case "?":
                out.println(CLI_HELP);
                break;
            case "exit":
            case "e":
                out.print('\0');
                break loop;
            default:
                out.println("Invalid input");
            }
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
