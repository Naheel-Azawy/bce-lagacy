package app;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Info {

    public static final String NAME = "Basic Computer Emulator";
    public static final String VERSION = "1.0";
    public static final Image ICON = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("ic.png")).getImage();
    public static final String ABOUT = "<html><body>" + NAME + "<br>" + VERSION
            + "<br>Copyright (C) 2017-2018 Naheel Azawy<br>" + "This program comes with absolutely no warranty.<br>"
            + "See the <a href=\"http://www.gnu.org/licenses/\">GNU General Public License, version 3 or later</a> for details."
            + "</body></html>";

}