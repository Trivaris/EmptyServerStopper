package com.trivaris.serverstopper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ISystemReportExtender;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Mod(value = ServerStopper.MODID, dist = Dist.DEDICATED_SERVER)
public class ServerStopper {
    public static final String MODID = "serverstopper";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> shutdownTask = null;

    public ServerStopper(ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public synchronized void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();

        if (server == null) {
            LOGGER.info("Server is null.");
            return;
        }

        // If it is currently scheduled
        if (shutdownTask != null && !shutdownTask.isDone() && !shutdownTask.isCancelled()) {
            LOGGER.info("Cancelling existing shutdown task.");
            shutdownTask.cancel(false);
        }


        LOGGER.info("Scheduling server shutdown.");
        shutdownTask = scheduleShutdown(server);
    }

    @SubscribeEvent
    public synchronized void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();

        if (server == null) {
            LOGGER.info("Server is null.");
            return;
        }

        // If a shutdown is already scheduled, cancel it when a player joins
        if (shutdownTask != null && !shutdownTask.isDone() && !shutdownTask.isCancelled()) {
            LOGGER.info("Player joined! Cancelling scheduled shutdown.");
            shutdownTask.cancel(false);
        }
    }

    private ScheduledFuture<?> scheduleShutdown(MinecraftServer server) {
        return scheduler.schedule(() -> {
            if (server.getPlayerList().getPlayerCount() == 0) {
                server.stopServer();
                LOGGER.info("Shutting down server...");
                System.exit(0);
            } else LOGGER.info("Players online...");
        }, Config.cooldown, TimeUnit.MILLISECONDS);
    }
}

