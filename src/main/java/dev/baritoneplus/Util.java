package dev.baritoneplus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;

final class Util {
    private Util() {}

    /** Status message, respects the config toggle. */
    static void chat(String msg) {
        if (Config.MESSAGES.get()) say(msg);
    }

    /** Always shown (command feedback). */
    static void say(String msg) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) p.displayClientMessage(Component.literal("§b[B+]§r " + msg), false);
    }

    /** Living hostile mobs within r blocks. Neutral mobs that aren't angry at the player are ignored. */
    static List<Monster> hostilesNear(LocalPlayer p, double r) {
        return p.level().getEntitiesOfClass(
                Monster.class,
                p.getBoundingBox().inflate(r),
                m -> m.isAlive()
                        && m.distanceToSqr(p) <= r * r
                        && !(m instanceof NeutralMob nm && !nm.isAngryAt(p)));
    }
}
