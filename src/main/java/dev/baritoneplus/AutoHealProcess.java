package dev.baritoneplus;

import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * When health is low, pauses Baritone and lets natural regeneration work.
 * Gives up if health is falling (fire, poison, drowning, starvation) or isn't recovering.
 */
final class AutoHealProcess implements IBaritoneProcess {
    private static final int NO_PROGRESS_TICKS = 20 * 45;

    private final IBaritone baritone;

    private boolean healing;
    private float lastHealth;
    private long lastProgressTick;
    private long cooldownUntil;

    AutoHealProcess(IBaritone baritone) {
        this.baritone = baritone;
    }

    @Override
    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return false;
        if (!Config.HEAL_ENABLED.get() || p.isDeadOrDying()) {
            healing = false;
            return false;
        }
        long now = mc.level.getGameTime();
        float hp = p.getHealth();

        if (!healing) {
            if (now < cooldownUntil || hp > Config.PAUSE_BELOW.get()) return false;
            // Don't stand still while burning, in lava or running out of air.
            if (p.isOnFire() || p.isInLava() || p.getAirSupply() < p.getMaxAirSupply() / 2) return false;
            healing = true;
            lastHealth = hp;
            lastProgressTick = now;
            Util.chat("Low health (" + (int) hp + "), pausing to recover.");
            return true;
        }

        if (hp >= Config.RESUME_ABOVE.get()) {
            healing = false;
            Util.chat("Recovered, resuming.");
            return false;
        }
        if (hp < lastHealth - 0.1f || p.isOnFire() || p.isInLava()) {
            // Getting hurt while resting: resting isn't the answer.
            give(now, 20 * 15, "Still taking damage, not pausing.");
            return false;
        }
        if (hp > lastHealth + 0.1f) lastProgressTick = now;
        lastHealth = Math.max(lastHealth, hp);
        if (now - lastProgressTick > NO_PROGRESS_TICKS) {
            give(now, 20 * 60, "Not regenerating (hungry?), resuming.");
            return false;
        }
        return true;
    }

    private void give(long now, int cooldown, String why) {
        healing = false;
        cooldownUntil = now + cooldown;
        Util.chat(why);
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public void onLostControl() {
        // Nothing to clean up; isActive() decides whether we keep resting.
    }

    @Override
    public double priority() {
        return 5.8;
    }

    public IBaritone associatedWith() {
        return baritone;
    }

    public String displayName0() {
        return "Baritone+ Auto Heal";
    }
}
