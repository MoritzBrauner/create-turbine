package moritz.createturbine.content;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;

import moritz.createturbine.CTConfig;
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
 * Extends Create's {@link WaterWheelBlockEntity} to reuse its exact rendering and its flow-score
 * logic. The flow score decides the SPIN DIRECTION exactly like a vanilla water wheel: each side
 * where water flows tangentially past the blades contributes +/-1, so water dropping evenly down
 * both sides cancels to 0 and the turbine stands still.
 *
 * The MAGNITUDE of speed (RPM) and the stress capacity (SU) come from the water pressure — the
 * height of the connected water column above the block — via the power-of-two curve in
 * {@link TurbineCurve}. Create's kinetic network computes total SU as capacity x |speed|, so
 * {@link #calculateAddedStressCapacity()} returns totalSU(height) / rpm(height).
 */
public class WaterTurbineBlockEntity extends WaterWheelBlockEntity {

    /** How often (in ticks) the water column is re-evaluated. */
    private static final int RECALC_RATE = 20;

    private PressureResult pressure = PressureResult.EMPTY;
    private int recalcTimer = 0;

    // Outputs last pushed into the kinetic network. When the recomputed values differ (water
    // column changed, or the config was edited), updateGeneratedRotation() re-propagates.
    private float lastAppliedRpm = Float.NaN;
    private float lastAppliedCapacity = Float.NaN;

    public WaterTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // Same default wood as a freshly placed Create water wheel (set in the super ctor, made
        // explicit here); right-clicking with planks swaps it via applyMaterialIfValid.
        this.material = Blocks.SPRUCE_PLANKS.defaultBlockState();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            resampleAndApply();
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
        resampleAndApply();
    }

    /**
     * Immediate reaction to a neighbour change, scheduled by the block one tick after water
     * around the turbine changes (mirrors Create's water wheel responsiveness).
     */
    public void waterChanged() {
        // Pressure first: a direction flip must be applied with the fresh magnitude. Applying
        // the new sign at a stale, lower magnitude would make Create's applyNewSpeed pop the
        // turbine off an externally driven network instead of letting it take over.
        samplePressure();
        determineAndApplyFlowScore(); // spin direction; updates rotation itself when it changes
        recalcTimer = RECALC_RATE;
        applyIfChanged();
    }

    @Override
    public void lazyTick() {
        // Same ordering concern as waterChanged(): the inherited lazyTick recomputes the flow
        // score, so refresh the pressure it will be applied with.
        if (level != null && !level.isClientSide) {
            samplePressure();
        }
        super.lazyTick();
    }

    /** Re-samples the water column and re-propagates speed/stress when the outputs changed. */
    private void resampleAndApply() {
        samplePressure();
        applyIfChanged();
    }

    private void samplePressure() {
        pressure = PressureSampler.sample(level, worldPosition, CTConfig.MAX_COLUMN_HEIGHT.get());
    }

    /** Re-propagates speed/stress when the computed outputs changed (water column or config). */
    private void applyIfChanged() {
        float rpm = targetRpm();
        float capacity = targetCapacity();
        if (rpm != lastAppliedRpm || capacity != lastAppliedCapacity) {
            lastAppliedRpm = rpm;
            lastAppliedCapacity = capacity;
            updateGeneratedRotation();
            setChanged();
        }
    }

    /** Speed magnitude (RPM) for the current water column; 0 without water. */
    private int targetRpm() {
        int height = pressure.height();
        return height <= 0 ? 0 : TurbineCurve.rpm(height);
    }

    /** Stress capacity in SU per RPM (Create's unit): total SU / RPM for the current column. */
    private float targetCapacity() {
        int height = pressure.height();
        if (height <= 0) {
            return 0f;
        }
        return TurbineCurve.stressUnits(height) / (float) TurbineCurve.rpm(height);
    }

    @Override
    public float getGeneratedSpeed() {
        // Direction from the water-wheel flow score: water passing one side of the blades spins
        // the turbine; symmetric flow down both sides cancels to 0 -> standstill.
        int direction = Integer.signum(flowScore);
        return direction * targetRpm();
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = targetCapacity();
        // Mirror the base implementation, which stores the value for network bookkeeping.
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
