package io.naheel.scs.base.computers;

import io.naheel.scs.base.simulator.Computer;

public class Computers {

    // Mainly used inside help strings
    public static final String STR = "AC, BEN";

    public static final String[] NAMES = {ComputerAC.NAME, ComputerBen.NAME};

    public static final String[] NAMES_SHORT = {"AC", "BEN"};

    public static Computer strToComputer(String str) {
        switch (str.toUpperCase()) {
        case "AC":  return new ComputerAC();
        case "BEN": return new ComputerBen();
        }
        return null;
    }

}
