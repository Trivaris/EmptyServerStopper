package com.trivaris.serverstopper;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ServerStopper.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec.ConfigValue<Integer> COOLDOWN = BUILDER.comment("Time until the Server shuts down in Milliseconds").define("cooldown", 300000);
    static final ModConfigSpec SPEC = BUILDER.build();

    public static int cooldown;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        cooldown = COOLDOWN.get();
    }
}
