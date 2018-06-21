package app;

import java.awt.EventQueue;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.UIManager;

import assembler.Assembler;

import computers.ComputerAC;
import computers.ComputerBen;

import gui.MainFrame;

import simulator.Computer;

import utils.Logger;
import utils.Utils;

public class Console {

    private static final String CLI_HELP = Info.NAME + " Help\n"
        + "set-mem-start     set memory start address\n"
        + "mem               show memory. `mem connect` to keep connected to memory updates\n"
        + "reg               show registers. `reg connect` to keep connected to register updates\n"
        + "log               show logs. `log connect` to keep connected to logs updates\n"
        + "all               show memory, register, and logs. `all connect` to keep connected updates\n"
        + "arch-help         help for the current computer architecture\n"
        + "set-arch          selects the computer architecture [AC, BEN]\n"
        + "load              load a file\n"
        + "load-type         load a file after selecting the type (e.g. `load-type AC file`)\n"
        + "reload            reload the file\n"
        + "clear             halt the computer and clear everything\n"
        + "clear-mem         clears the memory\n"
        + "clear-reg         clears the registers\n"
        + "clear-io          clears the I/O\n"
        + "run               run the computer\n"
        + "tick, t           tick the computer clock\n"
        + "stop, halt, hlt   halt the computer\n"
        + "terminal, trm     start the I/O terminal\n"
        + "about             program details\n"
        + "update            update the program\n"
        + "help, h, ?        show this help\n"
        + "exit, e           exit the program\n";

    private static final String CMD_HELP = "Usage: java -jar scs.jar [options] [file]\n"
        + "Options:\n"
        + "  -a, --architecture=ARCH        selects the computer archetecture [AC, BEN]\n"
        + "  -t, --file-type=TYPE           selects the input file type [ASM (default), HEX, BIN, DEC]\n"
        + "  -asm, --assemble               only assemble and output the result\n"
        + "  -asmf, --assembly-format=TYPE  selects the assembly output format [HEX (default), BIN, DEC]\n"
        + "  -nw, --no-window-system        use command line interface\n"
        + "  -m, --memory-location=ADDRESS  quit after finishing execution and only print that memory location\n"
        + "  -q, --quiet                    quit after finishing execution\n"
        + "  --server=PORT                  start in server mode\n"
        + "  --client=PORT [CMD]            start in client mode\n"
        + "  -v, --version                  output version information and exit\n"
        + "  -h, -?, --help                 display this help and exit\n";

    Computer c;
    Logger logger;
    Computer.Formatter formatter;
    int[] m;
    boolean trmLock = false;
    boolean promp = true;

    int width;
    int height;
    Scanner in;
    PrintStream out;

    String filePath = null;
    char fileType = 'a';
    boolean gui = true;
    boolean q = false;
    int showMem = -1;
    char asm = 'n';

    public Console(String[] args, Scanner in, PrintStream out) {
        this(null, args, in, out);
    }

    public Console(Computer computer, String[] args, Scanner in, PrintStream out) {
        this(true, computer, args, in, out);
    }

