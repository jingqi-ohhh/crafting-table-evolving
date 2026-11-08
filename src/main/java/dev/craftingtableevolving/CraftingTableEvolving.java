package dev.craftingtableevolving;

import dev.craftingtableevolving.block.ModBlocks;
import dev.craftingtableevolving.item.ModItems;
import dev.craftingtableevolving.network.StorageNetworking;
import dev.craftingtableevolving.storage.CraftingTableStorageEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftingTableEvolving implements ModInitializer {
    public static final String MOD_ID = "crafting_table_evolving";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModItems.register();
        ModBlocks.register();
        CraftingTableStorageEvents.register();
        StorageNetworking.registerServerReceiver();
        LOGGER.info("Crafting Table Evolving initialized");
    }
}
