package dev.craftingtableevolving.screen;

import java.util.Optional;
import java.util.function.BiFunction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortableCraftingScreenHandler extends CraftingScreenHandler {
    public PortableCraftingScreenHandler(
        int syncId,
        PlayerInventory playerInventory,
        ServerPlayerEntity player
    ) {
        super(syncId, playerInventory, createContext(player));
    }

    private static ScreenHandlerContext createContext(ServerPlayerEntity player) {
        return new ScreenHandlerContext() {
            @Override
            public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
                return Optional.ofNullable(
                    getter.apply(player.getServerWorld(), player.getBlockPos())
                );
            }
        };
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // A portable table is not tied to a block, so it must stay open anywhere.
        return true;
    }
}
