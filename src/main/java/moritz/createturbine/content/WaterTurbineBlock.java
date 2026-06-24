package moritz.createturbine.content;

import moritz.createturbine.registry.CTBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Water Turbine block. It is a Create kinetic generator whose axle runs along the
 * {@link #FACING} axis; shafts connect to both ends of that axle.
 *
 * Mirrors Create's own water wheel: FACING is normalised to the positive axis direction on
 * placement so orientation and rotation are deterministic, and shafts connect on either side.
 */
public class WaterTurbineBlock extends DirectionalKineticBlock implements IBE<WaterTurbineBlockEntity> {

    public WaterTurbineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        // Normalise FACING to the positive direction of its axis. This makes both axle ends
        // equivalent and keeps the rendered orientation/rotation consistent regardless of
        // which side the block was placed against (same approach as Create's water wheel).
        Direction.Axis axis = state.getValue(FACING).getAxis();
        return state.setValue(FACING, Direction.get(Direction.AxisDirection.POSITIVE, axis));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        // The axle runs along the facing axis; connect on both ends.
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<WaterTurbineBlockEntity> getBlockEntityClass() {
        return WaterTurbineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WaterTurbineBlockEntity> getBlockEntityType() {
        return CTBlockEntities.WATER_TURBINE.get();
    }
}