    public Console(boolean run, Computer computer, String[] args, Scanner in, PrintStream out) {

        setSize();
        this.c = computer;
        this.in = in;
        this.out = out;

        String o;
        for (int i = 0; i < args.length; ++i) {
            o = args[i];
            if (o.charAt(0) == '-') {
                o = o.substring(1);
                switch (o) {
                case "-server":
                    try {
                        int p = Integer.parseInt(args[++i]);
                        new Server(p);
                    } catch (Exception e) {
                        wrongInput();
                    }
                    return;
                case "-client":
                    try {
                        int p = Integer.parseInt(args[++i]);
                        String cmd;
                        try {
                            cmd = args[++i];
                        } catch (Exception e) {
                            cmd = null;
                        }
                        int sArgc = args.length - i;
                        if (sArgc < 1) sArgc = 1;
                        String[] sArgs = new String[sArgc];
                        ++i;
                        sArgs[0] = "-nw";
                        for (int j = i, k = 1; j < args.length; ++j, ++k) {
                            sArgs[k] = args[j];
                        }
                        new Client("localhost", p, cmd, sArgs);
                    } catch (Exception e) {
                        wrongInput();
                    }
                    return;
                case "-architecture":
                case "a":
                    if (computer != null) break;
                    switch (args[++i].toUpperCase()) {
                    case "AC":
                        c = new ComputerAC();
                        break;
                    case "BEN":
                        c = new ComputerBen();
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
            c = new ComputerAC();
        m = c.getMemory();
        logger = c.getLogger();
        formatter = c.getFormatter();

        if (run) run();

    }

    public void run() {

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
                c.start();
                out.println(m[showMem]);
            } else {
                c.startEnable();
                cli(!q);
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

    public Computer getComputer() {
        return c;
    }

    private void cli(boolean keepRunning) {
        try {
            String input;
            boolean exit;
            //if (promp) out.print("> ");
            while (c.isRunning() || keepRunning) {
                input = in.nextLine();
                if (input.startsWith("__DIM")) {
                    try {
                        String[] sp = input.split(" ");
                        width = Integer.parseInt(sp[1]);
                        height = Integer.parseInt(sp[2]);
                    } catch (Exception ignored) {}
                } else {
                    //if (promp) out.print("> ");
                    promp = true;
                    exit = exec(input);
                    if (exit) break;
                }
            }
        } catch (NoSuchElementException ignored) {
        }
    }

    public boolean exec(String cmd) {
        if (cmd.contains(";")) {
            String[] cmds = cmd.split(";");
            boolean res = false;
            boolean ret;
            for (String c : cmds) {
                ret = exec(c);
                if (ret)
                    res = true;
            }
            return res;
        }
        String[] input = cmd.trim().split(" ");
        if (input.length == 0) return false;
        switch (input[0]) {
        case "":
            break;
        case "set-mem-start":
            try {
                c.mStart = Integer.parseInt(input[1]);
                int max = m.length - height + 4;
                if (c.mStart < 0) {
                    c.mStart = 0;
                } else if (c.mStart > max) {
                    c.mStart = max;
                }
            } catch (NumberFormatException e) {
                c.mStart = 0;
                out.println(CLI_HELP);
            }
            break;
        case "mem":
            out.print(formatter.getMemory(c.mStart, width, height));
            if (input.length == 2 && input[1].equals("connect")) {
                promp = false;
                c.connectOnUpdate(f -> out.print(formatter.getMemory(c.mStart, width, height)));
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
            out.print(formatter.getAll(c.mStart, width, height));
            if (input.length == 2 && input[1].equals("connect")) {
                promp = false;
                c.connectOnUpdate(f -> out.print(formatter.getAll(c.mStart, width, height)));
            }
            break;
        case "log":
            for (int i = 0; i < logger.size(); ++i) {
                out.println(logger.get(i));
            }
            if (input.length == 2 && input[1].equals("connect")) {
                promp = false;
                logger.connect(log -> out.println(log));
            }
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
                c = new ComputerAC();
                break;
            case "BEN":
                c = new ComputerBen();
                break;
            }
            m = c.getMemory();
            logger = c.getLogger();
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
            for (int i = 0; i < height; ++i)
                out.println();
            c.connectOnOut(ch -> {
                    trmLock = true;
                    if (c.isIoCleared()) {
                        for (int i = 0; i < height; ++i)
                            out.println();
                    } else if (ch != '\0') {
                        out.print(ch);
                    }
                    trmLock = false;
                });
            String line;
            while ((line = in.nextLine()) != null) {
                if (trmLock || line.startsWith("__DIM")) continue;
                c.putInpStr(line);
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
            return true;
        default:
            out.println("Invalid input");
        }
        return false;
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
