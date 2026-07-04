package moritz.createturbine.content;

import moritz.createturbine.CTConfig;
import net.minecraft.util.Mth;

/**
 * The turbine's power curve: geometric interpolation between the configured min value (at water
 * column height 1) and max value (at height maxColumnHeight), snapped to powers of two.
 *
 * With the defaults (8..256 RPM, 256..16384 SU over heights 1..32) every step of the curve is an
 * exact power of two and the endpoints are hit exactly:
 *   RPM: 8 (h1-4), 16 (h5-10), 32 (h11-16), 64 (h17-22), 128 (h23-28), 256 (h29-32)
 *   SU:  256 (h1-3), 512 (h4-8), 1024 (h9-13), 2048 (h14-19), 4096 (h20-24),
 *        8192 (h25-29), 16384 (h30-32)
 */
public final class TurbineCurve {

    private TurbineCurve() {
    }

    /** Rotation speed (RPM) for the given water column height. */
    public static int rpm(int height) {
        return interpolatePow2(height, CTConfig.MAX_COLUMN_HEIGHT.get(),
                CTConfig.MIN_RPM.get(), CTConfig.MAX_RPM.get());
    }

    /** Total stress capacity (SU) for the given water column height. */
    public static int stressUnits(int height) {
        return interpolatePow2(height, CTConfig.MAX_COLUMN_HEIGHT.get(),
                CTConfig.MIN_SU.get(), CTConfig.MAX_SU.get());
    }

    /**
     * Geometrically interpolates from minValue (height 1) to maxValue (height maxHeight), snaps
     * the result to the nearest power of two and clamps it into [min, max]. Defined for any
     * positive endpoints — equal, inverted or non-powers-of-two — so config edits cannot break
     * the calculation, only bend the curve.
     */
    static int interpolatePow2(int height, int maxHeight, int minValue, int maxValue) {
        int clampedHeight = Mth.clamp(height, 1, Math.max(1, maxHeight));
        double t = maxHeight <= 1 ? 1.0 : (clampedHeight - 1) / (double) (maxHeight - 1);
        double log2Min = Math.log(minValue) / Math.log(2);
        double log2Max = Math.log(maxValue) / Math.log(2);
        long exponent = Math.round(log2Min + t * (log2Max - log2Min));
        double snapped = Math.pow(2, exponent);
        return (int) Mth.clamp(snapped, Math.min(minValue, maxValue), Math.max(minValue, maxValue));
    }
}
