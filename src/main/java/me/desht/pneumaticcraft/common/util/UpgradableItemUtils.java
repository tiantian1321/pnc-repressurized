/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.util.upgrade.UpgradeCache;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Some helper methods to manage items which can store upgrades (Pneumatic Armor, Drones...)
 */
public class UpgradableItemUtils {
    public static final String NBT_CREATIVE = "CreativeUpgrade";
    public static final String NBT_UPGRADE_TAG = "UpgradeInventory";
    public static final int UPGRADE_INV_SIZE = 9;
    private static final String NBT_UPGRADE_CACHE_TAG = "UpgradeCache";

    /**
     * Add a standardized tooltip listing the installed upgrades in the given item.
     *
     * @param iStack the item
     * @param textList list of text to append tooltip too
     * @param flag tooltip flag
     */
    public static void addUpgradeInformation(ItemStack iStack, List<Component> textList, TooltipFlag flag) {
        ItemStack[] inventoryStacks = getUpgradeStacks(iStack);
        boolean isItemEmpty = true;
        for (ItemStack stack : inventoryStacks) {
            if (!stack.isEmpty()) {
                isItemEmpty = false;
                break;
            }
        }
        if (isItemEmpty) {
            if (!(iStack.getItem() instanceof BlockItem)) {
                textList.add(xlate("pneumaticcraft.gui.tooltip.upgrades.empty").withStyle(ChatFormatting.DARK_GREEN));
            }
        } else {
            textList.add(xlate("pneumaticcraft.gui.tooltip.upgrades.not_empty").withStyle(ChatFormatting.GREEN));
            PneumaticCraftUtils.summariseItemStacks(textList, inventoryStacks, ChatFormatting.DARK_GREEN + Symbols.BULLET + " ");
        }
    }

    /**
     * Store a collection of upgrades into an item stack.  This should be only be used for items; don't use it
     * to manage saved upgrades on a dropped block which has serialized upgrade data.
     *
     * @param stack the stack
     * @param handler an ItemStackHandler holding upgrade items
     */
    public static void setUpgrades(ItemStack stack, ItemStackHandler handler) {
        stack.getOrCreateTag().put(NBT_UPGRADE_TAG, handler.serializeNBT());
        UpgradeCache cache = new UpgradeCache(() -> handler);
        Objects.requireNonNull(stack.getTag()).put(NBT_UPGRADE_CACHE_TAG, cache.toNBT());

        stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
            if (h.getPressure() > h.maxPressure()) {
                int maxAir = (int)(h.getVolume() * h.maxPressure());
                h.addAir(maxAir - h.getAir());
            }
        });
    }

    /**
     * Retrieves the upgrades currently installed on the given itemstack.
     */
    public static ItemStack[] getUpgradeStacks(ItemStack stack) {
        CompoundTag tag = getSerializedUpgrades(stack);
        ItemStack[] inventoryStacks = new ItemStack[UPGRADE_INV_SIZE];
        Arrays.fill(inventoryStacks, ItemStack.EMPTY);
        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag slotEntry = itemList.getCompound(i);
            int j = slotEntry.getByte("Slot");
            if (j >= 0 && j < UPGRADE_INV_SIZE) {
                inventoryStacks[j] = ItemStack.of(slotEntry);
            }
        }
        return inventoryStacks;
    }

    public static ItemStackHandler getUpgrades(ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(UPGRADE_INV_SIZE);
        CompoundTag tag = getSerializedUpgrades(stack);
        if (!tag.isEmpty()) handler.deserializeNBT(tag);
        return handler;
    }

    public static int getUpgrades(ItemStack stack, PNCUpgrade upgrade) {
        if (stack.hasTag()) {
            if (stack.getTag().contains(NBT_UPGRADE_TAG) && !stack.getTag().contains(NBT_UPGRADE_CACHE_TAG)) {

            }
            CompoundTag subTag = Objects.requireNonNull(stack.getTag()).getCompound(NBT_UPGRADE_CACHE_TAG);
            String key = PneumaticCraftUtils.modDefaultedString(upgrade.getRegistryName());
            return subTag.getInt(key);
        }
        return 0;
    }

    public static List<Integer> getUpgradeList(ItemStack stack, PNCUpgrade... upgradeList) {
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        if (stack.hasTag()) {
            CompoundTag subTag = Objects.requireNonNull(stack.getTag()).getCompound(NBT_UPGRADE_CACHE_TAG);
            for (PNCUpgrade upgrade : upgradeList) {
                String key = PneumaticCraftUtils.modDefaultedString(upgrade.getRegistryName());
                builder.add(subTag.getInt(key));
            }
        }
        return builder.build();
    }

    public static boolean hasCreativeUpgrade(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(UpgradableItemUtils.NBT_CREATIVE);
    }

    private static CompoundTag getSerializedUpgrades(ItemStack stack) {
        if (!stack.hasTag()) return new CompoundTag();
        if (Objects.requireNonNull(stack.getTag()).contains(NBTKeys.BLOCK_ENTITY_TAG)) {
            return stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG).getCompound(NBT_UPGRADE_TAG);
        } else {
            return stack.getTag().getCompound(NBT_UPGRADE_TAG);
        }
    }
}
