package ru.drughack.modules.impl.render;

import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.CategorySetting;

public class ViewModel extends Module {

    public Setting<CategorySetting> mainHand = new Setting<>("MainHand", new CategorySetting());
    public Setting<Float> mainX = new Setting<>("MainX", 0f, -2f, 2f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainY = new Setting<>("MainY", 0f, -2f, 2f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainZ = new Setting<>("MainZ", 0f, -2f, 2f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainRotateX = new Setting<>("MainRotateX", 0f, -180f, 180f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainRotateY = new Setting<>("MainRotateY", 0f, -180f, 180f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainRotateZ = new Setting<>("MainRotateZ", 0f, -180f, 180f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainScaleX = new Setting<>("MainScaleX", 1f, 0f, 3f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainScaleY = new Setting<>("MainScaleY", 1f, 0f, 3f, v -> mainHand.getValue().isOpen());
    public Setting<Float> mainScaleZ = new Setting<>("MainScaleZ", 1f, 0f, 3f, v -> mainHand.getValue().isOpen());

    public Setting<CategorySetting> offHand = new Setting<>("OffHand", new CategorySetting());
    public Setting<Float> offX = new Setting<>("OffX", 0f, -2f, 2f, v -> offHand.getValue().isOpen());
    public Setting<Float> offY = new Setting<>("OffY", 0f, -2f, 2f, v -> offHand.getValue().isOpen());
    public Setting<Float> offZ = new Setting<>("OffZ", 0f, -2f, 2f, v -> offHand.getValue().isOpen());
    public Setting<Float> offRotateX = new Setting<>("OffRotateX", 0f, -180f, 180f, v -> offHand.getValue().isOpen());
    public Setting<Float> offRotateY = new Setting<>("OffRotateY", 0f, -180f, 180f, v -> offHand.getValue().isOpen());
    public Setting<Float> offRotateZ = new Setting<>("OffRotateZ", 0f, -180f, 180f, v -> offHand.getValue().isOpen());
    public Setting<Float> offScaleX = new Setting<>("OffScaleX", 1f, 0f, 3f, v -> offHand.getValue().isOpen());
    public Setting<Float> offScaleY = new Setting<>("OffScaleY", 1f, 0f, 3f, v -> offHand.getValue().isOpen());
    public Setting<Float> offScaleZ = new Setting<>("OffScaleZ", 1f, 0f, 3f, v -> offHand.getValue().isOpen());

    public Setting<CategorySetting> misc = new Setting<>("Misc", new CategorySetting());
    public Setting<Boolean> instantSwap = new Setting<>("InstantSwap", false, v -> misc.getValue().isOpen());
    public Setting<Float> eatMultiplier = new Setting<>("EatMultiplier", 1f, 0.1f, 1f, v -> misc.getValue().isOpen());

    public ViewModel() {
        super("ViewModel", "top", Category.Render);
    }
}