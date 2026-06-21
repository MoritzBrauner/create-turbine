package moritz.createturbine.content;

import java.util.List;

import moritz.createturbine.content.PressureSampler.PressureResult;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kinetic generator whose output scales with the water pressure (column height)
 * above the block. Both the generated speed (RPM) and the stress capacity per RPM
 * grow with height, so the effective power (≈ capacity × speed) scales strongly
 * with how deep the water above the turbine is ("beides skaliert").
 */
public class WaterTurbineBlockEntity extends GeneratingKineticBlockEntity {

    // ---- Tunables (kept as constants for now; can move to a NeoForge config later) ----
    /** RPM produced at one block of water height. */
    private static final float BASE_RPM = 4f;
    /** Additional RPM per block of water height. */
    private static final float RPM_PER_BLOCK = 4f;
    /** Create's hard speed cap. */
    private static final float MAX_RPM = 256f;
    /** Base stress capacity (SU per RPM) at one block of height. */
    private static final float BASE_CAPACITY = 4f;
    /** Capacity growth factor per block of height. */
    private static final float CAP_PER_BLOCK = 0.5f;

    /** How often (in ticks) the water column is re-evaluated. */
    private static final int RECALC_RATE = 20;

    private PressureResult pressure = PressureResult.EMPTY;

    public WaterTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(RECALC_RATE);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // No extra behaviours needed.
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            pressure = PressureSampler.sample(level, worldPosition);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) {
            return;
        }
        PressureResult sampled = PressureSampler.sample(level, worldPosition);
        if (!sampled.equals(pressure)) {
            pressure = sampled;
            // Recompute generated speed + stress and propagate through the network.
            updateGeneratedRotation();
            setChanged();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        int height = pressure.height();
        if (height <= 0) {
            return 0f;
        }
        float rpm = BASE_RPM + RPM_PER_BLOCK * height;
        return Math.min(rpm, MAX_RPM);
    }

    @Override
    public float calculateAddedStressCapacity() {
        int height = pressure.height();
        float capacity = height <= 0 ? 0f : BASE_CAPACITY * (1f + CAP_PER_BLOCK * height);
        // Mirror the base implementation, which stores the value for goggle/network readout.
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("PressureHeight", pressure.height());
        tag.putInt("PressureVolume", pressure.volume());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        pressure = new PressureResult(tag.getInt("PressureHeight"), tag.getInt("PressureVolume"));
    }
}
