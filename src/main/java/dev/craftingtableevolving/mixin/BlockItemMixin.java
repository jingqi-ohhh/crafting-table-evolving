package dev.craftingtableevolving.mixin;

import dev.craftingtableevolving.storage.CraftingTableStorage;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void craftingtableevolving$restoreTableContents(
        ItemPlacementContext context,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!cir.getReturnValue().isAccepted()) {
            return;
        }

        World world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        BlockPos pos = context.getBlockPos();
        if (!CraftingTableStorage.isStorageTable(serverWorld.getBlockState(pos))) {
            return;
        }

        ItemStack stack = context.getStack();
        if (stack.getSubNbt(CraftingTableStorage.CARRY_NBT_KEY) == null) {
            return;
        }

        CraftingTableStorage.restoreFromItem(serverWorld, pos, stack);
    }
}
