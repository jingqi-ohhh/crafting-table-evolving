package dev.craftingtableevolving.item;

import dev.craftingtableevolving.CraftingTableEvolving;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModItems {
    public static final Item PORTABLE_CRAFTING_TABLE =
        new PortableCraftingTableItem(new Item.Settings().maxCount(1));

    private ModItems() {
    }

    public static void register() {
        Registry.register(
            Registries.ITEM,
            CraftingTableEvolving.id("portable_crafting_table"),
            PORTABLE_CRAFTING_TABLE
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
            .register(entries -> entries.add(PORTABLE_CRAFTING_TABLE));
    }
}
