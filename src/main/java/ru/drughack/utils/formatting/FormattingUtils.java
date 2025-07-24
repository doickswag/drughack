package ru.drughack.utils.formatting;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IStyle;
import ru.drughack.api.mixins.accesors.ITextColor;
import ru.drughack.utils.render.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class FormattingUtils {

    public static Style withExclusiveFormatting(Style style, CustomFormatting formatting) {
        TextColor textColor = style.getColor();
        if (formatting == CustomFormatting.CLIENT) {
            textColor = ITextColor.create(ColorUtils.getGlobalColor().getRGB(), "CLIENT");
        } else if (formatting == CustomFormatting.RAINBOW) {
            textColor = ITextColor.create(ColorUtils.getRainbow().getRGB(), "RAINBOW");
        }

        return IStyle.create(textColor, null, false, false, false, false, false, style.getClickEvent(), style.getHoverEvent(), style.getInsertion(), style.getFont());
    }

    public static List<String> wrapText(String text, int width) {
        List<String> wrappedText = new ArrayList<>();
        String[] words = text.split(" ");
        String current = "";

        for (String word : words) {
            if (DrugHack.getInstance().getFontManager().getWidth(current) + DrugHack.getInstance().getFontManager().getWidth(word) <= width) {
                current += word + " ";
            } else {
                wrappedText.add(current);
                current = word + " ";
            }
        }
        if (DrugHack.getInstance().getFontManager().getWidth(current) > 0) wrappedText.add(current);

        return wrappedText;
    }
}