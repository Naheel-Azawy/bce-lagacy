package io.naheel.scs;

import java.util.Scanner;

import io.naheel.scs.base.Console;

public class Main {

    public static void main(String[] args) {
        Updater.dealWithIt();
        new Console(args, new Scanner(System.in), System.out);
    }

}
