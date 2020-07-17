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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

        if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
            tooltip.add(StringHelper.shiftForDetails());
        }
        if (!StringHelper.isShiftKeyDown()) {
            return;
        }
        tooltip.add(StringHelper.getInfoText("info.redstonerepository.ring.mining.title"));

        if (isActive(stack)) {
            tooltip.add(StringHelper.localizeFormat("info.redstonearsenal.tool.chargeOff", StringHelper.getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));
        } else {
            tooltip.add(StringHelper.localizeFormat("info.redstonearsenal.tool.chargeOn", StringHelper.getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));
        }

        if (!RedstoneRepositoryEquipment.EquipmentInit.enable[3]) {
            tooltip.add(StringHelper.RED + "Baubles not loaded: Recipe disabled.");
        }
        tooltip.add(StringHelper.localize("info.cofh.charge") + ": " + StringHelper.getScaledNumber(getEnergyStored(stack)) + " / " + StringHelper.getScaledNumber(getMaxEnergyStored(stack)) + " RF");
        tooltip.add(StringHelper.localize("info.cofh.send") + "/" + StringHelper.localize("info.cofh.receive") + ": " + StringHelper.formatNumber(maxTransfer) + "/" + StringHelper.formatNumber(maxTransfer) + " RF/t");
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