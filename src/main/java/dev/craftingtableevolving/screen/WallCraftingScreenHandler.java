package dev.craftingtableevolving.screen;

import dev.craftingtableevolving.block.WallCraftingTableBlock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Crafting screen for the wall-mounted table. The vanilla crafting handler
 * only accepts a vanilla crafting table at the context position, so its
 * canUse check is replaced here.
 */
public class WallCraftingScreenHandler extends CraftingScreenHandler {
    private final World world;
    private final BlockPos pos;

    public WallCraftingScreenHandler(
        int syncId,
        PlayerInventory playerInventory,
        World world,
        BlockPos pos
    ) {
        super(syncId, playerInventory, ScreenHandlerContext.create(world, pos));
        this.world = world;
        this.pos = pos;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return world.getBlockState(pos).getBlock() instanceof WallCraftingTableBlock
            && player.squaredDistanceTo(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5
            ) <= 64.0;
    }
}
