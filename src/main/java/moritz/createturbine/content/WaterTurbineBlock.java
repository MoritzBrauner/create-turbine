package moritz.createturbine.content;

import moritz.createturbine.registry.CTBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Water Turbine block. It is a Create kinetic generator: its rotational
 * output comes out of the {@link #FACING} side (where a shaft/cogwheel connects).
 *
 * Mirrors Create's own {@code CreativeMotorBlock} structure (DirectionalKineticBlock + IBE).
 */
public class WaterTurbineBlock extends DirectionalKineticBlock implements IBE<WaterTurbineBlockEntity> {

    public WaterTurbineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        // The shaft sticks out of the facing direction.
        return face == state.getValue(FACING);
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
