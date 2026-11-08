package dev.craftingtableevolving.block;

import java.util.ArrayList;
import java.util.List;

import dev.craftingtableevolving.CraftingTableEvolving;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    /**
     * Woods that get their own crafting table and wall table. Oak is covered by
     * the vanilla crafting table and the original wall crafting table.
     */
    public static final String[] WOOD_TYPES = {
        "spruce", "birch", "jungle", "acacia", "dark_oak",
        "mangrove", "cherry", "bamboo", "crimson", "warped"
    };

    public static final Block WALL_CRAFTING_TABLE = new WallCraftingTableBlock(
        AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE).nonOpaque()
    );

    public static final Item WALL_CRAFTING_TABLE_ITEM = new BlockItem(
        WALL_CRAFTING_TABLE,
        new Item.Settings()
    );

    public static final List<Block> WALL_TABLES = new ArrayList<>();
    public static final List<Item> WALL_TABLE_ITEMS = new ArrayList<>();
    public static final List<Block> CRAFTING_TABLES = new ArrayList<>();
    public static final List<Item> CRAFTING_TABLE_ITEMS = new ArrayList<>();

    static {
        WALL_TABLES.add(WALL_CRAFTING_TABLE);
        WALL_TABLE_ITEMS.add(WALL_CRAFTING_TABLE_ITEM);
    }

    private ModBlocks() {
    }

    public static void register() {
        registerBlock("wall_crafting_table", WALL_CRAFTING_TABLE, WALL_CRAFTING_TABLE_ITEM);

        for (String wood : WOOD_TYPES) {
            Block table = new ModCraftingTableBlock(
                AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE)
            );
            Item tableItem = new BlockItem(table, new Item.Settings());
            registerBlock(wood + "_crafting_table", table, tableItem);
            CRAFTING_TABLES.add(table);
            CRAFTING_TABLE_ITEMS.add(tableItem);

            Block wall = new WallCraftingTableBlock(
                AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE).nonOpaque()
            );
            Item wallItem = new BlockItem(wall, new Item.Settings());
            registerBlock(wood + "_wall_crafting_table", wall, wallItem);
            WALL_TABLES.add(wall);
            WALL_TABLE_ITEMS.add(wallItem);
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            for (Item item : CRAFTING_TABLE_ITEMS) {
                entries.add(item);
            }
            for (Item item : WALL_TABLE_ITEMS) {
                entries.add(item);
            }
        });
    }

    private static void registerBlock(String id, Block block, Item item) {
        Identifier identifier = CraftingTableEvolving.id(id);
        Registry.register(Registries.BLOCK, identifier, block);
        Registry.register(Registries.ITEM, identifier, item);
    }
}
