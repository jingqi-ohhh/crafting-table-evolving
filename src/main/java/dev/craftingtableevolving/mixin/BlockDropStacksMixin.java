package dev.craftingtableevolving.mixin;

import dev.craftingtableevolving.storage.CraftingTableStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockDropStacksMixin {
    @Inject(
        method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;"
            + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;"
            + "Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void craftingtableevolving$dropStoredContents(
        BlockState state,
        World world,
        BlockPos pos,
        BlockEntity blockEntity,
        Entity entity,
        ItemStack tool,
        CallbackInfo ci
    ) {
        if (!(world instanceof ServerWorld serverWorld)
            || !CraftingTableStorage.isStorageTable(state)
            || !CraftingTableStorage.isPlayerBreakPending(serverWorld, pos)) {
            return;
        }

        ItemStack carried = CraftingTableStorage.takeWithContents(
            serverWorld, pos, state
        );
        CraftingTableStorage.clearPlayerBreak(serverWorld, pos);

        if (!carried.isEmpty()) {
            Block.dropStack(world, pos, carried);
            ci.cancel();
        }
    }
}
