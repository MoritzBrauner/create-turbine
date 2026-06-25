package moritz.createturbine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import moritz.createturbine.content.WaterTurbineBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Draws the solid andesite hub in the centre of the turbine.
 *
 * A solid, closed cube (see the water_turbine_hub model) is used instead of Create's open
 * water-wheel frame: that frame is open along the axle, so viewed head-on it showed a black tube
 * with the axle phasing through. A closed cube has no opening, so it can never be black; the
 * spinning wheel blades render around it via Create's Flywheel visual.
 *
 * Plain {@link BlockEntityRenderer} on purpose (does not extend Create's
 * {@code KineticBlockEntityRenderer}, which bails out under the Flywheel backend). The visualizer
 * is registered with {@code neverSkipVanillaRender()} so this keeps running with the wheel visual.
 */
public class WaterTurbineRenderer implements BlockEntityRenderer<WaterTurbineBlockEntity> {

    public WaterTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WaterTurbineBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer vc = buffers.getBuffer(RenderType.solid());
        // Cube is centred and symmetric, so no per-facing orientation is needed.
        CachedBuffers.partial(CreateTurbineClient.WATER_TURBINE_HUB, be.getBlockState())
                .light(light)
                .renderInto(ms, vc);
    }
}
