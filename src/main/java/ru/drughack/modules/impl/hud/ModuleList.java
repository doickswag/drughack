package ru.drughack.modules.impl.hud;

import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.render.ColorUtils;

import java.util.Comparator;
import java.util.List;

public class ModuleList extends HudModule {
    private final Setting<Boolean> onlyBound = new Setting<>("Only Bound", false);

    public ModuleList() {
        super("ModuleList", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        float stringWidth;
        boolean reverse = getX() > (mc.getWindow().getScaledWidth() / 2f);
        int offset = 0;
        float maxWidth = 0;
        Comparator<Module> modules = Comparator.comparing(module -> DrugHack.getInstance().getFontManager().getWidth(module.getName() + (module.getDisplayInfo() != null ? " [" + module.getDisplayInfo() + "]" : "")) * -1);
        List<Module> list = DrugHack.getInstance().getModuleManager().getEnabledModules().stream()
                .sorted(modules)
                .toList();

        for (Module module : list) {
            if (onlyBound.getValue() && module.getBind().isEmpty()) continue;
            stringWidth = (DrugHack.getInstance().getFontManager().getWidth(module.getName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")));
            String text = module.getName() + Formatting.GRAY + (module.getDisplayInfo() != null ? " [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]" : "");
            if (stringWidth > maxWidth) maxWidth = stringWidth;
            int scissorX1 = (int) (reverse ? (getX() - maxWidth) : getX());
            int scissorX2 = (int) (reverse ? getX() : (getX() + maxWidth));
            e.getContext().enableScissor(scissorX1, (int) getY(), scissorX2, (int) (getY() + offset + DrugHack.getInstance().getFontManager().getHeight()));
            e.getContext().getMatrices().push();
            e.getContext().getMatrices().translate((float) (!module.isToggled() ? module.getAnimation().getEase() : 1 - module.getAnimation().getEase()) * stringWidth, 0, 0);
            DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), text, reverse ? (getX() - stringWidth) : getX(), getY() + offset, ColorUtils.getGlobalColor().getRGB());
            offset += DrugHack.getInstance().getFontManager().getHeight();
            e.getContext().getMatrices().pop();
            e.getContext().disableScissor();
        }

        setBounds(getX(), getY(), (int) (maxWidth * (reverse ? -1 : 1)), offset);
    }
}