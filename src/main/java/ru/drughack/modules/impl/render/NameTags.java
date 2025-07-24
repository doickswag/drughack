package ru.drughack.modules.impl.render;

import lombok.AllArgsConstructor;
import net.minecraft.util.Identifier;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IItemRenderState;
import ru.drughack.api.mixins.accesors.IItemRenderer;
import ru.drughack.api.mixins.accesors.ILayerRenderState;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.world.EntityUtils;
import ru.drughack.commands.impl.FakePlayerCommand;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Comparator;

public class NameTags extends Module {

    public Setting<Boolean> gameMode = new Setting<>("GameMode", false);
    public Setting<Boolean> ping = new Setting<>("Ping",  true);
    public Setting<Boolean> entityId = new Setting<>("EntityID", false);
    public Setting<Boolean> health = new Setting<>("Health", true);
    public Setting<Boolean> totemPops = new Setting<>("TotemPops", true);
    public Setting<Boolean> antiBot = new Setting<>("AntiBot", false);
    public Setting<Boolean> self = new Setting<>("Self", false);
    public Setting<Boolean> clientCheck = new Setting<>("ClientCheck", false);
    public Setting<Boolean> items = new Setting<>("Items",  true);
    public Setting<Boolean> enchantments = new Setting<>("Enchantments", false);
    public Setting<Boolean> durability = new Setting<>("Durability", true);
    public Setting<Boolean> itemName = new Setting<>("ItemName", true);
    public Setting<Integer> scale = new Setting<>("Scale", 30, 10, 100);
    public Setting<Border> border = new Setting<>("Border", Border.Both);

    public NameTags() {
        super("NameTags", "tags for players", Category.Render);
    }

    private final ItemRenderState itemRenderState = new ItemRenderState();

    @EventHandler
    public void onRenderWorld(EventRender3D.Post event) {
        MatrixStack matrices = event.getMatrices();
        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();

        for (PlayerEntity player : mc.world.getPlayers().stream().sorted(Comparator.comparing(p -> -mc.player.distanceTo(p))).toList()) {
            if (!self.getValue() && player == mc.player) continue;
            if (antiBot.getValue() && EntityUtils.isBot(player)) continue;
            if (!Renderer3D.isFrustumVisible(player.getBoundingBox())) continue;
            double x = MathHelper.lerp(event.getTickDelta(), player.lastRenderX, player.getX());
            double y = MathHelper.lerp(event.getTickDelta(), player.lastRenderY, player.getY()) + (player.isSneaking() ? 1.9f : 2.1f);
            double z = MathHelper.lerp(event.getTickDelta(), player.lastRenderZ, player.getZ());
            Vec3d vec3d = new Vec3d(x - mc.getEntityRenderDispatcher().camera.getPos().x, y - mc.getEntityRenderDispatcher().camera.getPos().y, z - mc.getEntityRenderDispatcher().camera.getPos().z);
            float distance = (float) Math.sqrt(mc.getEntityRenderDispatcher().camera.getPos().squaredDistanceTo(x, y, z));
            float scaling = 0.0018f + (scale.getValue() / 10000.0f) * distance;
            if (distance <= 8.0) scaling = 0.0245f;
            matrices.push();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            matrices.scale(scaling, -scaling, scaling);
            String text = player.getName().getString();
            if (gameMode.getValue()) text += " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(player)) + "]";
            if (ping.getValue()) text += " " + EntityUtils.getLatency(player) + "ms";
            if (entityId.getValue()) text += " " + player.getId();
            if (health.getValue()) text += " " + ColorUtils.getHealthColor(player.getHealth() + player.getAbsorptionAmount()) + new DecimalFormat("0.0").format(player.getHealth() + player.getAbsorptionAmount()) + Formatting.RESET;
            int pops = DrugHack.getInstance().getWorldManager().getPoppedTotems().getOrDefault(player.getUuid(), 0);
            if (totemPops.getValue() && pops > 0) text += " " + ColorUtils.getTotemColor(pops) + "-" + pops;
            int width = DrugHack.getInstance().getFontManager().getWidth(text);
            if (border.getValue() == Border.Fill || border.getValue() == Border.Both) Renderer2D.renderQuad(matrices, -width / 2.0f - 1, -DrugHack.getInstance().getFontManager().getHeight() - 1, width / 2.0f + 2, 0, new Color(0, 0, 0, 100));
            if (border.getValue() == Border.Outline || border.getValue() == Border.Both) Renderer2D.renderOutline(matrices, -width / 2.0f - 1, -DrugHack.getInstance().getFontManager().getHeight() - 1, width / 2.0f + 2, 0, new Color(0, 0, 0, 100));

            DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, text, -width / 2, -DrugHack.getInstance().getFontManager().getHeight(), vertexConsumers, FakePlayerCommand.player != null && FakePlayerCommand.player == player ? new Color(225, 0, 70) : player.isSneaking() ? new Color(255, 170, 0) : DrugHack.getInstance().getFriendManager().contains(player.getName().getString()) ? DrugHack.getInstance().getFriendManager().getDefaultFriendColor() : Color.WHITE);

