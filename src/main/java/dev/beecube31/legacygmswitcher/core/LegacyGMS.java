package dev.beecube31.legacygmswitcher.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = LegacyGMS.MODID,
		version = LegacyGMS.VERSION,
		name = LegacyGMS.MODNAME,
        acceptedMinecraftVersions = "[1.8,1.9)"
)
public class LegacyGMS {
	public static final String MODID = "legacy_gm_switcher";
	public static final String VERSION = "v0.1";
	public static final String MODNAME = "Legacy Gamemode Switcher";
	public static LegacyGMS instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LegacyGMS.instance = this;
	}

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClientHandler());
        }
    }
}