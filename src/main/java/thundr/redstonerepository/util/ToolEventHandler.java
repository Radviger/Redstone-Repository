package thundr.redstonerepository.util;

import cofh.core.util.helpers.BaublesHelper;
import cofh.redstonearsenal.item.tool.ItemToolFlux;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thundr.redstonerepository.RedstoneRepository;
import thundr.redstonerepository.api.IToolEnderium;
import thundr.redstonerepository.items.baubles.ItemRingEffect;
import thundr.redstonerepository.items.baubles.ItemRingMining;
import thundr.redstonerepository.items.tools.gelidenderium.ItemAxeGelidEnderium;
import thundr.redstonerepository.items.tools.gelidenderium.ItemPickaxeGelidEnderium;
import thundr.redstonerepository.items.tools.gelidenderium.ItemShovelGelidEnderium;
import thundr.redstonerepository.items.tools.gelidenderium.ItemSickleGelidEnderium;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ToolEventHandler {

    public static int pickaxeDistanceFactor;
    public static int pickaxeDimensionFactor;

    public static void preInit() {
        pickaxeDistanceFactor = RedstoneRepository.CONFIG.get("Equipment.Tools", "PickaxeDistanceDrainFactor", 5,
            "Set the factor that scales the power drained from the Gelid Enderium Pickaxe when teleporting items over a distance. (distance*factor*itemDrops=power)");
        pickaxeDimensionFactor = RedstoneRepository.CONFIG.get("Equipment.Tools", "PickaxeDimensionDrainFactor", 7500,
            "Set the factor that scales the power drained from the Gelid Enderium Pickaxe when teleporting items between dimensions. This is a flat value per item.");
    }

    private boolean isGelidTool(Item item) {
        return item instanceof ItemPickaxeGelidEnderium || item instanceof ItemAxeGelidEnderium
            || item instanceof ItemSickleGelidEnderium || item instanceof ItemShovelGelidEnderium;
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        if (!world.isRemote) {
            if (event.getHarvester() != null && !event.getHarvester().getHeldItem(EnumHand.MAIN_HAND).isEmpty() &&
                isGelidTool(event.getHarvester().getHeldItem(EnumHand.MAIN_HAND).getItem())) {

                ItemStack stack = event.getHarvester().getHeldItem(EnumHand.MAIN_HAND);
                ItemToolFlux tool = (ItemToolFlux) event.getHarvester().getHeldItem(EnumHand.MAIN_HAND).getItem();

                if (isEmpowered(stack)) {
                    if (stack.getTagCompound() == null) {
                        stack.setTagCompound(new NBTTagCompound());
                    }

                    NBTTagCompound tag = stack.getTagCompound();
                    int coordX = tag.getInteger("CoordX");
                    int coordY = tag.getInteger("CoordY");
                    int coordZ = tag.getInteger("CoordZ");
                    int dimID = tag.getInteger("DimID");
                    int side = tag.getInteger("Side");
                    boolean isBound = tag.getBoolean("Bound");

                    if (isBound) {
                        World boundWorld = DimensionManager.getWorld(dimID);
                        if (event.getWorld().getBlockState(event.getPos()) != boundWorld.getBlockState(new BlockPos(coordX, coordY, coordZ))) {
                            TileEntity bound = boundWorld.getTileEntity(new BlockPos(coordX, coordY, coordZ));
                            IItemHandler inventory;
                            EnumFacing dir = EnumFacing.getFront(side);

                            if (bound.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
                                inventory = bound.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
                            } else {
                                return;
                            }

                            List<ItemStack> drops = event.getDrops();
                            for (Iterator<ItemStack> iterator = drops.iterator(); iterator.hasNext();) {
                                ItemStack drop = iterator.next();
                                ItemStack returned = ItemHandlerHelper.insertItemStacked(inventory, drop, false);

                                //drain energy depending on how far away you are from the inventory
                                int temp = drainEnergyByDistance(event.getPos(), new BlockPos(coordX, coordY, coordZ),
                                    !(dimID == event.getHarvester().dimension));
                                tool.extractEnergy(stack, temp, false);
                                if (returned.isEmpty()) {
                                    world.playSound(null, event.getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1F, 0.8F + world.rand.nextFloat() * 0.2F);
                                    world.playSound(null, bound.getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1F, 0.8F + world.rand.nextFloat() * 0.2F);
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //calculates the proper amount of energy to drain by how far the harvested blocks are being teleported
    //returns energy to drain
    private int drainEnergyByDistance(BlockPos from, BlockPos to, boolean interdim) {
        if (!interdim) {
            return (int) ((Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2) + Math.pow(from.getX() - to.getX(), 2))) * pickaxeDistanceFactor);
        } else {
            return pickaxeDimensionFactor;
        }
    }

    private static Method IS_EMPOWERED;

    public boolean isEmpowered(ItemStack stack) {
        IToolEnderium tool = (IToolEnderium) stack.getItem();
        return tool.isEmpowered(stack);
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            if (event.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getEntity();
                for (ItemStack itemStack : BaublesHelper.getBaubles(player)) {
                    //make sure we have a player with a ring on
                    if (itemStack.getItem() instanceof ItemRingEffect) {
                        ItemRingEffect ring = (ItemRingEffect) itemStack.getItem();
                        if (ring.isActive(itemStack)) {
                            ArrayList<PotionEffect> potions = ring.globalMap.get(player.getUniqueID());
                            if (potions != null) {
                                int diff = player.getActivePotionEffects().size() - potions.size();
                                if (diff > 0) {
                                    ring.useEnergy(itemStack, (int) Math.pow(2, diff + 6.0), false);
                                    player.clearActivePotions();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockMined(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            for (ItemStack itemStack : BaublesHelper.getBaubles(player)) {
                if (itemStack.getItem() instanceof ItemRingMining) {
                    ItemRingMining ring = (ItemRingMining) itemStack.getItem();

                    if (ring.isActive(itemStack) && !player.onGround) {
                        event.setNewSpeed(event.getOriginalSpeed() * 5.0f);
                        ring.useEnergy(itemStack, 1, false);
                    }
                }
            }
        }
    }
}

