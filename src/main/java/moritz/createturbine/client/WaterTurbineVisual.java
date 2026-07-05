package moritz.createturbine.client;

import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import moritz.createturbine.content.WaterTurbineBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Flywheel visual for the spinning turbine wheel. Structure mirrors Create's WaterWheelVisual,
 * but the model comes from {@link TurbineWheelModels} so any tagged material retextures the
 * wheel (Create's version only handles planks). Rebuilds the instance when the material changes.
 */
public class WaterTurbineVisual extends KineticBlockEntityVisual<WaterTurbineBlockEntity> {

    private static final RendererReloadCache<BlockState, Model> MODEL_CACHE = new RendererReloadCache<>(
            material -> new BakedModelBuilder(TurbineWheelModels.generateModel(material)).build());

    protected BlockState lastMaterial;
    protected RotatingInstance rotatingModel;

    public WaterTurbineVisual(VisualizationContext context, WaterTurbineBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        setupInstance();
    }

    private void setupInstance() {
        lastMaterial = blockEntity.material;
        rotatingModel = instancerProvider()
                .instancer(AllInstanceTypes.ROTATING, MODEL_CACHE.get(blockEntity.material))
                .createInstance();
        rotatingModel.setup(blockEntity)
                .setPosition(getVisualPosition())
                .rotateToFace(rotationAxis())
                .setChanged();
    }

    @Override
    public void update(float partialTick) {
        if (lastMaterial != blockEntity.material) {
            rotatingModel.delete();
            setupInstance();
        } else {
            rotatingModel.setup(blockEntity)
                    .setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(rotatingModel);
    }
}
