package moritz.createturbine.content;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

/**
 * Computes the "water pressure" above a turbine via a flood fill through connected
 * water blocks, starting directly above the turbine and only ever moving to blocks
 * at or above that level (i.e. water "above / diagonally above" the turbine).
 *
 * The result carries both the column height (used now) and the connected volume
 * (carried already, so a volume term can be switched on later without touching
 * the sampling logic).
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
        BlockPos start = turbinePos.above();
        if (!isWater(level, start)) {
            return PressureResult.EMPTY;
        }

        final int baseY = turbinePos.getY();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

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
                // Only spread through water that is at or above the start of the column.
                // Diagonally-offset pools are still reached via up + sideways paths.
                if (n.getY() <= baseY) {
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
