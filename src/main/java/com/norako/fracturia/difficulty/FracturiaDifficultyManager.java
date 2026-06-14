package com.norako.fracturia.difficulty;

// Client-side cache — synced from server via SyncDifficultyPayload on join/change
public class FracturiaDifficultyManager {
    private static volatile FracturiaDifficulty current = FracturiaDifficulty.NONE;
    // Choice made in CreateWorldScreen, sent to server on first join
    private static volatile FracturiaDifficulty pending = FracturiaDifficulty.NONE;

    public static FracturiaDifficulty getCurrent() { return current; }
    public static void setCurrent(FracturiaDifficulty d) { current = d; }

    public static FracturiaDifficulty getPending() { return pending; }
    public static void setPending(FracturiaDifficulty d) { pending = d; }
}
