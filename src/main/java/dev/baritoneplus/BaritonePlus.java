package dev.baritoneplus;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = BaritonePlus.MODID, dist = Dist.CLIENT)
public class BaritonePlus {
    public static final String MODID = "baritoneplus";

    private static boolean installed = false;

    public BaritonePlus(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(BaritonePlus::onClientTick);
        NeoForge.EVENT_BUS.addListener(BPlusCommand::register);
    }

    /** Baritone's primary instance is only usable once a player exists, so register on the first in-world tick. */
    private static void onClientTick(ClientTickEvent.Post event) {
        if (installed) return;
        if (Minecraft.getInstance().player == null) return;

        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        IBaritoneProcess[] processes = {
                new DangerProcess(baritone),
                new AutoEatProcess(baritone),
                new AutoHealProcess(baritone)
        };
        for (IBaritoneProcess p : processes) {
            baritone.getPathingControlManager().registerProcess(p);
        }
        Profiles.apply(Config.PROFILE.get());
        installed = true;
    }
}
