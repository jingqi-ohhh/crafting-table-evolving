package dev.craftingtableevolving.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.craftingtableevolving.block.ModCraftingTableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CraftingTableStorage {
    /**
     * NBT key used on a crafting table item to carry the table's contents
     * while the block is in item form.
     */
    public static final String CARRY_NBT_KEY = "CraftingTableEvolvingStorage";
    private static final String CARRY_ITEMS_KEY = "Items";

    /**
     * Player breaks in progress. A marker tells onStateReplaced not to scatter
     * the contents, because the normal block drop will be replaced by an item
     * that carries them.
     */
    private static final Map<RegistryKey<World>, Set<BlockPos>> PENDING_PLAYER_BREAKS =
        new HashMap<>();

    private CraftingTableStorage() {
    }

    /**
     * Whether this block is a crafting table that uses the mod's 27-slot
     * storage. The wall tables are crafting-only, so they are excluded.
     */
    public static boolean isStorageTable(BlockState state) {
        return state.isOf(Blocks.CRAFTING_TABLE)
            || state.getBlock() instanceof ModCraftingTableBlock;
    }

    public static boolean isStorageTable(Block block) {
        return block == Blocks.CRAFTING_TABLE
            || block instanceof ModCraftingTableBlock;
    }

    public static boolean isStorageTableItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
            && isStorageTable(blockItem.getBlock());
    }

    public static void onCraftingTableRemoved(ServerWorld world, BlockPos pos) {
        CraftingTableStorageState state = CraftingTableStorageState.getIfPresent(world);
        if (state == null) {
            return;
        }

        CraftingTableInventory inventory = state.get(pos);
        if (inventory == null) {
            return;
        }

        closeViewers(world, inventory);
        ItemScatterer.spawn(world, pos, inventory);
        inventory.clear();
        state.remove(pos);
    }

    public static boolean hasStoredItems(ServerWorld world, BlockPos pos) {
        CraftingTableStorageState state = CraftingTableStorageState.getIfPresent(world);
        if (state == null) {
            return false;
        }

        CraftingTableInventory inventory = state.get(pos);
        return inventory != null && !inventory.isEmpty();
    }

    /**
     * Moves the table's contents onto the matching crafting table item and
     * removes the block's storage entry.
     */
    public static ItemStack takeWithContents(
        ServerWorld world,
        BlockPos pos,
        BlockState state
    ) {
        CraftingTableStorageState storageState =
            CraftingTableStorageState.getIfPresent(world);
        if (storageState == null) {
            return ItemStack.EMPTY;
        }

        CraftingTableInventory inventory = storageState.get(pos);
        if (inventory == null || inventory.isEmpty()) {
            storageState.remove(pos);
            return ItemStack.EMPTY;
        }

        closeViewers(world, inventory);

        ItemStack carried = new ItemStack(state.getBlock().asItem());
        NbtCompound data = new NbtCompound();
        data.put(CARRY_ITEMS_KEY, inventory.writeItemsNbt());
        carried.setSubNbt(CARRY_NBT_KEY, data);

        inventory.clear();
        storageState.remove(pos);
        return carried;
    }

    /**
     * Restores contents carried by an item into the storage at the position
     * where that item was just placed. Any overflow is dropped on the ground.
     */
    public static void restoreFromItem(ServerWorld world, BlockPos pos, ItemStack stack) {
        NbtCompound data = stack.getSubNbt(CARRY_NBT_KEY);
        if (data == null) {
            return;
        }

        CraftingTableStorageState state = CraftingTableStorageState.get(world);
        CraftingTableInventory inventory = state.getOrCreate(pos);
        NbtList items = data.getList(CARRY_ITEMS_KEY, NbtElement.COMPOUND_TYPE);

        for (int slot = 0; slot < Math.min(items.size(), CraftingTableInventory.SIZE); slot++) {
            NbtCompound itemNbt = items.getCompound(slot);
            if (!itemNbt.contains("id", NbtElement.STRING_TYPE)) {
                continue;
            }

            ItemStack restored = ItemStack.fromNbt(itemNbt);
            if (restored.isEmpty()) {
                continue;
            }

            ItemStack leftover = inventory.insert(restored);
            if (!leftover.isEmpty()) {
                Block.dropStack(world, pos, leftover);
            }
        }

        if (inventory.isEmpty()) {
            state.remove(pos);
        }

        // Remove the data from the item so that placing it again cannot
        // duplicate the contents (especially in creative mode).
        stack.removeSubNbt(CARRY_NBT_KEY);
    }

    /**
     * Number of non-empty storage slots carried by the given item stack.
     */
    public static int carriedSlotCount(ItemStack stack) {
        NbtCompound data = stack.getSubNbt(CARRY_NBT_KEY);
        if (data == null) {
            return 0;
        }

        NbtList items = data.getList(CARRY_ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        int count = 0;
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.getCompound(slot).contains("id", NbtElement.STRING_TYPE)) {
                count++;
            }
        }

        return count;
    }

    public static void markPlayerBreak(ServerWorld world, BlockPos pos) {
        PENDING_PLAYER_BREAKS
            .computeIfAbsent(world.getRegistryKey(), key -> new HashSet<>())
            .add(pos);
    }

    public static boolean isPlayerBreakPending(ServerWorld world, BlockPos pos) {
        Set<BlockPos> positions = PENDING_PLAYER_BREAKS.get(world.getRegistryKey());
        return positions != null && positions.contains(pos);
    }

    public static void clearPlayerBreak(ServerWorld world, BlockPos pos) {
        RegistryKey<World> key = world.getRegistryKey();
        Set<BlockPos> positions = PENDING_PLAYER_BREAKS.get(key);
        if (positions == null) {
            return;
        }

        positions.remove(pos);
        if (positions.isEmpty()) {
            PENDING_PLAYER_BREAKS.remove(key);
        }
    }

    private static void closeViewers(ServerWorld world, CraftingTableInventory inventory) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.currentScreenHandler instanceof GenericContainerScreenHandler handler
                && handler.getInventory() == inventory) {
                player.closeHandledScreen();
            }
        }
    }
}
