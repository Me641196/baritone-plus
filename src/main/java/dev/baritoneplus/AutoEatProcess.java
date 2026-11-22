package dev.baritoneplus;

import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.input.Input;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/**
 * Pauses whatever Baritone is doing, moves food into the selected hotbar slot,
 * holds right-click until it is eaten, then puts everything back.
 */
final class AutoEatProcess implements IBaritoneProcess {
    private static final Set<net.minecraft.world.item.Item> NEVER = Set.of(
            Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.CHORUS_FRUIT);

    private final IBaritone baritone;

    private boolean eating;
    private boolean sawUse;
    private int ticks;
    private int prevSlot = -1;
    private int swapFrom = -1;
    private int usedSlot = -1;
    private long cooldownUntil;

    AutoEatProcess(IBaritone baritone) {
        this.baritone = baritone;
    }

    @Override
    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) return false;
        if (eating) return true;
        if (!Config.AUTO_EAT.get() && !emergency(p)) return false;
        if (mc.screen != null) return false;
        if (mc.level.getGameTime() < cooldownUntil) return false;
        return pick(p) >= 0;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return null;

        if (!eating) {
            int slot = pick(p);
            if (slot < 0) return null;
            select(mc, p, slot);
            eating = true;
            sawUse = false;
            ticks = 0;
            Util.chat("Eating " + p.getInventory().getItem(usedSlot).getHoverName().getString() + "...");
        }

        ticks++;
        if (p.isUsingItem()) sawUse = true;

        boolean finished = sawUse && !p.isUsingItem();
        boolean stuck = !sawUse && ticks > 8;
        boolean timeout = ticks > 100;
        if (finished || stuck || timeout) {
            if (stuck || timeout) cooldownUntil = p.level().getGameTime() + 100;
            finish(mc, p);
        } else {
            baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
        }
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public void onLostControl() {
        Minecraft mc = Minecraft.getInstance();
        if (eating && mc.player != null) finish(mc, mc.player);
        eating = false;
    }

    @Override
    public double priority() {
        return 6.0;
    }

    public IBaritone associatedWith() {
        return baritone;
    }

    public String displayName0() {
        return "Baritone+ Auto Eat";
    }

    // ------------------------------------------------------------------

    private boolean emergency(LocalPlayer p) {
        return Config.HEAL_ENABLED.get() && Config.GAPPLE_EMERGENCY.get()
                && p.getHealth() <= Config.CRITICAL_BELOW.get();
    }

    /** @return inventory index (0-35) of what to eat, or -1. */
    private int pick(LocalPlayer p) {
        if (emergency(p)) {
            int g = findItem(p, Items.GOLDEN_APPLE);
            if (g < 0) g = findItem(p, Items.ENCHANTED_GOLDEN_APPLE);
            if (g >= 0) return g;
        }
        if (!Config.AUTO_EAT.get()) return -1;
        var food = p.getFoodData();
        if (food.getFoodLevel() > Config.HUNGER_THRESHOLD.get() || !food.needsFood()) return -1;
        return findFood(p, 20 - food.getFoodLevel());
    }

    private static int findItem(LocalPlayer p, net.minecraft.world.item.Item item) {
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }

    /** Best food: fills the missing hunger with the least waste; otherwise the biggest one. */
    private static int findFood(LocalPlayer p, int missing) {
        int best = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < 36; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.isEmpty() || NEVER.contains(s.getItem())) continue;
            FoodProperties fp = s.get(DataComponents.FOOD);
            if (fp == null || harmful(fp)) continue;

            int over = fp.nutrition() - missing;
            // Not wasting hunger points matters most; among equals prefer the larger meal.
            int score = (over <= 0 ? 1000 + fp.nutrition() : 500 - over);
            // Small bonus for hotbar items (no inventory swap needed).
            if (i < 9) score += 1;
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private static boolean harmful(FoodProperties fp) {
        for (FoodProperties.PossibleEffect pe : fp.effects()) {
            if (pe.effect().getEffect().value().getCategory() == MobEffectCategory.HARMFUL) return true;
        }
        return false;
    }

    private void select(Minecraft mc, LocalPlayer p, int invIndex) {
        var inv = p.getInventory();
        prevSlot = inv.selected;
        if (invIndex < 9) {
            inv.selected = invIndex;
            usedSlot = invIndex;
            swapFrom = -1;
        } else {
            // Swap the food into the currently selected hotbar slot.
            mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId, invIndex, inv.selected, ClickType.SWAP, p);
            usedSlot = inv.selected;
            swapFrom = invIndex;
        }
    }

    private void finish(Minecraft mc, LocalPlayer p) {
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
        if (swapFrom >= 0 && mc.gameMode != null) {
            mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId, swapFrom, usedSlot, ClickType.SWAP, p);
        }
        if (prevSlot >= 0) p.getInventory().selected = prevSlot;
        eating = false;
        sawUse = false;
        prevSlot = -1;
        swapFrom = -1;
        usedSlot = -1;
    }
}
