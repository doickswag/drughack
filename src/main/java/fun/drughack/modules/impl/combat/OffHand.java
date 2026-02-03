package fun.drughack.modules.impl.combat;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventPlayerTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.movement.GuiMove;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.utils.world.InventoryUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class OffHand extends Module {

    private final NumberSetting health = new NumberSetting("settings.offhand.health", 5f, 0f, 36f, 0.05f);
    private final BooleanSetting elytra = new BooleanSetting("settings.offhand.elytra", false);
    private final NumberSetting elytraHealth = new NumberSetting("settings.offhand.elytrahealth", 10f, 0f, 36f, 0.05f, elytra::getValue);
    private final BooleanSetting fall = new BooleanSetting("settings.offhand.fall", false);
    private final NumberSetting fallDistance = new NumberSetting("settings.offhand.falldistance", 20f, 10f, 50f, 0.05f, fall::getValue);
    private final BooleanSetting inContainer = new BooleanSetting("settings.offhand.incontainer", true);
    private final BooleanSetting sync = new BooleanSetting("settings.sync", false);

    public OffHand() {
        super("OffHand", Category.Combat);
    }

    private int ticks;
    private Item previousItem = null;

    @EventHandler
    public void onPlayerTick(EventPlayerTick e) {
        if (fullNullCheck()) return;
        if (mc.currentScreen != null && !inContainer.getValue()) return;
        
        if (ticks > 0) {
        	ticks--;
            return;
        }

        Item currentOffhandItem = mc.player.getOffHandStack().isEmpty() ? null : mc.player.getOffHandStack().getItem();
        
        if (needTotems()) {
            if (currentOffhandItem != Items.TOTEM_OF_UNDYING) {
                int totemSlot = InventoryUtils.find(Items.TOTEM_OF_UNDYING);
                if (totemSlot != -1) {
                    if (sync.getValue()) InventoryUtils.bypassSwap(totemSlot, 45);
                    else InventoryUtils.swap(totemSlot, 45);
                    ticks = sync.getValue() ? DrugHack.getInstance().getModuleManager().getModule(GuiMove.class).getTicks() : 0;
                }
            }
        } else if (currentOffhandItem == Items.TOTEM_OF_UNDYING && previousItem != null) {
            int previousSlot = InventoryUtils.find(previousItem);
            if (previousSlot != -1) {
                if (sync.getValue()) InventoryUtils.bypassSwap(previousSlot, 45);
                else InventoryUtils.swap(previousSlot, 45);
                ticks = sync.getValue() ? DrugHack.getInstance().getModuleManager().getModule(GuiMove.class).getTicks() : 0;
                previousItem = null;
            }
        }

        if (needTotems() && currentOffhandItem != Items.TOTEM_OF_UNDYING && currentOffhandItem != null) previousItem = currentOffhandItem;
    }

    private boolean needTotems() {
        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= health.getValue()) return true;
        if (fall.getValue() && DrugHack.getInstance().getServerManager().getFallDistance() >= fallDistance.getValue()) return true;
        return (elytra.getValue()
                && mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA
                && (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= elytraHealth.getValue()
        );
    }
}