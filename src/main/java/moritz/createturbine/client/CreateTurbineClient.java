package moritz.createturbine.client;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelVisual;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import moritz.createturbine.content.WaterTurbineBlockEntity;
import moritz.createturbine.registry.CTBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only bootstrap. Wired in from the main mod constructor behind a Dist check.
 *
 * Rendering mirrors Create's small water wheel exactly:
 *  - The static andesite frame is the chunk blockstate model (create:block/water_wheel/block via
 *    our water_turbine model, which adds the cutout_mipped render type the frame texture needs).
 *  - The spinning wheel is Create's Flywheel {@link WaterWheelVisual}.
 *  - {@link WaterWheelRenderer} is the BER fallback for non-instancing backends; under Flywheel
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
        event.registerBlockEntityRenderer(CTBlockEntities.WATER_TURBINE.get(), WaterWheelRenderer::standard);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                SimpleBlockEntityVisualizer.<WaterTurbineBlockEntity>builder(CTBlockEntities.WATER_TURBINE.get())
                        .factory(WaterWheelVisual::standard)
                        .apply());
    }
}
