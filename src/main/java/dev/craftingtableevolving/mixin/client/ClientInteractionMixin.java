package dev.craftingtableevolving.mixin.client;

import dev.craftingtableevolving.client.CraftingTableEvolvingClient;
import dev.craftingtableevolving.network.StorageNetworking;
import dev.craftingtableevolving.storage.CraftingTableStorage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientInteractionMixin {
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void craftingtableevolving$openStorage(
        ClientPlayerEntity player,
        Hand hand,
        BlockHitResult hitResult,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!CraftingTableEvolvingClient.isStorageModifierPressed()) {
            return;
        }

        if (CraftingTableStorage.isStorageTable(
            player.getWorld().getBlockState(hitResult.getBlockPos())
        )) {
            ClientPlayNetworking.send(
                StorageNetworking.OPEN_STORAGE_CHANNEL,
                PacketByteBufs.create().writeBlockPos(hitResult.getBlockPos())
            );
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
