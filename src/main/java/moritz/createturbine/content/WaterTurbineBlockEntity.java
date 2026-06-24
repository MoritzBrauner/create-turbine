package moritz.createturbine.content;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;

import moritz.createturbine.content.PressureSampler.PressureResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Water turbine generator.
 *
 * Extends Create's {@link WaterWheelBlockEntity} purely to reuse its exact rendering
 * (Create's WaterWheelRenderer draws the wheel for this block entity). The water-wheel's
 * flow-based speed is replaced here: speed (RPM) and stress capacity (SU per RPM) both scale
 * with the water pressure = height of the connected water column above the block, so the
 * effective power (≈ capacity × speed) grows strongly with depth ("beides skaliert").
 */
public class WaterTurbineBlockEntity extends WaterWheelBlockEntity {

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
    private int recalcTimer = 0;

    public WaterTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // The renderer reads 'material' for the wood texture; keep it non-null (default oak).
        this.material = Blocks.OAK_PLANKS.defaultBlockState();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            pressure = PressureSampler.sample(level, worldPosition);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (--recalcTimer > 0) {
            return;
        }
        recalcTimer = RECALC_RATE;
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
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
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
