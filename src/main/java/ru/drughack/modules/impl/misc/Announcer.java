package ru.drughack.modules.impl.misc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.*;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.math.TimerUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Announcer extends Module {

    public Setting<Integer> delay = new Setting<>("Delay", 2500, 0, 10000);
    public Setting<Boolean> clientside = new Setting<>("Clientside", false);
    public Setting<Boolean> greenText = new Setting<>("GreenText", false);
    public Setting<Boolean> global = new Setting<>("Global", true);
    public Setting<Boolean> distance = new Setting<>("Distance", true);
    public Setting<Boolean> blocksMined = new Setting<>("BlocksMined", true);
    public Setting<Boolean> blocksPlaced = new Setting<>("BlocksPlaced", true);
    public Setting<Boolean> eating = new Setting<>("Eating", true);

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    private final TimerUtils messageTimer = new TimerUtils();
    private final TimerUtils distanceTimer = new TimerUtils();
    private Vec3d lastPos;
    private int mined;
    private int placed;
    private int eaten;

    public Announcer() {
        super("Announcer", "announcer", Category.Misc);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (lastPos == null) lastPos = new Vec3d(mc.player.lastRenderX, mc.player.lastRenderY, mc.player.lastRenderZ);
        double traveled = Math.abs(lastPos.x - mc.player.lastRenderX) + Math.abs(lastPos.y - mc.player.lastRenderY) + Math.abs(lastPos.z - mc.player.lastRenderZ);

        if (distance.getValue() && traveled > 1 && distanceTimer.hasTimeElapsed(delay.getValue()) && queue.size() <= 5) {
            queue.add(getDistanceMessage(MathUtils.round(traveled, 1) + ""));
            lastPos = new Vec3d(mc.player.lastRenderX, mc.player.lastRenderY, mc.player.lastRenderZ);
            distanceTimer.reset();
        }

        if (messageTimer.hasTimeElapsed(delay.getValue()) && !queue.isEmpty()) {
            String message = queue.poll();
            if (clientside.getValue()) DrugHack.getInstance().getChatManager().message(message);
            else mc.player.networkHandler.sendChatMessage((global.getValue() ? "!" : "") + (greenText.getValue() ? "> " : "") + message);
            messageTimer.reset();
        }
    }

    @EventHandler
    public void onBreakBlock(EventBreakBlock event) {
        mined++;

        if (blocksMined.getValue() && mined >= MathUtils.random(6, 1) && queue.size() <= 5) {
            queue.add(getMineMessage(mined + ""));
            mined = 0;
        }
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && mc.player.getStackInHand(packet.getHand()).getItem() instanceof BlockItem) {
            placed++;

            if (blocksPlaced.getValue() && placed >= MathUtils.random(6, 1) && queue.size() <= 5) {
                queue.add(getPlaceMessage(placed + ""));
                placed = 0;
            }
        }
    }

    @EventHandler
    public void onConsumeItem(EventConsumeItem event) {
        if (fullNullCheck() || event.getStack().get(DataComponentTypes.FOOD) == null) return;

        eaten++;

        if (eating.getValue() && eaten >= MathUtils.random(6, 1) && queue.size() <= 5) {
            queue.add(getEatMessage(eaten + " " + event.getStack().getName().getString()));
            eaten = 0;
        }
    }

    @EventHandler
    public void onChatInput(EventChatInput event) {
        messageTimer.reset();
    }

    private String getDistanceMessage(String replacement) {
        String[] messages = new String[]{
                "I just flew " + replacement + " meters thanks to " + DrugHack.getInstance().getProtection().getName() + "!",
                "Ich bin gerade " + replacement + " Meter weit geflogen, dank " + DrugHack.getInstance().getProtection().getName() + "!",
                "Je viens de voler " + replacement + " mètres grâce à " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + "のおかげで" + replacement + "メートル飛んだよ！",
                "Lensin juuri " + replacement + " metriä " + DrugHack.getInstance().getProtection().getName() + " ansiosta!",
                "Я только что пролетел " + replacement + " метров благодаря " + DrugHack.getInstance().getProtection().getName() + "!",
                "Acabo de volar " + replacement + " metros gracias a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jag flög just " + replacement + " meter tack vare " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + " sayesinde " + replacement + " metre uçtum!",
                "Ik heb net " + replacement + " meter gevlogen dankzij " + DrugHack.getInstance().getProtection().getName() + "!",
                "Μόλις πέταξα " + replacement + " μέτρα χάρη στην " + DrugHack.getInstance().getProtection().getName() + "!",
                "我刚刚飞了" + replacement + "米，多亏了" + DrugHack.getInstance().getProtection().getName() + "!",
                "Ho appena volato per " + replacement + " metri grazie ad " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jeg fløy nettopp " + replacement + " meter takket være " + DrugHack.getInstance().getProtection().getName() + "!",
                "Tocmai am zburat " + replacement + " de metri datorită lui " + DrugHack.getInstance().getProtection().getName() + "!",
                "Díky " + DrugHack.getInstance().getProtection().getName() + " jsem právě uletěl " + replacement + " metrů!",
                "Acabei de voar " + replacement + " metros graças ao " + DrugHack.getInstance().getProtection().getName() + "!",
                "Z " + DrugHack.getInstance().getProtection().getName() + " sem pravkar preletel " + replacement + " metrov!",
                "Właśnie przeleciałem " + replacement + " metrów dzięki " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + " 덕분에 방금 " + replacement + "를 비행했습니다!",
                DrugHack.getInstance().getProtection().getName() + " dėka ką tik nuskridau " + replacement + " metrus!",
                "Saya baru saja terbang sejauh " + replacement + " meter berkat " + DrugHack.getInstance().getProtection().getName() + "!",
                replacement + " métert repültem az " + DrugHack.getInstance().getProtection().getName() + " köszönhetően!"
        };

        return messages[(int) MathUtils.random(22, 0)];
    }

    private String getMineMessage(String replacement) {
        String[] messages = new String[]{
                "I just mined " + replacement + " blocks thanks to " + DrugHack.getInstance().getProtection().getName() + "!",
                "Ich habe gerade " + replacement + " Blöcke abgebaut, dank " + DrugHack.getInstance().getProtection().getName() + "!",
                "Je viens d'extraire " + replacement + " blocs grâce à " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + "のおかげで" + replacement + "ブロック採掘したところです！",
                "Louhin juuri " + replacement + " lohkoa " + DrugHack.getInstance().getProtection().getName() + " ansiosta!",
                "Я только что добыл " + replacement + " блоков благодаря " + DrugHack.getInstance().getProtection().getName() + "!",
                "¡Acabo de minar " + replacement + " bloques gracias a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jag har precis tagit fram " + replacement + " block tack vare " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + " sayesinde az önce " + replacement + " blok kazdım!",
                "Ik heb net " + replacement + " blokken gedolven dankzij " + DrugHack.getInstance().getProtection().getName() + "!",
                "Μόλις εξόρυξα " + replacement + " μπλοκ χάρη στην " + DrugHack.getInstance().getProtection().getName() + "!",
                "我刚刚开采了" + replacement + "个区块，感谢" + DrugHack.getInstance().getProtection().getName() + "!",
                "Ho appena estratto " + replacement + " blocchi grazie ad " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jeg har nettopp utvunnet " + replacement + " blokker takket være " + DrugHack.getInstance().getProtection().getName() + "!",
                "Tocmai am minat " + replacement + " de blocuri datorită lui " + DrugHack.getInstance().getProtection().getName() + "!",
                "Právě jsem vytěžil " + replacement + " bloků díky " + DrugHack.getInstance().getProtection().getName() + "!",
                "Acabei de extrair " + replacement + " blocos graças ao " + DrugHack.getInstance().getProtection().getName() + "!",
                "Zahvaljujoč " + DrugHack.getInstance().getProtection().getName() + " sem pravkar izkopal " + replacement + " blokov!",
                "Właśnie wydobyłem " + replacement + " bloków dzięki " + DrugHack.getInstance().getProtection().getName() + "!",
                "방금 " + DrugHack.getInstance().getProtection().getName() + " 덕분에 " + replacement + " 블록을 채굴했습니다!",
                DrugHack.getInstance().getProtection().getName() + " dėka ką tik iškasiau " + replacement + " blokus!",
                "Saya baru saja menambang " + replacement + " blok berkat " + DrugHack.getInstance().getProtection().getName() + "!",
                "Most bányásztam " + replacement + " blokkot az " + DrugHack.getInstance().getProtection().getName() + " köszönhetően!"
        };

        return messages[(int) MathUtils.random(22, 0)];
    }

    private String getPlaceMessage(String replacement) {
        String[] messages = new String[] {
                "I just placed " + replacement + " blocks thanks to " + DrugHack.getInstance().getProtection().getName() + "!",
                "Ich habe gerade " + replacement + " Blöcke dank " + DrugHack.getInstance().getProtection().getName() + " platziert!",
                "Je viens de placer " + replacement + " blocs grâce à " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + "のおかげで" + replacement + "個のブロックを置いたところだ!",
                "Sijoitin juuri " + replacement + " lohkoa " + DrugHack.getInstance().getProtection().getName() + " ansiosta!",
                "Я только что разместил " + replacement + " блоков благодаря " + DrugHack.getInstance().getProtection().getName() + "!",
                "¡Acabo de colocar " + replacement + " bloques gracias a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jag har precis placerat " + replacement + " block tack vare " + DrugHack.getInstance().getProtection().getName() + "!",
                "Az önce " + DrugHack.getInstance().getProtection().getName() + " sayesinde " + replacement + " blok yerleştirdim!",
                "Ik heb net " + replacement + " blokken geplaatst dankzij " + DrugHack.getInstance().getProtection().getName() + "!",
                "Μόλις τοποθέτησα " + replacement + " μπλοκ χάρη στο " + DrugHack.getInstance().getProtection().getName() + "!",
                "多亏了 " + DrugHack.getInstance().getProtection().getName() + "，我刚刚放了 " + replacement + " 块!",
                "Ho appena piazzato " + replacement + " blocchi grazie a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jeg har nettopp plassert " + replacement + " blokker takket være " + DrugHack.getInstance().getProtection().getName() + "!",
                "Tocmai am plasat " + replacement + " blocuri datorită lui " + DrugHack.getInstance().getProtection().getName() + "!",
                "Právě jsem umístil " + replacement + " bloků díky " + DrugHack.getInstance().getProtection().getName() + "!",
                "Acabei de colocar " + replacement + " blocos graças ao " + DrugHack.getInstance().getProtection().getName() + "!",
                "Pravkar sem postavil " + replacement + " blokov zahvaljujoč " + DrugHack.getInstance().getProtection().getName() + "!",
                "Właśnie umieściłem " + replacement + " bloków dzięki " + DrugHack.getInstance().getProtection().getName() + "!",
                DrugHack.getInstance().getProtection().getName() + " 덕분에 방금 XXX 블록을 배치했습니다!",
                "Ką tik įdėjau " + replacement + " blokų dėka " + DrugHack.getInstance().getProtection().getName() + "!",
                "Saya baru saja menempatkan blok " + replacement + " berkat " + DrugHack.getInstance().getProtection().getName() + "!",
                "Most helyeztem el " + replacement + " blokkot a " + DrugHack.getInstance().getProtection().getName() + "-nek köszönhetően!"
        };

        return messages[(int) MathUtils.random(22, 0)];
    }

    private String getEatMessage(String replacement) {
        String[] messages = new String[] {
                "I just ate " + replacement + " thanks to " + DrugHack.getInstance().getProtection().getName() + "!",
                "Ich habe gerade " + replacement + " gegessen, dank " + DrugHack.getInstance().getProtection().getName() + "!",
                "Je viens de manger " + replacement + " grâce à " + DrugHack.getInstance().getProtection().getName() + " !",
                DrugHack.getInstance().getProtection().getName() + "のおかげで" + replacement + "を食べたよ!",
                "Söin juuri " + replacement + " kiitos " + DrugHack.getInstance().getProtection().getName() + "!",
                "Я только что съел " + replacement + " благодаря " + DrugHack.getInstance().getProtection().getName() + "!",
                "¡Acabo de comer " + replacement + " gracias a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jag åt just " + replacement + " tack vare " + DrugHack.getInstance().getProtection().getName() + "!",
                "Az önce " + DrugHack.getInstance().getProtection().getName() + " sayesinde " + replacement + " yedim!",
                "Ik heb net " + replacement + " gegeten dankzij " + DrugHack.getInstance().getProtection().getName() + "!",
                "Μόλις έφαγα " + replacement + " χάρη στο " + DrugHack.getInstance().getProtection().getName() + "!",
                "我刚吃了 " + replacement + "，多亏了 " + DrugHack.getInstance().getProtection().getName() + "!",
                "Ho appena mangiato " + replacement + " grazie a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Jeg spiste nettopp " + replacement + " takket være " + DrugHack.getInstance().getProtection().getName() + "!",
                "Právě jsem snědl " + replacement + " díky " + DrugHack.getInstance().getProtection().getName() + "!",
                "Acabei de comer " + replacement + " graças a " + DrugHack.getInstance().getProtection().getName() + "!",
                "Pravkar sem pojedel " + replacement + " zaradi " + DrugHack.getInstance().getProtection().getName() + "!",
                "Właśnie zjadłem " + replacement + " dzięki " + DrugHack.getInstance().getProtection().getName() + "!",
                "방금 " + DrugHack.getInstance().getProtection().getName() + " 덕분에 " + replacement + "를 먹었어요!",
                "Ką tik suvalgiau " + replacement + " dėl " + DrugHack.getInstance().getProtection().getName() + "!",
                "Saya baru saja makan " + replacement + " berkat " + DrugHack.getInstance().getProtection().getName() + "!",
                "Most ettem " + replacement + "-t, hála " + DrugHack.getInstance().getProtection().getName() + "-nek!"
        };

        return messages[(int) MathUtils.random(22, 0)];
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        queue.clear();
        messageTimer.reset();
        distanceTimer.reset();
        lastPos = null;
        mined = 0;
        placed = 0;
        eaten = 0;
    }
}