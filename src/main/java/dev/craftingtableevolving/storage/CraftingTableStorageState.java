package dev.craftingtableevolving.storage;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class CraftingTableStorageState extends PersistentState {
    private static final String DATA_NAME = "crafting_table_evolving_crafting_table_storage";
    private static final String TABLES_KEY = "tables";
    private static final String POS_KEY = "pos";
    private static final String ITEMS_KEY = "items";

    private final Map<BlockPos, CraftingTableInventory> inventories = new HashMap<>();

    public static CraftingTableStorageState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
            CraftingTableStorageState::fromNbt,
            CraftingTableStorageState::new,
            DATA_NAME
        );
    }

    public static CraftingTableStorageState getIfPresent(ServerWorld world) {
        return world.getPersistentStateManager().get(
            CraftingTableStorageState::fromNbt,
            DATA_NAME
        );
    }

    public static CraftingTableStorageState fromNbt(NbtCompound nbt) {
        CraftingTableStorageState state = new CraftingTableStorageState();
        NbtList tables = nbt.getList(TABLES_KEY, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < tables.size(); i++) {
            NbtCompound table = tables.getCompound(i);
            BlockPos pos = BlockPos.fromLong(table.getLong(POS_KEY));
            CraftingTableInventory inventory =
                new CraftingTableInventory(state::markDirty);
            NbtList items = table.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
            inventory.readItemsNbt(items);

            state.inventories.put(pos, inventory);
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList tables = new NbtList();

        for (Map.Entry<BlockPos, CraftingTableInventory> entry : inventories.entrySet()) {
            NbtCompound table = new NbtCompound();
            table.putLong(POS_KEY, entry.getKey().asLong());
            table.put(ITEMS_KEY, entry.getValue().writeItemsNbt());
            tables.add(table);
        }

        nbt.put(TABLES_KEY, tables);
        return nbt;
    }

    public CraftingTableInventory getOrCreate(BlockPos pos) {
        return inventories.computeIfAbsent(pos, p -> {
            CraftingTableInventory inventory =
                new CraftingTableInventory(this::markDirty);
            this.markDirty();
            return inventory;
        });
    }

    public CraftingTableInventory get(BlockPos pos) {
        return inventories.get(pos);
    }

    public CraftingTableInventory remove(BlockPos pos) {
        CraftingTableInventory inventory = inventories.remove(pos);
        if (inventory != null) {
            this.markDirty();
        }
        return inventory;
    }
}
