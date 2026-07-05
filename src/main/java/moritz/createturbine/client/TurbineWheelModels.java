package moritz.createturbine.client;

import java.util.List;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedModelHelper;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Bakes the turbine wheel model for a given material block state.
 *
 * Template is Create's water wheel OBJ ({@link AllPartialModels#WATER_WHEEL}), whose blades, rim
 * and spokes are textured with oak planks and whose hub drum is textured with the oak log side.
 * Unlike Create's {@code WaterWheelRenderer.generateModel} (which only accepts planks and derives
 * a matching log), this swap works with ANY block: blades take the material's side texture
 * (south face), the hub drum its top texture — for cube_all blocks like Create's casings both
 * are the same, for cube_column blocks like industrial iron the drum gets the framed top plate.
 */
public final class TurbineWheelModels {

    // Template textures of the wheel OBJ (blades/rim/spokes and hub drum respectively).
    private static final ResourceLocation OAK_PLANKS_TEXTURE =
            ResourceLocation.withDefaultNamespace("block/oak_planks");
    private static final ResourceLocation OAK_LOG_TEXTURE =
            ResourceLocation.withDefaultNamespace("block/oak_log");

    private TurbineWheelModels() {
    }

    public static BakedModel generateModel(BlockState material) {
        Reference2ReferenceOpenHashMap<TextureAtlasSprite, TextureAtlasSprite> map =
                new Reference2ReferenceOpenHashMap<>();
        map.put(atlasSprite(OAK_PLANKS_TEXTURE), getSpriteOnSide(material, Direction.SOUTH));
        map.put(atlasSprite(OAK_LOG_TEXTURE), getSpriteOnSide(material, Direction.UP));
        return BakedModelHelper.generateModel(AllPartialModels.WATER_WHEEL.get(), map::get);
    }

    /**
     * Resolves the template sprite live from the block atlas at generation time. Deliberately
     * NOT a catnip StitchedSprite: those are only (re)filled during texture stitching, and this
     * class is first loaded when the first wheel renders — after the stitch — so a StitchedSprite
     * created here would stay null and silently disable the whole swap. All generated models are
     * cached in caches that clear on resource reload, so a live lookup is always current.
     */
    private static TextureAtlasSprite atlasSprite(ResourceLocation texture) {
        return Minecraft.getInstance().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                .getSprite(texture);
    }

    // Copy of Create's private WaterWheelRenderer.getSpriteOnSide.
    private static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        RandomSource random = RandomSource.create(42L);
        List<BakedQuad> quads = model.getQuads(state, side, random, ModelData.EMPTY, null);
        if (!quads.isEmpty()) {
            return quads.get(0).getSprite();
        }
        random.setSeed(42L);
        for (BakedQuad quad : model.getQuads(state, null, random, ModelData.EMPTY, null)) {
            if (quad.getDirection() == side) {
                return quad.getSprite();
            }
        }
        return model.getParticleIcon(ModelData.EMPTY);
    }
}
