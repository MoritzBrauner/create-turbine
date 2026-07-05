package moritz.createturbine.content;

import moritz.createturbine.registry.CTBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.levelWrappers.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Water Turbine block. It is a Create kinetic generator whose axle runs along the
 * {@link #FACING} axis; shafts connect to both ends of that axle.
 *
 * Mirrors Create's water wheel as shipped: all six FACING values occur in the world (Create's
 * own placement normalisation is a no-op — the setValue result is discarded), the blockstate
 * rotations orient the frame per facing, and right-clicking with a block tagged
 * {@code createturbine:turbine_materials} swaps the wheel material.
 */
public class WaterTurbineBlock extends DirectionalKineticBlock implements IBE<WaterTurbineBlockEntity> {

    public WaterTurbineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return onBlockEntityUseItemOn(level, pos, be -> be.applyMaterialIfValid(stack));
    }

    // Neighbour-change -> recompute chain, mirroring Create's water wheel: any shape update (and
    // placement) schedules a 1-tick block tick, which re-evaluates flow direction and pressure.

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                  LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
        if (world instanceof WrappedLevel || world.isClientSide()) {
            return state;
        }
        if (!world.getBlockTicks().hasScheduledTick(pos, this)) {
            world.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide()) {
            return;
        }
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        withBlockEntityDo(level, pos, WaterTurbineBlockEntity::waterChanged);
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

    // Kinetic-indicator particle ring sized like Create's water wheel (defaults are smaller).
    @Override
    public float getParticleInitialRadius() {
        return 1f;
    }

    @Override
    public float getParticleTargetRadius() {
        return 1.125f;
    }

    // Generators show capacity, not impact; Create's wheel hides the impact line too.
    @Override
    public boolean hideStressImpact() {
        return true;
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
