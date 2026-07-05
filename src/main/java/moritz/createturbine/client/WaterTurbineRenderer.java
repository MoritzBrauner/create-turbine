package moritz.createturbine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import moritz.createturbine.content.WaterTurbineBlock;
import moritz.createturbine.content.WaterTurbineBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BER fallback for the spinning wheel (used when the Flywheel backend is off/batched; under
 * instancing it early-returns and {@link WaterTurbineVisual} draws instead). Mirrors Create's
 * WaterWheelRenderer, with the material-aware model from {@link TurbineWheelModels}.
 */
public class WaterTurbineRenderer extends KineticBlockEntityRenderer<WaterTurbineBlockEntity> {

    /** Registered with the SuperByteBufferCache in {@link CreateTurbineClient}. */
    public static final SuperByteBufferCache.Compartment<ModelKey> TURBINE_WHEEL =
            new SuperByteBufferCache.Compartment<>();

    public record ModelKey(BlockState state, BlockState material) {
    }

    public WaterTurbineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(WaterTurbineBlockEntity be, BlockState state) {
        ModelKey key = new ModelKey(state, be.material);
        return SuperByteBufferCache.getInstance().get(TURBINE_WHEEL, key, () -> {
            BakedModel model = TurbineWheelModels.generateModel(key.material());
            Direction dir = key.state().getValue(WaterTurbineBlock.FACING);
            PoseStack transform = CachedBuffers.rotateToFaceVertical(dir).get();
            return SuperBufferFactory.getInstance().createForBlock(model, Blocks.AIR.defaultBlockState(), transform);
        });
    }
}
