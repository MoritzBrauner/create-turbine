package moritz.createturbine.content;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

/**
 * Computes the "water pressure" acting on a turbine via a flood fill through the connected
 * body of water that touches the block.
 *
 * The fill starts from every water block adjacent to the turbine that sits at or above the
 * turbine's own level (any side, not just the top), and only spreads to water at or above that
 * level. The pressure height is the highest connected water level above the turbine — i.e. how
 * deep the turbine sits below the surface of the water column it touches. This makes it work when
 * the turbine is submerged, sits beside a tall water column, or has a shaft of water above it.
 *
 * The result also carries the connected volume (so a volume term can be switched on later
 * without touching the sampling logic).
 */
public final class PressureSampler {

    /** Maximum column height that contributes to pressure (caps the search vertically). */
    public static final int MAX_HEIGHT = 64;
    /** Hard cap on the number of water blocks visited (caps CPU cost / prevents lag). */
    public static final int MAX_BLOCKS = 512;

    private PressureSampler() {}

    public record PressureResult(int height, int volume) {
        public static final PressureResult EMPTY = new PressureResult(0, 0);
    }

    public static PressureResult sample(Level level, BlockPos turbinePos) {
        final int baseY = turbinePos.getY();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        // Seed from any water touching the turbine at or above its own level. Water strictly
        // below the turbine adds no pressure, so it is not used as a starting point.
        for (Direction dir : Direction.values()) {
            BlockPos n = turbinePos.relative(dir);
            if (n.getY() >= baseY && isWater(level, n) && visited.add(n)) {
                queue.add(n);
            }
        }
        if (queue.isEmpty()) {
            return PressureResult.EMPTY;
        }

        int maxHeight = 0;
        int volume = 0;

        while (!queue.isEmpty() && volume < MAX_BLOCKS) {
            BlockPos p = queue.poll();
            volume++;

            int h = p.getY() - baseY;
            if (h > maxHeight) {
                maxHeight = h;
            }

            for (Direction dir : Direction.values()) {
                BlockPos n = p.relative(dir);
                // Only spread through water at or above the turbine; deeper water adds no pressure.
                if (n.getY() < baseY) {
                    continue;
                }
                if (n.getY() - baseY > MAX_HEIGHT) {
                    continue;
                }
                if (visited.contains(n)) {
                    continue;
                }
                if (!isWater(level, n)) {
                    continue;
                }
                visited.add(n);
                queue.add(n);
            }
        }

        return new PressureResult(maxHeight, volume);
    }

    private static boolean isWater(Level level, BlockPos pos) {
        // Counts both source and flowing water (and waterlogged blocks).
        return level.getFluidState(pos).is(FluidTags.WATER);
    }
}
