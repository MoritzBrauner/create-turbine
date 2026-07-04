package moritz.createturbine;

import moritz.createturbine.client.CreateTurbineClient;
import moritz.createturbine.registry.CTBlockEntities;
import moritz.createturbine.registry.CTBlocks;
import moritz.createturbine.registry.CTItems;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Entry point of the "Create: Turbine" addon.
 *
 * Adds a single block – the Water Turbine – that behaves like a Create kinetic
 * generator, but whose generated speed (RPM) and stress capacity (SU) scale with
 * the height of the connected water column above / diagonally above the block.
 */
@Mod(CreateTurbine.MODID)
public class CreateTurbine {
    public static final String MODID = "createturbine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateTurbine(IEventBus modEventBus, ModContainer modContainer) {
        // Register all DeferredRegisters to the mod event bus.
        CTBlocks.register(modEventBus);
        CTBlockEntities.register(modEventBus);
        CTItems.register(modEventBus);

        // Power-curve values; per-world server config, synced to clients on join.
        modContainer.registerConfig(ModConfig.Type.SERVER, CTConfig.SPEC);

        // Put the turbine item into a vanilla creative tab.
        modEventBus.addListener(this::addCreative);

        // Client-only setup (block entity renderer for the spinning wheel).
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CreateTurbineClient.init(modEventBus);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(CTItems.WATER_TURBINE);
        }
    }
    // A rotating-shaft BlockEntityRenderer can be added later (client-side) via
    // EntityRenderersEvent.RegisterRenderers. The turbine is fully functional without it.
}
