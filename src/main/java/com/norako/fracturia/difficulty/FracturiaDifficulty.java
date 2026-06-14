package com.norako.fracturia.difficulty;

public enum FracturiaDifficulty {
    NONE("none", 1.0f, 1.0f, 1.0f),
    DEMENTED("demented", 1.5f, 1.5f, 0.5f),
    INCONCEIVABLE("inconceivable", 2.5f, 2.5f, 0.25f);

    private final String id;
    private final float hpMultiplier;
    private final float damageMultiplier;
    private final float oreMultiplier;

    FracturiaDifficulty(String id, float hpMultiplier, float damageMultiplier, float oreMultiplier) {
        this.id = id;
        this.hpMultiplier = hpMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.oreMultiplier = oreMultiplier;
    }

    public String getId() { return id; }
    public float getHpMultiplier() { return hpMultiplier; }
    public float getDamageMultiplier() { return damageMultiplier; }
    public float getOreMultiplier() { return oreMultiplier; }
    public boolean isActive() { return this != NONE; }

    public FracturiaDifficulty next() {
        FracturiaDifficulty[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static FracturiaDifficulty fromId(String id) {
        for (FracturiaDifficulty d : values()) {
            if (d.id.equals(id)) return d;
        }
        return NONE;
    }
}
