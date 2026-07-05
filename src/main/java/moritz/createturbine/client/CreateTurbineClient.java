package moritz.createturbine.client;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import moritz.createturbine.content.WaterTurbineBlockEntity;
import moritz.createturbine.registry.CTBlockEntities;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only bootstrap. Wired in from the main mod constructor behind a Dist check.
 *
 * Rendering mirrors Create's small water wheel:
 *  - The static andesite frame is the chunk blockstate model (create:block/water_wheel/block via
 *    our water_turbine model, which adds the cutout_mipped render type the frame texture needs).
 *  - The spinning wheel is {@link WaterTurbineVisual} (Flywheel), retextured to the applied
 *    turbine material (default industrial iron).
 *  - {@link WaterTurbineRenderer} is the BER fallback for non-instancing backends; under Flywheel
 *    it early-returns and the visual suppresses it, just like for Create's own wheel.
 */
public final class CreateTurbineClient {

    private CreateTurbineClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CreateTurbineClient::onRegisterRenderers);
        modEventBus.addListener(CreateTurbineClient::onClientSetup);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CTBlockEntities.WATER_TURBINE.get(), WaterTurbineRenderer::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // The BER's buffer cache compartment must be registered before first use.
            SuperByteBufferCache.getInstance().registerCompartment(WaterTurbineRenderer.TURBINE_WHEEL);
            SimpleBlockEntityVisualizer.<WaterTurbineBlockEntity>builder(CTBlockEntities.WATER_TURBINE.get())
                    .factory(WaterTurbineVisual::new)
                    .apply();
        });
    }
}
