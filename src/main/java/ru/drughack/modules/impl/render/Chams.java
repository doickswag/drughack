package ru.drughack.modules.impl.render;

import lombok.AllArgsConstructor;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.ModelRenderer;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.world.EntityUtils;

import java.awt.*;

public class Chams extends Module {

    public Setting<CategoryBooleanSetting> entityCategory = new Setting<>("Entity", new CategoryBooleanSetting(true));
    public Setting<CategorySetting> entityType = new Setting<>("Type", new CategorySetting(), v -> entityCategory.getValue().isOpen());
    public Setting<Boolean> players = new Setting<>("Players", true, v -> entityCategory.getValue().isOpen() && entityType.getValue().isOpen());
    public Setting<Boolean> hostiles = new Setting<>("Hostiles", false, v -> entityCategory.getValue().isOpen() && entityType.getValue().isOpen());
    public Setting<Boolean> passives = new Setting<>("Passives", false, v -> entityCategory.getValue().isOpen() && entityType.getValue().isOpen());
    public Setting<Boolean> entityPulse = new Setting<>("EntityPulse", false, v -> entityCategory.getValue().isOpen());
    public Setting<Boolean> entityShine = new Setting<>("EntityShine", false, v -> entityCategory.getValue().isOpen());
    public Setting<EntityMode> entityMode = new Setting<>("EntityMode", EntityMode.Both, v -> entityCategory.getValue().isOpen());
    public Setting<Boolean> damageModify = new Setting<>("DamageModify", false, v -> entityCategory.getValue().isOpen() && (entityMode.getValue() == EntityMode.Fill || entityMode.getValue() == EntityMode.Both));
    public Setting<CategorySetting> entityColors = new Setting<>("Colors", new CategorySetting(), v -> entityCategory.getValue().isOpen());

    public Setting<ColorSetting> entityFill = new Setting<>("Fill", new ColorSetting(
            new ColorSetting.Colors(new Color(189, 153, 255, 50), false)),
            v -> entityColors.getValue().isOpen()
    );

    public Setting<ColorSetting> entityOutline = new Setting<>("Outline", new ColorSetting(
            new ColorSetting.Colors(new Color(189, 153, 255, 255), false)),
            v -> entityColors.getValue().isOpen()
    );

    public Setting<CategoryBooleanSetting> crystalsCategory = new Setting<>("Crystals", new CategoryBooleanSetting(true));
    public Setting<Boolean> crystalPulse = new Setting<>("CrystalPulse", false, v -> crystalsCategory.getValue().isOpen());
    public Setting<Boolean> crystalShine = new Setting<>("CrystalShine", false, v -> crystalsCategory.getValue().isOpen());
    public Setting<CrystalMode> crystalMode = new Setting<>("CrystalMode", CrystalMode.Both, v -> crystalsCategory.getValue().isOpen());
    public Setting<CategorySetting> crystalsColor = new Setting<>("Colors", new CategorySetting(), v -> crystalsCategory.getValue().isOpen());

    public Setting<ColorSetting> crystalFill = new Setting<>("Fill", new ColorSetting(
            new ColorSetting.Colors(new Color(189, 153, 255, 50), false)),
            v -> crystalsColor.getValue().isOpen()
    );

    public Setting<ColorSetting> crystalOutline = new Setting<>("Outline", new ColorSetting(
            new ColorSetting.Colors(new Color(189, 153, 255, 255), false)),
            v -> crystalsColor.getValue().isOpen()
    );

    public Setting<CategoryBooleanSetting> itemsCategory = new Setting<>("Items", new CategoryBooleanSetting(true));
    public Setting<Boolean> itemPulse = new Setting<>("ItemPulse", false, v -> itemsCategory.getValue().isOpen());
    public Setting<ItemMode> itemMode = new Setting<>("ItemMode", ItemMode.Both, v -> itemsCategory.getValue().isOpen());
    public Setting<CategorySetting> itemsColor = new Setting<>("Colors", new CategorySetting(), v -> itemsCategory.getValue().isOpen());

    public Setting<ColorSetting> itemFill = new Setting<>("Fill", new ColorSetting(
            new ColorSetting.Colors(new Color(189, 153, 255, 50), false)),
            v -> itemsColor.getValue().isOpen()
    );

    public Setting<ColorSetting> itemOutline = new Setting<>("Outline", new ColorSetting(
            new ColorSetting.Colors(new Color(189, 153, 255, 255), false)),
            v -> itemsColor.getValue().isOpen()
    );

