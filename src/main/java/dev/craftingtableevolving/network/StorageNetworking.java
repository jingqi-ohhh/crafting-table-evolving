package dev.craftingtableevolving.network;

import dev.craftingtableevolving.CraftingTableEvolving;
import dev.craftingtableevolving.storage.CraftingTableInventory;
import dev.craftingtableevolving.storage.CraftingTableStorage;
import dev.craftingtableevolving.storage.CraftingTableStorageState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class StorageNetworking {
    public static final Identifier OPEN_STORAGE_CHANNEL =
        CraftingTableEvolving.id("open_storage");

    private static final Text STORAGE_TITLE =
        Text.translatable("container.crafting_table_evolving.crafting_table_storage");

    private StorageNetworking() {
    }

    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(
            OPEN_STORAGE_CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                BlockPos pos = buf.readBlockPos();
                server.execute(() -> openStorage(player, pos));
            }
        );
    }

    private static void openStorage(ServerPlayerEntity player, BlockPos pos) {
        if (player.isSpectator()) {
            return;
        }

        ServerWorld world = player.getServerWorld();
        if (!CraftingTableStorage.isStorageTable(world.getBlockState(pos))) {
            return;
        }

        if (player.squaredDistanceTo(Vec3d.ofCenter(pos)) > 64.0) {
            return;
        }

        CraftingTableStorageState state = CraftingTableStorageState.get(world);
        CraftingTableInventory inventory = state.getOrCreate(pos);

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, p) ->
                GenericContainerScreenHandler.createGeneric9x3(
                    syncId,
                    playerInventory,
                    inventory
                ),
            STORAGE_TITLE
        ));
    }
}
