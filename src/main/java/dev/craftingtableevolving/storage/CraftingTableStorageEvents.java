package dev.craftingtableevolving.storage;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

public final class CraftingTableStorageEvents {
    private CraftingTableStorageEvents() {
    }

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerWorld serverWorld
                && CraftingTableStorage.isStorageTable(state)
                && CraftingTableStorage.hasStoredItems(serverWorld, pos)) {
                CraftingTableStorage.markPlayerBreak(serverWorld, pos);
            }

            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)
                || !CraftingTableStorage.isStorageTable(state)
                || !CraftingTableStorage.isPlayerBreakPending(serverWorld, pos)) {
                return;
            }

            if (player.isCreative()) {
                ItemStack carried = CraftingTableStorage.takeWithContents(
                    serverWorld, pos, state
                );
                CraftingTableStorage.clearPlayerBreak(serverWorld, pos);

                if (!carried.isEmpty()) {
                    player.getInventory().insertStack(carried);
                    if (!carried.isEmpty()) {
                        player.dropItem(carried, false);
                    }
                }
            } else {
                // Survival drops are handled by the Block.dropStacks mixin.
                // If something else prevented that from running, recover the
                // contents on the next tick instead of leaving them behind.
                serverWorld.getServer().execute(() -> {
                    if (!CraftingTableStorage.isPlayerBreakPending(serverWorld, pos)) {
                        return;
                    }

                    CraftingTableStorage.clearPlayerBreak(serverWorld, pos);
                    if (serverWorld.getBlockState(pos).getBlock()
                        == state.getBlock()) {
                        return;
                    }

                    ItemStack carried = CraftingTableStorage.takeWithContents(
                        serverWorld, pos, state
                    );
                    if (!carried.isEmpty()) {
                        Block.dropStack(serverWorld, pos, carried);
                    }
                });
            }
        });

        PlayerBlockBreakEvents.CANCELED.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerWorld serverWorld
                && CraftingTableStorage.isStorageTable(state)) {
                CraftingTableStorage.clearPlayerBreak(serverWorld, pos);
            }
        });
    }
}
