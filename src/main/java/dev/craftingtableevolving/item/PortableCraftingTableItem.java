package dev.craftingtableevolving.item;

import dev.craftingtableevolving.screen.PortableCraftingScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PortableCraftingTableItem extends Item {
    private static final Text TITLE =
        Text.translatable("container.crafting_table_evolving.portable_crafting_table");

    public PortableCraftingTableItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, player) ->
                    new PortableCraftingScreenHandler(syncId, playerInventory, serverPlayer),
                TITLE
            ));
        }

        return TypedActionResult.success(stack);
    }
}
