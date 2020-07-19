package thundr.redstonerepository.items.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cofh.core.key.KeyBindingItemMultiMode;
import cofh.core.util.helpers.StringHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import thundr.redstonerepository.RedstoneRepository;
import thundr.redstonerepository.init.RedstoneRepositoryEquipment;
import thundr.redstonerepository.items.ItemCoreRF;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.helpers.StringHelper.*;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemRingMining extends ItemCoreRF implements IBauble {

    public ItemRingMining() {
        super(RedstoneRepository.NAME);

        maxEnergy = 4000000;
        maxTransfer = 5000;
        energyPerUse = 100;
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag advanced) {
        if (isShiftKeyDown()) {
            tooltip.add(getInfoText("info.redstonerepository.ring.mining.title"));

            if (isActive(stack)) {
                tooltip.add(localizeFormat("info.redstonearsenal.tool.chargeOff", getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));
            } else {
                tooltip.add(localizeFormat("info.redstonearsenal.tool.chargeOn", getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));
            }

            if (!RedstoneRepositoryEquipment.EquipmentInit.enable[3]) {
                tooltip.add(RED + localize("info.redstonerepository.tooltip.noBaubleRecipe"));
            }
            tooltip.add(localize("info.cofh.charge") + ": " + getScaledNumber(getEnergyStored(stack)) + " / " + getScaledNumber(getMaxEnergyStored(stack)) + " RF");
            tooltip.add(localize("info.cofh.send") + "/" + localize("info.cofh.receive") + ": " + formatNumber(maxTransfer) + "/" + formatNumber(maxTransfer) + " RF/t");
        } else if (StringHelper.displayShiftForDetail) {
            tooltip.add(shiftForDetails());
        }
    }

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.RING;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }
}