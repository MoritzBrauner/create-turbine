package moritz.createturbine.client;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelVisual;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import moritz.createturbine.CreateTurbine;
import moritz.createturbine.content.WaterTurbineBlockEntity;
import moritz.createturbine.registry.CTBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only bootstrap. Wired in from the main mod constructor behind a Dist check.
 *
 * Rendering is split:
 *  - The spinning wheel blades are drawn by Create's Flywheel {@link WaterWheelVisual}.
 *  - The solid central hub is drawn by our own {@link WaterTurbineRenderer}.
 * The visualizer uses {@code neverSkipVanillaRender()} so the hub renderer keeps running alongside
 * the wheel visual.
 */
public final class CreateTurbineClient {

    /** Solid central hub model (our own asset). */
    public static final PartialModel WATER_TURBINE_HUB =
            PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateTurbine.MODID, "block/water_turbine_hub"));

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
        event.enqueueWork(() ->
                SimpleBlockEntityVisualizer.<WaterTurbineBlockEntity>builder(CTBlockEntities.WATER_TURBINE.get())
                        .factory(WaterWheelVisual::standard)
                        .neverSkipVanillaRender()
                        .apply());
    }
}
