package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.GuiSearcher;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.SearchUpgradeHandler;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateSearchStack;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;

public class GuiSearchUpgradeOptions implements IOptionPage {

    private final SearchUpgradeHandler renderHandler;
    private static GuiSearcher searchGui;
    private final EntityPlayer player = FMLClientHandler.instance().getClient().player;

    public GuiSearchUpgradeOptions(SearchUpgradeHandler searchUpgradeHandler) {
        renderHandler = searchUpgradeHandler;
    }

    @Override
    public String getPageName() {
        return "Item Searcher";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        gui.getButtonList().add(new GuiButton(10, 30, 40, 150, 20, "Search for item..."));
        gui.getButtonList().add(new GuiButton(11, 30, 128, 150, 20, "Move Stat Screen..."));
        if (searchGui != null && !player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
            ItemStack searchStack = searchGui.getSearchStack();
            ItemStack helmetStack = ItemPneumaticArmor.getSearchedStack(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            if (searchStack.isEmpty() && !helmetStack.isEmpty() || !searchStack.isEmpty() && helmetStack.isEmpty() || !searchStack.isEmpty() && !helmetStack.isEmpty() && !searchStack.isItemEqual(helmetStack)) {
                NetworkHandler.sendToServer(new PacketUpdateSearchStack(searchStack));
                NBTTagCompound tag = NBTUtil.getCompoundTag(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), ItemPneumaticArmor.NBT_SEARCH_STACK);
                tag.setInteger("itemID", searchStack.isEmpty() ? -1 : Item.getIdFromItem(searchStack.getItem()));
                tag.setInteger("itemDamage", searchStack.isEmpty() ? -1 : searchStack.getItemDamage());
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            searchGui = new GuiSearcher(player);
            if (!player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
                searchGui.setSearchStack(ItemPneumaticArmor.getSearchedStack(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD)));
            }
            Minecraft.getMinecraft().displayGuiScreen(searchGui);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMoveStat(renderHandler, ArmorHUDLayout.LayoutTypes.ITEM_SEARCH));
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
    }

    @Override
    public void keyTyped(char ch, int key) {
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
    }

    @Override
    public void handleMouseInput() {
    }

    @Override
    public boolean canBeTurnedOff() {
        return true;
    }

    @Override
    public boolean displaySettingsText() {
        return true;
    }
}