            if (DrugHack.getInstance().getGvobavs().isUser(player.getName().getString()) && clientCheck.getValue()) {
                Renderer2D.renderTexture(event.getMatrices(), (-width / 2.0f) - DrugHack.getInstance().getFontManager().getHeight() - 2f, -DrugHack.getInstance().getFontManager().getHeight() - 1, (-width / 2.0f) - 2f, 0, Identifier.of("drughack", "textures/drughack.png"), Color.WHITE);
            }

            if (DrugHack.getInstance().getGvobavs().isTHUser(player.getName().getString()) && clientCheck.getValue()) {
                Renderer2D.renderTexture(event.getMatrices(), (-width / 2.0f) - DrugHack.getInstance().getFontManager().getHeight() - 15f, -DrugHack.getInstance().getFontManager().getHeight() - 1, (-width / 2.0f) - 15f, 0, Identifier.of("drughack", "textures/thunderhack.png"), Color.WHITE);
            }

            boolean renderedDurability = false;
            boolean renderedItems = false;
            int maxEnchants = 0;

            if (enchantments.getValue()) {
                for (int i = 0; i < 6; i++) {
                    ItemStack stack = getItem(player, i);
                    ItemEnchantmentsComponent component = EnchantmentHelper.getEnchantments(stack);

                    if (!component.getEnchantments().isEmpty()) {
                        int height = (component.getEnchantments().size() * DrugHack.getInstance().getFontManager().getHeight() / 2) - 18;
                        if (height > 0 && (height + 1) > maxEnchants) maxEnchants = height + 1;
                    }
                }
            }

            for (int i = 0; i < 6; i++) {
                ItemStack stack = getItem(player, i);
                if (stack.isEmpty()) continue;

                renderedItems = true;

                int stackX = -(108 / 2) + (i * 18) + 1;
                int stackY = -DrugHack.getInstance().getFontManager().getHeight() - 1 - (items.getValue() ? 18 + maxEnchants : 1);

                if (items.getValue()) {
                    ((IItemRenderer) mc.getItemRenderer()).getItemModelManager().update(itemRenderState, stack, ModelTransformationMode.GUI, false, mc.world, mc.player, 0);
                    matrices.push();
                    matrices.translate(stackX + 8, stackY + 8, 0);
                    matrices.scale(16.0F, -16.0F, -0.001f);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    GL11.glDepthFunc(GL11.GL_ALWAYS);

                    for (int j = 0; j < ((IItemRenderState) itemRenderState).getLayerCount(); j++) {
                        ItemRenderState.LayerRenderState layer = ((IItemRenderState) itemRenderState).getLayers()[j];
                        matrices.push();
                        ((ILayerRenderState) layer).getModel().getTransformation().getTransformation(ModelTransformationMode.GUI).apply(false, matrices);
                        matrices.translate(-0.5F, -0.5F, -0.5F);

                        if (((ILayerRenderState) layer).getSpecialModelType() != null) {
                            ((ILayerRenderState) layer).getSpecialModelType().render(((ILayerRenderState) layer).getData(), ModelTransformationMode.GUI, matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, ((ILayerRenderState) layer).getGlint() != ItemRenderState.Glint.NONE);
                        } else if (((ILayerRenderState) layer).getModel() != null) {
                            VertexConsumer vertexConsumer;
                            RenderLayer renderLayer = RenderLayer.getGuiTextured(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

                            if (((ILayerRenderState) layer).getGlint() == ItemRenderState.Glint.SPECIAL) {
                                MatrixStack.Entry entry = matrices.peek().copy();
                                MatrixUtil.scale(entry.getPositionMatrix(), 0.5F);
                                vertexConsumer = IItemRenderer.invokeGetDynamicDisplayGlintConsumer(vertexConsumers, renderLayer, entry);
                            } else {
                                vertexConsumer = ItemRenderer.getItemGlintConsumer(vertexConsumers, renderLayer, true, ((ILayerRenderState) layer).getGlint() != ItemRenderState.Glint.NONE);
                            }

                            IItemRenderer.inovkeRenderBakedItemModel(((ILayerRenderState) layer).getModel(), ((ILayerRenderState) layer).getTints(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, matrices, vertexConsumer);
                        }

                        matrices.pop();
                    }

                    vertexConsumers.draw();
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
                    RenderSystem.disableBlend();
                    matrices.pop();

                    if (stack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                        matrices.push();
                        matrices.translate(stackX, stackY, 0);
                        matrices.scale(0.5f, 0.5f, 1);
                        DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, "God", 0, 0, vertexConsumers, new Color(255, 125, 255));
                        matrices.pop();
                    }

                    if (stack.getCount() != 1) {
                        String count = stack.getCount() + "";
                        matrices.push();
                        matrices.translate(stackX + 17 - DrugHack.getInstance().getFontManager().getWidth(count), stackY + 9, 0);
                        DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, count, 0, 0, vertexConsumers, Color.WHITE);
                        matrices.pop();
                    }
                }

                if (durability.getValue() && stack.isDamageable()) {
                    float green = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
                    float red = 1.0f - green;
                    matrices.push();
                    matrices.translate(stackX, stackY - DrugHack.getInstance().getFontManager().getHeight() / 2f - 1, 0);
                    matrices.scale(0.5f, 0.5f, 1);
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, Math.round(((stack.getMaxDamage() - stack.getDamage()) * 100.0f) / stack.getMaxDamage()) + "%", 0, 0, vertexConsumers, new Color(red, green, 0));
                    matrices.pop();
                    renderedDurability = true;
                }

