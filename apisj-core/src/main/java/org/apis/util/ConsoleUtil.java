package org.apis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleUtil {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static String readLine(String format, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(format, args);
        }
        System.out.print(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.readLine();
    }

    public static char[] readPassword(String format, Object... args)
            throws IOException {
        if (System.console() != null)
            return System.console().readPassword(format, args);
        return readLine(format, args).toCharArray();
    }

    public static void printBlack(String format, Object... args) {
        System.out.print(ANSI_BLACK + String.format(format, args) + ANSI_RESET);
    }
    public static void printRed(String format, Object... args) {
        System.out.print(ANSI_RED+ String.format(format, args) + ANSI_RESET);
    }
    public static void printGreen(String format, Object... args) {
        System.out.print(ANSI_GREEN + String.format(format, args) + ANSI_RESET);
    }
    public static void printYellow(String format, Object... args) {
        System.out.print(ANSI_YELLOW + String.format(format, args) + ANSI_RESET);
    }
    public static void printBlue(String format, Object... args) {
        System.out.print(ANSI_BLUE + String.format(format, args) + ANSI_RESET);
    }
    public static void printPurple(String format, Object... args) {
        System.out.print(ANSI_PURPLE+ String.format(format, args) + ANSI_RESET);
    }
    public static void printCyan(String format, Object... args) {
        System.out.print(ANSI_CYAN + String.format(format, args) + ANSI_RESET);
    }
    public static void printWhite(String format, Object... args) {
        System.out.print(ANSI_WHITE + String.format(format, args) + ANSI_RESET);
    }

    public static void printlnBlack(String format, Object... args) {
        System.out.println(ANSI_BLACK + String.format(format, args) + ANSI_RESET);
    }
    public static void printlnRed(String format, Object... args) {
        System.out.println(ANSI_RED+ String.format(format, args) + ANSI_RESET);
    }
    public static void printlnGreen(String format, Object... args) {
        System.out.println(ANSI_GREEN + String.format(format, args) + ANSI_RESET);
    }
    public static void printlnYellow(String format, Object... args) {
        System.out.println(ANSI_YELLOW + String.format(format, args) + ANSI_RESET);
    }
    public static void printlnBlue(String format, Object... args) {
        System.out.println(ANSI_BLUE + String.format(format, args) + ANSI_RESET);
    }
    public static void printlnPurple(String format, Object... args) {
        System.out.println(ANSI_PURPLE+ String.format(format, args) + ANSI_RESET);
    }
    public static void printlnCyan(String format, Object... args) {
        System.out.println(ANSI_CYAN + String.format(format, args) + ANSI_RESET);
    }
    public static void printlnWhite(String format, Object... args) {
        System.out.println(ANSI_WHITE + String.format(format, args) + ANSI_RESET);
    }
}
