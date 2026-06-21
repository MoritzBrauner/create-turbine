package moritz.createturbine.registry;

import moritz.createturbine.CreateTurbine;
import moritz.createturbine.content.WaterTurbineBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CTBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateTurbine.MODID);

    public static final DeferredBlock<WaterTurbineBlock> WATER_TURBINE =
            BLOCKS.registerBlock(
                    "water_turbine",
                    WaterTurbineBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
