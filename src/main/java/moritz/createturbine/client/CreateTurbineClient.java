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
 * Client-only bootstrap. Wired in from the main mod constructor behind a Dist check, so
 * none of these client classes are touched on a dedicated server.
 *
 * The water wheel is drawn two ways depending on the engine setting, and we register both so
 * it shows up regardless:
 *  - Flywheel backend ON (default): a Flywheel {@link WaterWheelVisual} (instanced). This is
 *    what actually draws the spinning blades; without it the wheel is invisible.
 *  - Flywheel backend OFF ("Batched"): Create's {@link WaterWheelRenderer} BlockEntityRenderer.
 */
public final class CreateTurbineClient {

    private CreateTurbineClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CreateTurbineClient::onRegisterRenderers);
        modEventBus.addListener(CreateTurbineClient::onClientSetup);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Fallback renderer (used when the Flywheel backend is disabled).
        event.registerBlockEntityRenderer(CTBlockEntities.WATER_TURBINE.get(), WaterWheelRenderer::standard);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                SimpleBlockEntityVisualizer.<WaterTurbineBlockEntity>builder(CTBlockEntities.WATER_TURBINE.get())
                        .factory(WaterWheelVisual::standard)
                        .apply());
    }
}