                if (items.getValue() && enchantments.getValue() && stack.hasEnchantments()) {
                    ItemEnchantmentsComponent component = EnchantmentHelper.getEnchantments(stack);
                    Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
                    for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) enchantments.put(enchantment, component.getLevel(enchantment));

                    int height = 0;
                    for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(enchantments)) {
                        String str = getEnchantmentName(entry.getKey().getIdAsString(), entry.getIntValue());
                        matrices.push();
                        matrices.translate(stackX, stackY + height, 0);
                        matrices.scale(0.5f, 0.5f, 1);
                        DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, str, 0, 0, vertexConsumers, Color.WHITE);
                        matrices.pop();
                        height += DrugHack.getInstance().getFontManager().getHeight() / 2;
                    }
                }
            }

            if (itemName.getValue() && !player.getMainHandStack().isEmpty()) {
                String itemText = player.getMainHandStack().getName().getString();

                matrices.push();
                matrices.translate(-DrugHack.getInstance().getFontManager().getWidth(itemText) / 2f / 2f, -DrugHack.getInstance().getFontManager().getHeight() - 1 - DrugHack.getInstance().getFontManager().getHeight() / 2f - 1 - (renderedItems ? (items.getValue() ? 18 + maxEnchants : 1) + (durability.getValue() && renderedDurability ? DrugHack.getInstance().getFontManager().getHeight() / 2.0f + 1 : 0) : 0), 0);
                matrices.scale(0.5f, 0.5f, 1);
                DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, itemText, 0, 0, vertexConsumers, Color.WHITE);
                matrices.pop();
            }

            matrices.pop();
        }
    }

    private ItemStack getItem(PlayerEntity player, int index) {
        return switch (index) {
            case 0 -> player.getMainHandStack();
            case 1 -> player.getInventory().armor.get(3);
            case 2 -> player.getInventory().armor.get(2);
            case 3 -> player.getInventory().armor.get(1);
            case 4 -> player.getInventory().armor.get(0);
            case 5 -> player.getOffHandStack();
            default -> ItemStack.EMPTY;
        };
    }

    private String getEnchantmentName(String id, int level) {
        id = id.replace("minecraft:", "");
        id = level > 1 ? id.substring(0, 2) : id.substring(0, 3);
        return id.substring(0, 1).toUpperCase() + id.substring(1) + " " + (level > 1 ? level : "");
    }

    @AllArgsConstructor
    private enum Border implements Nameable {
        None("None"),
        Fill("Fill"),
        Outline("Outline"),
        Both("Both");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}