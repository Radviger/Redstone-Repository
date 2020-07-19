package thundr.redstonerepository.items.armor;

import cofh.core.init.CoreProps;
import cofh.core.util.helpers.EnergyHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.StringHelper;
import cofh.redstonearsenal.item.armor.ItemArmorFlux;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thundr.redstonerepository.RedstoneRepository;
import thundr.redstonerepository.api.IArmorEnderium;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.helpers.StringHelper.*;

public class ItemArmorEnderium extends ItemArmorFlux implements IArmorEnderium {
    public ItemArmorEnderium(ArmorMaterial material, EntityEquipmentSlot type) {
        super(material, type);
        setNoRepair();
        setCreativeTab(RedstoneRepository.tabCommon);
        setMaxDamage(5);

        this.maxEnergy = 4000000;
        this.energyPerDamage = 4500;
        this.absorbRatio = .95D;
        this.maxTransfer = 20000;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.getItem().getRegistryName().toString().contains("boot")) {
            tooltip.add(GRAY + localize("info.redstonerepository.tooltip.fallingCost"));
        }
        if (StringHelper.displayShiftForDetail && !isShiftKeyDown()) {
            tooltip.add(StringHelper.shiftForDetails());
        }
        if (isShiftKeyDown()) {
            if (stack.getTagCompound() == null) {
                EnergyHelper.setDefaultEnergyTag(stack, 0);
            }
            tooltip.add(BRIGHT_BLUE + localize("info.redstonerepository.tooltip.fullSet") + ": " + GRAY + localize("info.redstonerepository.tooltip.fireCost"));
            tooltip.add(localize("info.cofh.charge") + ": " + StringHelper.formatNumber(stack.getTagCompound().getInteger("Energy")) + " / " + StringHelper.formatNumber(getMaxEnergyStored(stack)) + " RF");
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return CoreProps.RGB_DURABILITY_ENDER;
    }

    //soon
    public int useEnergy(ItemStack container, int maxExtract, boolean simulate) {
        int unbreakingLevel = MathHelper.clamp(EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, container), 0, 10);
        if (MathHelper.RANDOM.nextInt(3 + unbreakingLevel) >= 3) {
            return 0;
        }
        return extractEnergy(container, maxExtract, false);
    }

    @Override
    public boolean isEnderiumArmor(ItemStack stack) {
        return true;
    }
}
