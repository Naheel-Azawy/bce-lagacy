package io.naheel.scs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class Updater {

    private static final String EXTRA = "__tmp";

    private static String nameWithExtra(String name) {
        int slash = name.lastIndexOf(File.separator) + 1;
        String path;
        if (slash != -1) {
            path = name.substring(0, slash);
            name = name.substring(slash);
        } else {
            path = "";
        }
        int dot = name.lastIndexOf('.');
        if (dot == -1)
            return name;
        name = "." + name.substring(0, dot) + EXTRA + name.substring(dot);
        return path + name;
    }

    private static String nameWithoutExtra(String name) {
        int slash = name.lastIndexOf(File.separator) + 1;
        String path;
        if (slash != -1) {
            path = name.substring(0, slash);
            name = name.substring(slash);
        } else {
            path = "";
        }
        name = name.substring(1).replace(EXTRA, "");
        return path + name;
    }

    private static boolean nameIsWithExtra(String name) {
        int slash = name.lastIndexOf(File.separator) + 1;
        if (slash != -1)
            name = name.substring(slash);
        int dot = name.lastIndexOf('.');
        if (dot == -1)
            return false;
        name = name.substring(0, dot);
        return name.charAt(0) == '.' && name.endsWith(EXTRA);
    }

    public static void dealWithIt() { // This will delete garbage from updater
        String n = getPathName();
        if (!n.endsWith(".jar") && !n.endsWith(".exe"))
            return;
        if (nameIsWithExtra(n)) {
            try {
                String newName = nameWithoutExtra(n);
                Files.copy(new File(n).toPath(), new File(newName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                try {
                    Runtime.getRuntime().exec("attrib -H " + newName);
                } catch (Exception ignored) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File old = new File(nameWithExtra(n));
            if (old.exists())
                old.delete();
        }
    }

    public static double getLatestVersion() {
        double res = -1;
        try {
            Scanner in = new Scanner(URI
                    .create("https://raw.githubusercontent.com/Naheel-Azawy/Simple-Computer-Simulator/master/changelog")
                    .toURL().openStream());
            res = in.nextDouble();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String download() {
        String url;
        String fileName;
        String pathName = getPathName();
        boolean isWin = isWin();
        boolean isExe = false;
        if (pathName.endsWith(".jar")) {
            fileName = pathName;
        } else if (pathName.endsWith(".exe")) {
            fileName = pathName;
            isExe = true;
        } else if (isWin) {
            fileName = "scs.exe";
            isExe = true;
        } else {
            fileName = "scs.jar";
        }
        if (isExe) {
            url = "https://raw.githubusercontent.com/Naheel-Azawy/Simple-Computer-Simulator/master/extra/scs.exe";
        } else {
            url = "https://raw.githubusercontent.com/Naheel-Azawy/Simple-Computer-Simulator/master/scs.jar";
        }
        fileName = nameWithExtra(fileName);
        File f = new File(fileName);
        if (f.exists())
            f.delete();
        try {
            InputStream in = URI.create(url).toURL().openStream();
            Files.copy(in, Paths.get(fileName));
            try {
                Runtime.getRuntime().exec("attrib +H " + fileName);
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public static void run(String fileName) {
        ProcessBuilder builder;
        if (isWin()) {
            if (fileName.endsWith(".exe")) {
                builder = new ProcessBuilder("cmd", "/c", fileName);
            } else {
                builder = new ProcessBuilder("cmd", "/c", "java", "-jar", fileName);
            }
        } else {
            builder = new ProcessBuilder("java", "-jar", fileName);
        }
        builder.redirectErrorStream(true);
        try {
            builder.start();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isWin() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String getPathName() {
        return new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
    }

}
