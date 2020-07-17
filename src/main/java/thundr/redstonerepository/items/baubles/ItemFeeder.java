package thundr.redstonerepository.items.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cofh.api.item.IInventoryContainerItem;
import cofh.core.init.CoreEnchantments;
import cofh.core.key.KeyBindingItemMultiMode;
import cofh.core.util.helpers.StringHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import thundr.redstonerepository.RedstoneRepository;
import thundr.redstonerepository.api.IHungerStorageItem;
import thundr.redstonerepository.gui.GuiHandler;
import thundr.redstonerepository.init.RedstoneRepositoryEquipment;
import thundr.redstonerepository.items.ItemCoreRF;
import thundr.redstonerepository.util.HungerHelper;

import javax.annotation.Nullable;
import java.util.List;

import static thundr.redstonerepository.RedstoneRepository.NAME;


@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemFeeder extends ItemCoreRF implements IBauble, IInventoryContainerItem, IHungerStorageItem {

    public int hungerPointsMax;
    private int saturationFillMax;

    public ItemFeeder() {
        super(NAME);
        setMaxStackSize(1);
        setCreativeTab(RedstoneRepository.tabCommon);
    }

    public ItemFeeder(int hungerPointsMax, int maxEnergy, int maxTransfer, int energyPerUse, int saturationFillMax) {
        super(NAME);
        setMaxStackSize(1);
        setCreativeTab(RedstoneRepository.tabCommon);
        setNoRepair();
        this.hungerPointsMax = hungerPointsMax;
        this.maxEnergy = maxEnergy;
        this.maxTransfer = maxTransfer;
        this.energyPerUse = energyPerUse;
        this.saturationFillMax = saturationFillMax;

    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack feeder, EntityLivingBase player) {
        //enjoy your soylent...
        if (player.isServerWorld()) {
            if (isActive(feeder)) {
                if (getHungerPoints(feeder) > 0 && (getEnergyStored(feeder) >= getEnergyPerUse(feeder))) {
                    if (player instanceof EntityPlayer) {
                        EntityPlayer ePlayer = (EntityPlayer) player;
                        if (ePlayer.getFoodStats().needFood()) {
                            HungerHelper.addHunger(ePlayer, 1);
                            useHungerPoints(feeder, 1, ePlayer);
                            useEnergy(feeder, 1, false);
                        } else if (ePlayer.getFoodStats().getSaturationLevel() < saturationFillMax) {
                            HungerHelper.addSaturation(ePlayer, 1);
                            useHungerPoints(feeder, 1, ePlayer);
                            useEnergy(feeder, 1, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
            tooltip.add(StringHelper.shiftForDetails());
        }
        if (!StringHelper.isShiftKeyDown()) {
            tooltip.add(StringHelper.getInfoText("info.redstonerepository.feeder.short"));
            return;
        }
        tooltip.add(StringHelper.getInfoText("info.redstonerepository.feeder.title"));

        if (isActive(stack)) {
            tooltip.add(StringHelper.localizeFormat("info.redstonearsenal.tool.chargeOff", StringHelper.getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));
        } else {
            tooltip.add(StringHelper.localizeFormat("info.redstonearsenal.tool.chargeOn", StringHelper.getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));
        }

        if (!RedstoneRepositoryEquipment.EquipmentInit.enable[1]) {
            tooltip.add(StringHelper.RED + "Baubles not loaded: Recipe disabled.");
        }
        tooltip.add(StringHelper.localize("info.redstonerepository.hungerPoints") + ": " + StringHelper.ORANGE + StringHelper.getScaledNumber(getHungerPoints(stack)) + " / " + StringHelper.getScaledNumber(getMaxHungerPoints(stack)));
        tooltip.add(StringHelper.localize("info.cofh.charge") + ": " + StringHelper.RED + StringHelper.getScaledNumber(getEnergyStored(stack)) + " / " + StringHelper.getScaledNumber(getMaxEnergyStored(stack)) + " RF");
    }


    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.BELT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.openGui(RedstoneRepository.instance, GuiHandler.FEEDER_ID, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getSizeInventory(ItemStack container) {
        return 1;
    }

    @Override
    public int getHungerPoints(ItemStack container) {
        HungerHelper.setDefaultHungerTag(container);
        return Math.min(container.getTagCompound().getInteger("Hunger"), getMaxHungerPoints(container));
    }

    @Override
    public int receiveHungerPoints(ItemStack container, int maxReceive, boolean simulate) {
        HungerHelper.setDefaultHungerTag(container);
        int stored = Math.min(container.getTagCompound().getInteger("Hunger"), getMaxHungerPoints(container));
        int receive = Math.min(maxReceive, getMaxHungerPoints(container) - stored);

        if (!isCreative && !simulate) {
            stored += receive;
            container.getTagCompound().setInteger("Hunger", stored);
        }
        return receive;
    }

    @Override
    public int useHungerPoints(ItemStack container, int maxExtract, EntityPlayer player) {
        if (isCreative) {
            return maxExtract;
        }
        HungerHelper.setDefaultHungerTag(container);
        int stored = Math.min(container.getTagCompound().getInteger("Hunger"), getMaxHungerPoints(container));
        int extract = Math.min(maxExtract, stored);

        stored -= extract;
        container.getTagCompound().setInteger("Hunger", stored);

        return extract;
    }

    @Override
    public int getMaxHungerPoints(ItemStack container) {
        int enchant = EnchantmentHelper.getEnchantmentLevel(CoreEnchantments.holding, container);
        return hungerPointsMax + hungerPointsMax * enchant / 2;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }
}
