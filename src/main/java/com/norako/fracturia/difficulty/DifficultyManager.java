package com.norako.fracturia.difficulty;

public class DifficultyManager {
    private static volatile Difficulty current = Difficulty.NONE;

    public static Difficulty getCurrent() {
        return current;
    }

    public static void setCurrent(Difficulty d) {
        current = d;
    }
}
