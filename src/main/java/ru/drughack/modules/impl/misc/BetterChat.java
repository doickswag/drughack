package ru.drughack.modules.impl.misc;

import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventChatSend;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

import java.util.Map;

public class BetterChat extends Module {

    private final Setting<Boolean> suffix = new Setting<>("Suffix", false);
    private final Setting<String> text = new Setting<>("Text", " | ᴅʀᴜɢʜᴀᴄᴋ.ᴄᴄ", v -> suffix.getValue());
    private final Setting<Boolean> antiCyrillic = new Setting<>("AntiCyrillic", false);

    public BetterChat() {
        super("BetterChat", "chat but better", Category.Misc);
    }

    Map<String, String> cyrillicText = Map.<String, String>ofEntries(
            Map.entry("а", "a"),
            Map.entry("б", "6"),
            Map.entry("в", "B"),
            Map.entry("г", "r"),
            Map.entry("д", "d"),
            Map.entry("е", "e"),
            Map.entry("ё", "e"),
            Map.entry("ж", ">I<"),
            Map.entry("з", "3"),
            Map.entry("и", "u"),
            Map.entry("й", "u"),
            Map.entry("к", "k"),
            Map.entry("л", "JI"),
            Map.entry("м", "m"),
            Map.entry("н", "H"),
            Map.entry("о", "o"),
            Map.entry("п", "n"),
            Map.entry("р", "p"),
            Map.entry("с", "c"),
            Map.entry("т", "T"),
            Map.entry("у", "y"),
            Map.entry("ф", "o|o"),
            Map.entry("х", "x"),
            Map.entry("ц", "lI"),
            Map.entry("ч", "4"),
            Map.entry("ш", "w"),
            Map.entry("щ", "w"),
            Map.entry("ь", "b"),
            Map.entry("ы", "bI"),
            Map.entry("ъ", "b"),
            Map.entry("э", "-)"),
            Map.entry("ю", "I-0"),
            Map.entry("я", "9I"),
            Map.entry("А", "A"),
            Map.entry("Б", "6"),
            Map.entry("В", "B"),
            Map.entry("Г", "r"),
            Map.entry("Д", "D"),
            Map.entry("Е", "E"),
            Map.entry("Ё", "E"),
            Map.entry("Ж", ">I<"),
            Map.entry("З", "3"),
            Map.entry("И", "U"),
            Map.entry("Й", "U"),
            Map.entry("К", "K"),
            Map.entry("Л", "JI"),
            Map.entry("М", "M"),
            Map.entry("Н", "H"),
            Map.entry("О", "O"),
            Map.entry("П", "N"),
            Map.entry("Р", "P"),
            Map.entry("С", "C"),
            Map.entry("Т", "T"),
            Map.entry("У", "Y"),
            Map.entry("Ф", "0|0"),
            Map.entry("Х", "X"),
            Map.entry("Ц", "lI"),
            Map.entry("Ч", "4"),
            Map.entry("Ш", "W"),
            Map.entry("Щ", "W"),
            Map.entry("Ь", "b"),
            Map.entry("Ы", "bI"),
            Map.entry("Ъ", "b"),
            Map.entry("Э", "-)"),
            Map.entry("Ю", "I-O"),
            Map.entry("Я", "9I")
    );

    @EventHandler
    public void onChatSend(EventChatSend e) {
        if (suffix.getValue()) {
            if (!e.isCanceled()) e.resume();
            e.setMessage(e.getMessage() + text.getValue());
        }

        if (antiCyrillic.getValue()) {
            if (!e.isCanceled()) e.resume();
            e.setMessage(cyrillic(e.getMessage()));
        }
    }

    public String cyrillic(String message) {
        StringBuilder result = new StringBuilder();

        for (char ch : message.toCharArray()) {
            String str = cyrillicText.get(ch + "");
            result.append(str != null ? str : ch);
        }

        return result.toString();
    }
}