    public Chams() {
        super("Chams", "chamoy", Category.Render);
    }

    @EventHandler
    public void onRenderWorld(EventRender3D event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            if (!Renderer3D.isFrustumVisible(entity.getBoundingBox())) continue;

            if (entity instanceof LivingEntity livingEntity) {
                if (!isValidEntity(livingEntity)) continue;

                boolean flag = damageModify.getValue() && livingEntity.hurtTime > 0;

                Color fillColor = livingEntity instanceof PlayerEntity player && DrugHack.getInstance().getFriendManager().contains(player.getName().getString())
                        ? DrugHack.getInstance().getFriendManager().getDefaultFriendColor()
                        : flag ? ColorUtils.getColor(Color.RED, entityFill.getValue().getValue().getColor().getAlpha())
                        : entityFill.getValue().getValue().getColor();

                Color outlineColor = livingEntity instanceof PlayerEntity player && DrugHack.getInstance().getFriendManager().contains(player.getName().getString())
                        ? DrugHack.getInstance().getFriendManager().getDefaultFriendColor()
                        : flag ? ColorUtils.getColor(Color.RED, entityOutline.getValue().getValue().getColor().getAlpha())
                        : entityOutline.getValue().getValue().getColor();

                ModelRenderer.renderModel(livingEntity, 1.0f, event.getTickDelta(), new ModelRenderer.Render(
                        entityMode.getValue() == EntityMode.Fill || entityMode.getValue() == EntityMode.Both,
                        entityPulse.getValue() ? ColorUtils.getPulse(fillColor) : fillColor,
                        entityMode.getValue() == EntityMode.Outline || entityMode.getValue() == EntityMode.Both,
                        entityPulse.getValue() ? ColorUtils.getPulse(outlineColor) : outlineColor,
                        entityShine.getValue()
                ));
            }

            if (crystalsCategory.getValue().isEnabled() && entity instanceof EndCrystalEntity crystal) {
                ModelRenderer.renderModel(crystal, 1.0f, event.getTickDelta(), new ModelRenderer.Render(
                        crystalMode.getValue() == CrystalMode.Fill || crystalMode.getValue() == CrystalMode.Both,
                        crystalPulse.getValue() ? ColorUtils.getPulse(crystalFill.getValue().getValue().getColor()) : crystalFill.getValue().getValue().getColor(),
                        crystalMode.getValue() == CrystalMode.Outline || crystalMode.getValue() == CrystalMode.Both,
                        crystalPulse.getValue() ? ColorUtils.getPulse(crystalOutline.getValue().getValue().getColor()) : crystalOutline.getValue().getValue().getColor(),
                        crystalShine.getValue()
                ));
            }

            if (itemsCategory.getValue().isEnabled() && entity instanceof ItemEntity item) {
                Vec3d vec3d = EntityUtils.getRenderPos(item, event.getTickDelta());
                Renderer3D.renderCube(event.getMatrices(), vec3d, item.getHeight(), itemMode.getValue() == ItemMode.Fill || itemMode.getValue() == ItemMode.Both,
                        itemPulse.getValue() ? ColorUtils.getPulse(itemFill.getValue().getValue().getColor()) : itemFill.getValue().getValue().getColor(),
                        itemMode.getValue() == ItemMode.Outline || itemMode.getValue() == ItemMode.Both,
                        itemPulse.getValue() ? ColorUtils.getPulse(itemOutline.getValue().getValue().getColor()) : itemOutline.getValue().getValue().getColor());
            }
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (entityCategory.getValue().isEnabled() && players.getValue() && entity.getType() == EntityType.PLAYER) return true;
        if (entityCategory.getValue().isEnabled() && hostiles.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) return true;
        return entityCategory.getValue().isEnabled() && passives.getValue() && (entity.getType().getSpawnGroup() == SpawnGroup.CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.WATER_CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.WATER_AMBIENT || entity.getType().getSpawnGroup() == SpawnGroup.UNDERGROUND_WATER_CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.AXOLOTLS);
    }

    @AllArgsConstructor
    private enum EntityMode implements Nameable {
        Fill("Fill"),
        Outline("Outline"),
        Both("Both");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    private enum CrystalMode implements Nameable {
        Fill("Fill"),
        Outline("Outline"),
        Both("Both");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    private enum ItemMode implements Nameable {
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