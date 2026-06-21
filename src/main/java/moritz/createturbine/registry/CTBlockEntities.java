package moritz.createturbine.registry;

import moritz.createturbine.CreateTurbine;
import moritz.createturbine.content.WaterTurbineBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CTBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateTurbine.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterTurbineBlockEntity>> WATER_TURBINE =
            BLOCK_ENTITIES.register("water_turbine", () -> BlockEntityType.Builder.of(
                    // referenced lazily at BE-creation time, so the self-reference is safe
                    (pos, state) -> new WaterTurbineBlockEntity(CTBlockEntities.WATER_TURBINE.get(), pos, state),
                    CTBlocks.WATER_TURBINE.get()
            ).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
