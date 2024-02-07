package net.bettercombat.forge;

import net.bettercombat.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.ModList;

import static net.bettercombat.Platform.Type.FORGE;

public class PlatformImpl {
    public static Platform.Type getPlatformType() {
        return FORGE;
    }

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static boolean isCastingSpell(PlayerEntity player) {
        return false;
    }
}
