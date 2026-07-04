package moritz.createturbine;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config (stored per world, synced to clients on join — the goggle tooltip evaluates the
 * curve client-side) for the turbine's power curve.
 *
 * The curve interpolates geometrically between the min values (water column height 1) and the
 * max values (height {@code maxColumnHeight}), snapping every result to the nearest power of two
 * and clamping it into the configured [min, max] range. Any combination of values — equal,
 * inverted, non-powers-of-two — therefore yields a defined, monotonic curve instead of breaking
 * the calculation.
 */
public final class CTConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MIN_RPM;
    public static final ModConfigSpec.IntValue MAX_RPM;
    public static final ModConfigSpec.IntValue MIN_SU;
    public static final ModConfigSpec.IntValue MAX_SU;
    public static final ModConfigSpec.IntValue MAX_COLUMN_HEIGHT;

    static {
        BUILDER.comment("Water turbine power curve").push("turbine");
        MIN_RPM = BUILDER
                .comment("Rotation speed (RPM) at a water column height of 1 block.")
                .defineInRange("minRpm", 8, 1, 1024);
        MAX_RPM = BUILDER
                .comment("Rotation speed (RPM) at the maximum water column height.",
                        "Warning: values above Create's maxRotationSpeed (default 256) make attached components break off.")
                .defineInRange("maxRpm", 256, 1, 1024);
        MIN_SU = BUILDER
                .comment("Total stress capacity (SU) at a water column height of 1 block.")
                .defineInRange("minStressUnits", 256, 1, 16_777_216);
        MAX_SU = BUILDER
                .comment("Total stress capacity (SU) at the maximum water column height.")
                .defineInRange("maxStressUnits", 16384, 1, 16_777_216);
        MAX_COLUMN_HEIGHT = BUILDER
                .comment("Water column height (in blocks) at which the maximum values are reached.",
                        "Water above that height adds nothing.")
                .defineInRange("maxColumnHeight", 32, 1, 256);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CTConfig() {
    }
}
