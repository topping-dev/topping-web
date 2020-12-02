package com.dk.scriptingengine.osspecific;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

public class Log
{
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    public static int v(String tag, String message)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLACK)
                .build();
        cp.println("V/" + tag + ":" + message);
        cp.clear();
        return 0;
    }

    public static int d(String tag, String message)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.GREEN).background(Ansi.BColor.BLACK)
                .build();
        cp.println("D/" + tag + ":" + message);
        cp.clear();
        return 0;
    }

    public static int i(String tag, String message)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.YELLOW).background(Ansi.BColor.BLACK)
                .build();
        cp.println("I/" + tag + ":" + message);
        cp.clear();
        return 0;
    }

    public static int w(String tag, String message)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.BLUE).background(Ansi.BColor.BLACK)
                .build();
        cp.println("W/" + tag + ":" + message);
        cp.clear();
        return 0;
    }

    public static int w(String tag, Exception e)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.BLUE).background(Ansi.BColor.BLACK)
                .build();
        cp.println("W/" + tag + ":" + e.getMessage());
        cp.clear();
        return 0;
    }

    public static int e(String tag, String message)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.RED).background(Ansi.BColor.BLACK)
                .build();
        cp.println("E/" + tag + ":" + message);
        cp.clear();
        return 0;
    }

    public static int e(String tag, String message, Exception e)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.RED).background(Ansi.BColor.BLACK)
                .build();
        cp.println("E/" + tag + ":" + message);
        cp.println(e.getMessage());
        cp.clear();
        return 0;
    }
}
