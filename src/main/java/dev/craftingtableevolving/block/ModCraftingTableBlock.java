package dev.craftingtableevolving.block;

import dev.craftingtableevolving.screen.ModCraftingScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A crafting table made from a non-oak wood type. Vanilla's crafting screen
 * handler only accepts {@code minecraft:crafting_table}, so this block opens
 * the mod's own handler instead.
 */
public class ModCraftingTableBlock extends CraftingTableBlock {
    public ModCraftingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hit
    ) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, p) -> new ModCraftingScreenHandler(
                syncId,
                playerInventory,
                world,
                pos,
                state.getBlock()
            ),
            Text.translatable(state.getBlock().getTranslationKey())
        ));
        return ActionResult.CONSUME;
    }
}
