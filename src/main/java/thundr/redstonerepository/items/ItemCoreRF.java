package thundr.redstonerepository.items;

import cofh.api.item.IMultiModeItem;
import cofh.core.init.CoreEnchantments;
import cofh.core.init.CoreProps;
import cofh.core.item.IEnchantableItem;
import cofh.core.item.ItemCore;
import cofh.core.util.helpers.EnergyHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.redstoneflux.util.EnergyContainerItemWrapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemCoreRF extends ItemCore implements IMultiModeItem, IEnergyContainerItem, IEnchantableItem {

    protected int maxEnergy = 32000;
    protected int maxTransfer = 1000;
    protected int energyPerUse = 200;

    protected boolean isCreative = false;
    protected boolean showInCreative = true;

    public ItemCoreRF(String modName) {
        super(modName);
        addPropertyOverride(new ResourceLocation("active"), (stack, world, entity) -> this.isActive(stack) ? 1F : 0F);
    }

    public ItemCoreRF setEnergyParams(int maxEnergy, int maxTransfer, int energyPerUse) {
        this.maxEnergy = maxEnergy;
        this.maxTransfer = maxTransfer;
        this.energyPerUse = energyPerUse;

        return this;
    }

    public ItemCoreRF setCreative(boolean creative) {
        isCreative = creative;
        return this;
    }

    /* STANDARD METHODS */
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab) && showInCreative) {
            if (!isCreative) {
                items.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(this, 1, 0), 0));
            }
            items.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(this, 1, 0), maxEnergy));
        }
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged) && (slotChanged || getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getEnergyStored(stack) > 0;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return CoreProps.RGB_DURABILITY_FLUX;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(stack, 0);
        }
        return 1D - (double) stack.getTagCompound().getInteger(CoreProps.ENERGY) / (double) getMaxEnergyStored(stack);
    }

    @Override
    public boolean isActive(ItemStack stack) {
        return getMode(stack) == MODE.ENABLED.getValue() && getEnergyStored(stack) > getEnergyPerUse(stack);
    }

    /* HELPERS */
    protected int getEnergyPerUse(ItemStack stack) {
        if (isCreative) {
            return 0;
        }
        int unbreakingLevel = MathHelper.clamp(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack), 0, 4);
        return energyPerUse * (5 - unbreakingLevel) / 5;
    }

    protected int getTransfer(ItemStack container) {
        return maxTransfer;
    }

    public int useEnergy(ItemStack stack, int count, boolean simulate) {
        if (isCreative) {
            return 0;
        }
        int unbreakingLevel = MathHelper.clamp(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack), 0, 4);
        return extractEnergy(stack, count * energyPerUse * (5 - unbreakingLevel) / 5, simulate);
    }

    protected int useEnergyExact(ItemStack stack, int toUse, boolean simulate) {
        if (isCreative) {
            return 0;
        }
        int unbreakingLevel = MathHelper.clamp(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack), 0, 4);
        return extractEnergy(stack, toUse * (5 - unbreakingLevel) / 5, simulate);
    }

    /* IEnergyContainerItem */
    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        if (container.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(container, 0);
        }
        int stored = Math.min(container.getTagCompound().getInteger(CoreProps.ENERGY), getMaxEnergyStored(container));
        int receive = Math.min(maxReceive, Math.min(getMaxEnergyStored(container) - stored, getTransfer(container)));

        if (!simulate && !isCreative) {
            stored += receive;
            container.getTagCompound().setInteger(CoreProps.ENERGY, stored);
        }
        return receive;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        if (container.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(container, 0);
        }
        if (isCreative) {
            return maxExtract;
        }
        int stored = Math.min(container.getTagCompound().getInteger(CoreProps.ENERGY), getMaxEnergyStored(container));
        int extract = Math.min(maxExtract, stored);

        if (!simulate) {
            stored -= extract;
            container.getTagCompound().setInteger(CoreProps.ENERGY, stored);
        }
        return extract;
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        if (container.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(container, 0);
        }
        return Math.min(container.getTagCompound().getInteger(CoreProps.ENERGY), getMaxEnergyStored(container));
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        int enchant = EnchantmentHelper.getEnchantmentLevel(CoreEnchantments.holding, container);
        return maxEnergy + maxEnergy * enchant / 2;
    }

    /* IEnchantableItem */
    @Override
    public boolean canEnchant(ItemStack stack, Enchantment enchantment) {
        return enchantment == CoreEnchantments.holding;
    }

    /* CAPABILITIES */
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new EnergyContainerItemWrapper(stack, this);
    }

    public enum MODE {
        DISABLED(0),
        ENABLED(1);
        private final int value;

        MODE(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }
    }

}