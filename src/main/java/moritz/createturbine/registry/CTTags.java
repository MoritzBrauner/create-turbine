package moritz.createturbine.registry;

import moritz.createturbine.CreateTurbine;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class CTTags {

    /**
     * Blocks that can be applied to the turbine as wheel material by right-clicking (the turbine
     * counterpart to Create's planks-only rule). Data-driven, so packs can extend the list.
     */
    public static final TagKey<Block> TURBINE_MATERIALS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(CreateTurbine.MODID, "turbine_materials"));

    private CTTags() {
    }
}
