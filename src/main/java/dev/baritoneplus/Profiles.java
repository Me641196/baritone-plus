package dev.baritoneplus;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;

/** One-shot presets for Baritone's own settings. */
final class Profiles {
    private Profiles() {}

    static void apply(String name) {
        Settings s = BaritoneAPI.getSettings();
        // Always start from Baritone's defaults for the settings we touch.
        reset(s.allowParkour);
        reset(s.allowParkourAscend);
        reset(s.allowSprint);
        reset(s.allowWaterBucketFall);
        reset(s.maxFallHeightNoWater);
        reset(s.maxFallHeightBucket);
        reset(s.avoidance);
        reset(s.mobAvoidanceRadius);
        reset(s.mobAvoidanceCoefficient);

        switch (name) {
            case "speed" -> {
                s.allowParkour.value = true;
                s.allowParkourAscend.value = true;
                s.allowSprint.value = true;
                s.allowWaterBucketFall.value = true;
                s.maxFallHeightBucket.value = 23;
            }
            case "safe" -> {
                s.allowParkour.value = false;
                s.allowParkourAscend.value = false;
                s.maxFallHeightNoWater.value = 2;
                s.maxFallHeightBucket.value = 10;
                s.avoidance.value = true;
                s.mobAvoidanceRadius.value = 24;
                s.mobAvoidanceCoefficient.value = 2.0;
            }
            default -> { /* "default": Baritone's own values */ }
        }
    }

    private static <T> void reset(Settings.Setting<T> setting) {
        setting.value = setting.defaultValue;
    }
}
