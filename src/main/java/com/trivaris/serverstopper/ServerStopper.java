package com.trivaris.serverstopper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(value = ServerStopper.MODID, dist = Dist.DEDICATED_SERVER)
public class ServerStopper {
    public static final String MODID = "serverstopper";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ServerStopper(ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();

        if (server == null) {
            LOGGER.info("Server is null.");
            return;
        }

        if (server.getPlayerList().getPlayerCount() == 0) {
            LOGGER.info("No players online. Scheduling server shutdown.");

            scheduler.schedule(() -> {
                if (server.getPlayerList().getPlayerCount() == 0) {
                    LOGGER.info("Shutting down server...");
                    server.halt(true);
                }
            }, Config.cooldown, TimeUnit.MILLISECONDS);
        }

        else LOGGER.info("Players online, not scheduling shutdown.");

    }
}

