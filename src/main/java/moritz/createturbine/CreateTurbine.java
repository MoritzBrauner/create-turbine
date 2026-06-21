package moritz.createturbine;

import moritz.createturbine.registry.CTBlockEntities;
import moritz.createturbine.registry.CTBlocks;
import moritz.createturbine.registry.CTItems;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
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

    public CreateTurbine(IEventBus modEventBus) {
        // Register all DeferredRegisters to the mod event bus.
        CTBlocks.register(modEventBus);
        CTBlockEntities.register(modEventBus);
        CTItems.register(modEventBus);

        // Put the turbine item into a vanilla creative tab.
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(CTItems.WATER_TURBINE);
        }
    }
    // A rotating-shaft BlockEntityRenderer can be added later (client-side) via
    // EntityRenderersEvent.RegisterRenderers. The turbine is fully functional without it.
}
