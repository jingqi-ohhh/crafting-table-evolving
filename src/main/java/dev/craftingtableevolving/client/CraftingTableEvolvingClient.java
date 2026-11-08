package dev.craftingtableevolving.client;

import dev.craftingtableevolving.block.ModBlocks;
import dev.craftingtableevolving.storage.CraftingTableStorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class CraftingTableEvolvingClient implements ClientModInitializer {
    public static KeyBinding storageModifier;

    @Override
    public void onInitializeClient() {
        // The hanging chains use a cutout texture.
        for (Block wallTable : ModBlocks.WALL_TABLES) {
            BlockRenderLayerMap.INSTANCE.putBlock(
                wallTable, RenderLayer.getCutout()
            );
        }

        storageModifier = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.crafting_table_evolving.storage_modifier",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.crafting_table_evolving"
        ));

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!CraftingTableStorage.isStorageTableItem(stack)) {
                return;
            }

            int slots = CraftingTableStorage.carriedSlotCount(stack);
            if (slots == 0) {
                return;
            }

            lines.add(Text.translatable(
                "tooltip.crafting_table_evolving.stored_items", slots
            ).formatted(Formatting.GRAY));
            lines.add(Text.translatable(
                "tooltip.crafting_table_evolving.stored_items_warning"
            ).formatted(Formatting.RED));
        });
    }

    /**
     * Checks the physical state of the key bound to the storage modifier.
     * Vanilla only updates one KeyBinding per physical key, so two bindings
     * sharing a key can leave this binding's isPressed() state stuck.
     */
    public static boolean isStorageModifierPressed() {
        KeyBinding binding = storageModifier;
        if (binding == null || binding.isUnbound()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getWindow() == null) {
            return false;
        }

        InputUtil.Key key = KeyBindingHelper.getBoundKeyOf(binding);
        long handle = client.getWindow().getHandle();

        if (key.getCategory() == InputUtil.Type.KEYSYM) {
            return InputUtil.isKeyPressed(handle, key.getCode());
        }
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(handle, key.getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
}
