package dev.craftingtableevolving.storage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

public class CraftingTableInventory implements Inventory {
    public static final int SIZE = 27;

    private final DefaultedList<ItemStack> stacks =
        DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
    private final Runnable dirtyCallback;

    public CraftingTableInventory(Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(stacks, slot, amount);
        if (!result.isEmpty()) {
            dirtyCallback.run();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(stacks, slot);
        if (!result.isEmpty()) {
            dirtyCallback.run();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);

        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        dirtyCallback.run();
    }

    @Override
    public void markDirty() {
        dirtyCallback.run();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        for (int slot = 0; slot < SIZE; slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        dirtyCallback.run();
    }

    public NbtList writeItemsNbt() {
        NbtList items = new NbtList();

        for (int slot = 0; slot < SIZE; slot++) {
            NbtCompound itemNbt = new NbtCompound();
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty()) {
                stack.writeNbt(itemNbt);
            }
            items.add(itemNbt);
        }

        return items;
    }

    public void readItemsNbt(NbtList items) {
        for (int slot = 0; slot < SIZE; slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }

        for (int slot = 0; slot < Math.min(items.size(), SIZE); slot++) {
            NbtCompound itemNbt = items.getCompound(slot);
            if (itemNbt.contains("id", NbtElement.STRING_TYPE)) {
                ItemStack stack = ItemStack.fromNbt(itemNbt);
                if (!stack.isEmpty()) {
                    stacks.set(slot, stack);
                }
            }
        }

        dirtyCallback.run();
    }

    /**
     * Inserts as much of the given stack as possible, merging into existing
     * stacks first and then using empty slots.
     *
     * @return the part of the stack that did not fit
     */
    public ItemStack insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < SIZE && !stack.isEmpty(); slot++) {
            ItemStack existing = stacks.get(slot);
            if (existing.isEmpty() || !ItemStack.canCombine(existing, stack)) {
                continue;
            }

            int space = existing.getMaxCount() - existing.getCount();
            if (space <= 0) {
                continue;
            }

            int moved = Math.min(space, stack.getCount());
            existing.increment(moved);
            stack.decrement(moved);
            dirtyCallback.run();
        }

        for (int slot = 0; slot < SIZE && !stack.isEmpty(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                continue;
            }

            int moved = Math.min(stack.getCount(), stack.getMaxCount());
            ItemStack copy = stack.copy();
            copy.setCount(moved);
            stacks.set(slot, copy);
            stack.decrement(moved);
            dirtyCallback.run();
        }

        return stack;
    }
}
