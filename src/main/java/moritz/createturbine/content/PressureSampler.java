package moritz.createturbine.content;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Computes the "water pressure" acting on a turbine via a flood fill through the connected body
 * of water that touches the block.
 *
 * The fill starts from every water block adjacent to the turbine at or above its own level (any
 * side, not just the top) and spreads through connected water — source, flowing and waterlogged
 * blocks all count, so an offset/diagonal pipe drives the turbine as long as the water is not
 * interrupted. The search is best-first by height (highest frontier block expanded first), so
 * the block budget is spent climbing the column rather than flooding wide pools sideways.
 *
 * The pressure height is how far the highest connected water block sits above the turbine,
 * clamped to [1, maxHeight]: any touching water counts as at least a 1-block column, and water
 * above maxHeight adds nothing.
 */
public final class PressureSampler {

    /** Hard cap on the number of water blocks visited (caps CPU cost / prevents lag). */
    public static final int MAX_BLOCKS = 1024;

    private PressureSampler() {}

    public record PressureResult(int height, int volume) {
        public static final PressureResult EMPTY = new PressureResult(0, 0);
    }

    public static PressureResult sample(Level level, BlockPos turbinePos, int maxHeight) {
        final int baseY = turbinePos.getY();
        final int capY = baseY + Math.max(1, maxHeight);

        Set<BlockPos> visited = new HashSet<>();
        PriorityQueue<BlockPos> queue =
                new PriorityQueue<>(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed());

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

        int highestY = baseY;
        int volume = 0;

        while (!queue.isEmpty() && volume < MAX_BLOCKS) {
            BlockPos p = queue.poll();
            volume++;

            if (p.getY() > highestY) {
                highestY = p.getY();
                if (highestY >= capY) {
                    // Reached the height cap — nothing above can add pressure, stop searching.
                    break;
                }
            }

            for (Direction dir : Direction.values()) {
                BlockPos n = p.relative(dir);
                // Only spread through water between turbine level and the cap.
                if (n.getY() < baseY || n.getY() > capY) {
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

        return new PressureResult(Mth.clamp(highestY - baseY, 1, maxHeight), volume);
    }

    private static boolean isWater(Level level, BlockPos pos) {
        // Counts source and flowing water as well as waterlogged blocks.
        return level.getFluidState(pos).is(FluidTags.WATER);
    }
}
