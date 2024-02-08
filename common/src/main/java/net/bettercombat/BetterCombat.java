package net.bettercombat;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.bettercombat.client.BetterCombatClient;
import net.bettercombat.compatibility.CompatibilityFlags;
import net.bettercombat.config.FallbackConfig;
import net.bettercombat.config.ServerConfig;
import net.bettercombat.config.ServerConfigWrapper;
import net.bettercombat.logic.CombatMode;
import net.bettercombat.logic.WeaponAttributesFallback;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.network.ServerNetwork;
import net.bettercombat.utils.SoundHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.tinyconfig.ConfigManager;

public class BetterCombat implements ModInitializer {
    public static final String MODID = "bettercombat";
    public static ServerConfig config;
    private static FallbackConfig fallbackDefault = FallbackConfig.createDefault();
    public static ConfigManager<FallbackConfig> fallbackConfig = new ConfigManager<FallbackConfig>
            ("fallback_compatibility", fallbackDefault)
            .builder()
            .setDirectory(MODID)
            .sanitize(true)
            .build();

    @Override
    public void onInitialize() {
        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        // Intuitive way to load a config :)
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
        loadFallbackConfig();
        CompatibilityFlags.initialize();
        ServerNetwork.initializeHandlers();
        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
            WeaponRegistry.loadAttributes(minecraftServer.getResourceManager());
            if (config.fallback_compatibility_enabled) {
                WeaponAttributesFallback.initialize();
            }
            WeaponRegistry.encodeRegistry();
        });

        if(Platform.Fabric) {
            // forge locks the registries so this would crash
            SoundHelper.registerSounds();
        }
    }

    private void loadFallbackConfig() {
        fallbackConfig.load();
        if (fallbackConfig.value == null) {
            // Most likely corrupt config
            fallbackConfig.value = FallbackConfig.createDefault();
        }
        if (fallbackConfig.value.schema_version < fallbackDefault.schema_version) {
            fallbackConfig.value = FallbackConfig.migrate(fallbackConfig.value, FallbackConfig.createDefault());
        }
        fallbackConfig.save();
    }

    public static CombatMode getCurrentCombatMode() {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            return BetterCombatClient.config.singlePlayerCombatMode;
        }
        return BetterCombatClient.serverCombatMode;
    }
}