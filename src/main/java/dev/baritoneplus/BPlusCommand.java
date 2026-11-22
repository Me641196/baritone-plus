package dev.baritoneplus;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client command: /bplus status | eat|heal|danger on|off | profile default|speed|safe */
final class BPlusCommand {
    private BPlusCommand() {}

    static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bplus")
                .then(Commands.literal("status").executes(c -> {
                    Util.say("eat=" + Config.AUTO_EAT.get()
                            + " heal=" + Config.HEAL_ENABLED.get()
                            + " danger=" + Config.DANGER_ENABLED.get()
                            + " profile=" + Config.PROFILE.get());
                    return 1;
                }))
                .then(toggle("eat", Config.AUTO_EAT))
                .then(toggle("heal", Config.HEAL_ENABLED))
                .then(toggle("danger", Config.DANGER_ENABLED))
                .then(Commands.literal("profile")
                        .then(profile("default"))
                        .then(profile("speed"))
                        .then(profile("safe"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toggle(String name, ModConfigSpec.BooleanValue value) {
        return Commands.literal(name)
                .then(Commands.literal("on").executes(c -> set(name, value, true)))
                .then(Commands.literal("off").executes(c -> set(name, value, false)));
    }

    private static int set(String name, ModConfigSpec.BooleanValue value, boolean on) {
        value.set(on);
        value.save();
        Util.say(name + " " + (on ? "enabled" : "disabled"));
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> profile(String name) {
        return Commands.literal(name).executes(c -> {
            Profiles.apply(name);
            Config.PROFILE.set(name);
            Config.PROFILE.save();
            Util.say("Pathing profile: " + name);
            return 1;
        });
    }
}
