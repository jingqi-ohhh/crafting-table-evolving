package dev.craftingtableevolving.screen;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Crafting screen for the wood-variant crafting tables. Like the wall table,
 * the vanilla handler's canUse check would reject these blocks.
 */
public class ModCraftingScreenHandler extends CraftingScreenHandler {
    private final World world;
    private final BlockPos pos;
    private final Block block;

    public ModCraftingScreenHandler(
        int syncId,
        PlayerInventory playerInventory,
        World world,
        BlockPos pos,
        Block block
    ) {
        super(syncId, playerInventory, ScreenHandlerContext.create(world, pos));
        this.world = world;
        this.pos = pos;
        this.block = block;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return world.getBlockState(pos).isOf(block)
            && player.squaredDistanceTo(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5
            ) <= 64.0;
    }
}
