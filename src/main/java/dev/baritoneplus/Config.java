package dev.baritoneplus;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MESSAGES = B
            .comment("Print short [B+] status messages in chat.")
            .define("general.messages", true);

    // ---- Auto eat ----
    public static final ModConfigSpec.BooleanValue AUTO_EAT = B
            .comment("Pause Baritone and eat when hungry.")
            .define("autoEat.enabled", true);
    public static final ModConfigSpec.IntValue HUNGER_THRESHOLD = B
            .comment("Eat when the food bar is at or below this value (20 = full).")
            .defineInRange("autoEat.hungerThreshold", 14, 1, 19);

    // ---- Auto heal ----
    public static final ModConfigSpec.BooleanValue HEAL_ENABLED = B
            .comment("Pause Baritone to regenerate when health is low.")
            .define("heal.enabled", true);
    public static final ModConfigSpec.DoubleValue PAUSE_BELOW = B
            .comment("Pause when health (half-hearts, 20 = full) is at or below this.")
            .defineInRange("heal.pauseBelow", 8.0, 1.0, 19.0);
    public static final ModConfigSpec.DoubleValue RESUME_ABOVE = B
            .comment("Resume once health reaches this.")
            .defineInRange("heal.resumeAbove", 16.0, 2.0, 20.0);
    public static final ModConfigSpec.BooleanValue GAPPLE_EMERGENCY = B
            .comment("Eat a golden apple if health drops to criticalBelow (uses up valuable items!).")
            .define("heal.goldenAppleEmergency", false);
    public static final ModConfigSpec.DoubleValue CRITICAL_BELOW = B
            .comment("Health at or below which the golden apple emergency triggers.")
            .defineInRange("heal.criticalBelow", 5.0, 1.0, 19.0);

    // ---- Danger avoidance ----
    public static final ModConfigSpec.BooleanValue DANGER_ENABLED = B
            .comment("Run away from creepers and from hostile mobs when hurt.")
            .define("danger.enabled", true);
    public static final ModConfigSpec.IntValue CREEPER_RADIUS = B
            .comment("Flee from creepers closer than this many blocks.")
            .defineInRange("danger.creeperRadius", 7, 2, 24);
    public static final ModConfigSpec.IntValue HOSTILE_RADIUS = B
            .comment("When hurt, flee from hostile mobs closer than this many blocks.")
            .defineInRange("danger.hostileRadius", 8, 2, 32);
    public static final ModConfigSpec.DoubleValue FLEE_BELOW_HEALTH = B
            .comment("Health at or below which hostile mobs trigger fleeing.")
            .defineInRange("danger.fleeBelowHealth", 8.0, 1.0, 20.0);
    public static final ModConfigSpec.IntValue FLEE_DISTANCE = B
            .comment("How far to run (blocks).")
            .defineInRange("danger.fleeDistance", 16, 6, 48);

    // ---- Pathing ----
    public static final ModConfigSpec.ConfigValue<String> PROFILE = B
            .comment("Baritone setting profile applied at startup: default, speed or safe.")
            .defineInList("pathing.profile", "default", List.of("default", "speed", "safe"));

    public static final ModConfigSpec SPEC = B.build();

    private Config() {}
}
