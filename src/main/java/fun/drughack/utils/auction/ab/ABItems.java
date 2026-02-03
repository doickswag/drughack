package fun.drughack.utils.auction.ab;

import fun.drughack.DrugHack;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.auction.nbt.NbtUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;

@UtilityClass
public class ABItems implements Wrapper {

    //Пиздак блять)))) круш блядский
    public ItemStack krushHelmet() {
        return NbtUtils.loadItemStack("krushHelmet", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushChestplate() {
        return NbtUtils.loadItemStack("krushChestplate", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushLeggings() {
        return NbtUtils.loadItemStack("krushLeggings", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushBoots() {
        return NbtUtils.loadItemStack("krushBoots", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushSword() {
        return NbtUtils.loadItemStack("krushSword", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushPickaxe() {
        return NbtUtils.loadItemStack("krushPickaxe", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushTrident() {
        return NbtUtils.loadItemStack("krushTrident", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krushCrossbow() {
        return NbtUtils.loadItemStack("krushCrossbow", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Бафы
    public ItemStack serka() {
        return NbtUtils.loadItemStack("serka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack agentka() {
        return NbtUtils.loadItemStack("agentka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack killerka() {
        return NbtUtils.loadItemStack("killerka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack otrizhka() {
        return NbtUtils.loadItemStack("otrizhka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack medika() {
        return NbtUtils.loadItemStack("medika", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack pobedilka() {
        return NbtUtils.loadItemStack("pobedilka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Стрелы
    public ItemStack proklyatayaStrela() {
        return NbtUtils.loadItemStack("proklyatayaStrela", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack adskayaStrela() {
        return NbtUtils.loadItemStack("adskayaStrela", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack paranoiaStrela() {
        return NbtUtils.loadItemStack("paranoiaStrela", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack snezhnayaStrela() {
        return NbtUtils.loadItemStack("snezhnayaStrela", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Отмычки
    public ItemStack otmichkaArmor() {
        return NbtUtils.loadItemStack("otmichkaArmor", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack otmichkaResources() {
        return NbtUtils.loadItemStack("otmichkaResources", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack otmichkaSpheres() {
        return NbtUtils.loadItemStack("otmichkaSpheres", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack otmichkaTools() {
        return NbtUtils.loadItemStack("otmichkaTools", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack otmichkaWeapons() {
        return NbtUtils.loadItemStack("otmichkaWeapons", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Динамиты
    public ItemStack tierBlack() {
        return NbtUtils.loadItemStack("tierBlack", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack tierWhite() {
        return NbtUtils.loadItemStack("tierWhite", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Расходники
    public ItemStack desor() {
        return NbtUtils.loadItemStack("desor", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack plast() {
        return NbtUtils.loadItemStack("plast", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack bozhka() {
        return NbtUtils.loadItemStack("bozhka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack snezhok() {
        return NbtUtils.loadItemStack("snezhok", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack trapka() {
        return NbtUtils.loadItemStack("trapka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack yavka() {
        return NbtUtils.loadItemStack("yavka", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Талики
    public ItemStack dedalaTier1() {
        return NbtUtils.loadItemStack("dedalaTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack dedalaTier2() {
        return NbtUtils.loadItemStack("dedalaTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack dedalaTier3() {
        return NbtUtils.loadItemStack("dedalaTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack exidnaTier1() {
        return NbtUtils.loadItemStack("exidnaTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack exidnaTier2() {
        return NbtUtils.loadItemStack("exidnaTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack exidnaTier3() {
        return NbtUtils.loadItemStack("exidnaTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack garmoniiTier1() {
        return NbtUtils.loadItemStack("garmoniiTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack garmoniiTier2() {
        return NbtUtils.loadItemStack("garmoniiTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack garmoniiTier3() {
        return NbtUtils.loadItemStack("garmoniiTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack graniTier1() {
        return NbtUtils.loadItemStack("graniTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack graniTier2() {
        return NbtUtils.loadItemStack("graniTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack graniTier3() {
        return NbtUtils.loadItemStack("graniTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack haronTier1() {
        return NbtUtils.loadItemStack("haronTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack haronTier2() {
        return NbtUtils.loadItemStack("haronTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack haronTier3() {
        return NbtUtils.loadItemStack("haronTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack phoenixTier1() {
        return NbtUtils.loadItemStack("phoenixTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack phoenixTier2() {
        return NbtUtils.loadItemStack("phoenixTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack phoenixTier3() {
        return NbtUtils.loadItemStack("phoenixTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack tritonTier1() {
        return NbtUtils.loadItemStack("tritonTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack tritonTier2() {
        return NbtUtils.loadItemStack("tritonTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack tritonTier3() {
        return NbtUtils.loadItemStack("tritonTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack krush() {
        return NbtUtils.loadItemStack("krush", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack karatel() {
        return NbtUtils.loadItemStack("karatel", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    //Сферы
    public ItemStack andromedaTier1() {
        return NbtUtils.loadItemStack("andromedaTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack andromedaTier2() {
        return NbtUtils.loadItemStack("andromedaTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack andromedaTier3() {
        return NbtUtils.loadItemStack("andromedaTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack apollonaTier1() {
        return NbtUtils.loadItemStack("apollonaTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack apollonaTier2() {
        return NbtUtils.loadItemStack("apollonaTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack apollonaTier3() {
        return NbtUtils.loadItemStack("apollonaTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack astreaTier1() {
        return NbtUtils.loadItemStack("astreaTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack astreaTier2() {
        return NbtUtils.loadItemStack("astreaTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack astreaTier3() {
        return NbtUtils.loadItemStack("astreaTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack himeraTier1() {
        return NbtUtils.loadItemStack("himeraTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack himeraTier2() {
        return NbtUtils.loadItemStack("himeraTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack himeraTier3() {
        return NbtUtils.loadItemStack("himeraTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack osirisaTier1() {
        return NbtUtils.loadItemStack("osirisaTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack osirisaTier2() {
        return NbtUtils.loadItemStack("osirisaTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack osirisaTier3() {
        return NbtUtils.loadItemStack("osirisaTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack pandoraTier1() {
        return NbtUtils.loadItemStack("pandoraTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack pandoraTier2() {
        return NbtUtils.loadItemStack("pandoraTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack pandoraTier3() {
        return NbtUtils.loadItemStack("pandoraTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack titanTier1() {
        return NbtUtils.loadItemStack("titanTier1", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack titanTier2() {
        return NbtUtils.loadItemStack("titanTier2", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }

    public ItemStack titanTier3() {
        return NbtUtils.loadItemStack("titanTier3", DrugHack.getInstance().getAbItemsDir(), mc.getNetworkHandler().getRegistryManager());
    }
}