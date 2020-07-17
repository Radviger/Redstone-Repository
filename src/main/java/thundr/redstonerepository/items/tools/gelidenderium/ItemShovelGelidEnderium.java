package thundr.redstonerepository.items.tools.gelidenderium;

import cofh.core.init.CoreProps;
import cofh.core.util.helpers.StringHelper;
import cofh.redstonearsenal.item.tool.ItemShovelFlux;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.List;


public class ItemShovelGelidEnderium extends ItemShovelFlux {

    public ItemShovelGelidEnderium(ToolMaterial toolMaterial) {
        super(toolMaterial);
        maxEnergy = GelidEnderiumEnergy.maxEnergy;
        energyPerUse = GelidEnderiumEnergy.energyPerUse;
        energyPerUseCharged = GelidEnderiumEnergy.energyPerUseCharged;
        maxTransfer = GelidEnderiumEnergy.maxTransfer;
    }

    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        ItemStack stack = player.getHeldItem(hand);

        if (!player.isSneaking() && stack.getItem() instanceof ItemShovelGelidEnderium) {
            ItemShovelGelidEnderium shovel = (ItemShovelGelidEnderium) stack.getItem();

            if (!player.canPlayerEdit(pos, facing, stack)) {
                return EnumActionResult.FAIL;
            }
            if (!isEmpowered(stack) && getEnergyStored(stack) > energyPerUseCharged) {
                growCrop(worldIn, pos, player, stack, facing, hand, energyPerUseCharged);
            } else if (isEmpowered(stack) && getEnergyStored(stack) > energyPerUseCharged * 9 || player.capabilities.isCreativeMode) {
                for (int x = -1; x < 2; x++) {
                    for (int z = -1; z < 2; z++) {
                        BlockPos newPos = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                        growCrop(worldIn, newPos, player, stack, facing, hand, energyPerUseCharged * 9);
                    }
                }
            } else {
                return EnumActionResult.FAIL;
            }
        } else {
            super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.SUCCESS;
    }

    private void growCrop(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing facing, EnumHand hand, int energy) {

        Block block = player.world.getBlockState(pos).getBlock();

        if (!world.isRemote)
            if (block instanceof IGrowable || block instanceof IPlantable || block == Blocks.MYCELIUM) {
                //adds an extra 50 percent chance for a second call.
                if (world.rand.nextBoolean()) {
                    world.scheduleBlockUpdate(pos, block, 0, 100);
                }
                world.scheduleBlockUpdate(pos, block, 0, 100);
                if (!player.capabilities.isCreativeMode) {
                    useEnergy(stack, false);
                }
                //particle spawn (green bonemeal)
                if (world.rand.nextFloat() >= .90F) {
                    world.playEvent(2005, pos, 0);
                }
            }
    }

// TODO: maybe we will revisit this later
//	@Override
//	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
//
//        int x = pos.getX();
//        int y = pos.getY();
//        int z = pos.getZ();
//
////        if (!(player instanceof EntityPlayer)) {
////            return false;
////        }
//		World world = player.world;
//		IBlockState state = world.getBlockState(pos);
//        if (state.getBlockHardness(world, pos) == 0.0D) {
//            return true;
//        }
//
//        if (effectiveBlocks.contains(state.getBlock()) && isEmpowered(stack)) {
//            for (int i = x - 2; i <= x + 2; i++) {
//                for (int k = z - 2; k <= z + 2; k++) {
//                    for (int j = y - 2; j <= y + 2; j++) {
//                        if (world.getBlockState(pos) == state) {
//                            harvestBlock(world, pos, player);
//	                        if (!player.capabilities.isCreativeMode) {
//		                        extractEnergy(stack, energyPerUseCharged, false);
//	                        }
//                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(StringHelper.BRIGHT_GREEN + "Right click to rapidly grow crops. Works better when empowered.");
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return CoreProps.RGB_DURABILITY_ENDER;
    }
}
