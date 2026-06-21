package moritz.createturbine.registry;

import moritz.createturbine.CreateTurbine;

import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateTurbine.MODID);

    public static final DeferredItem<BlockItem> WATER_TURBINE =
            ITEMS.registerSimpleBlockItem("water_turbine", CTBlocks.WATER_TURBINE);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
