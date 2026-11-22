package dev.baritoneplus;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalRunAway;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes over pathing and runs away from creepers (always) and from any hostile mob when hurt.
 * Control returns to whatever Baritone was doing once the threat is gone.
 */
final class DangerProcess implements IBaritoneProcess {
    private static final int HOLD_TICKS = 15;

    private final IBaritone baritone;

    private BlockPos[] threats = new BlockPos[0];
    private long holdUntil;
    private boolean fleeing;
    private long lastGoalTick = Long.MIN_VALUE;

    DangerProcess(IBaritone baritone) {
        this.baritone = baritone;
    }

    @Override
    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || !Config.DANGER_ENABLED.get() || p.isDeadOrDying()) {
            fleeing = false;
            return false;
        }
        long now = mc.level.getGameTime();

        List<BlockPos> found = new ArrayList<>();
        double creeperR = Config.CREEPER_RADIUS.get();
        for (Monster m : Util.hostilesNear(p, Math.max(creeperR, Config.HOSTILE_RADIUS.get()))) {
            boolean creeper = m instanceof Creeper;
            if (creeper && m.distanceToSqr(p) <= creeperR * creeperR) {
                found.add(m.blockPosition());
            } else if (!creeper
                    && p.getHealth() <= Config.FLEE_BELOW_HEALTH.get()
                    && m.distanceToSqr(p) <= (double) Config.HOSTILE_RADIUS.get() * Config.HOSTILE_RADIUS.get()) {
                found.add(m.blockPosition());
            }
        }

        if (!found.isEmpty()) {
            threats = found.toArray(new BlockPos[0]);
            holdUntil = now + HOLD_TICKS;
            if (!fleeing) {
                fleeing = true;
                lastGoalTick = Long.MIN_VALUE;
                Util.chat("Danger nearby, retreating!");
            }
            return true;
        }
        if (fleeing && now < holdUntil) return true;
        fleeing = false;
        return false;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        Minecraft mc = Minecraft.getInstance();
        long now = mc.level != null ? mc.level.getGameTime() : 0;
        var goal = new GoalRunAway(Config.FLEE_DISTANCE.get(), threats);

        // Re-plan with the fresh threat positions about once a second; otherwise just keep the path valid.
        if (calcFailed || now - lastGoalTick >= 20) {
            lastGoalTick = now;
            return new PathingCommand(goal, PathingCommandType.SET_GOAL_AND_PATH);
        }
        return new PathingCommand(goal, PathingCommandType.REVALIDATE_GOAL_AND_PATH);
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public void onLostControl() {
        fleeing = false;
    }

    @Override
    public double priority() {
        return 6.5;
    }

    public IBaritone associatedWith() {
        return baritone;
    }

    public String displayName0() {
        return "Baritone+ Danger Avoidance";
    }
}
