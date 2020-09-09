package thundr.redstonerepository.items.tools.gelidenderium;

import cofh.core.init.CoreProps;
import cofh.core.util.RayTracer;
import cofh.core.util.helpers.StringHelper;
import cofh.redstonearsenal.item.tool.ItemPickaxeFlux;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import thundr.redstonerepository.api.IToolEnderium;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemPickaxeGelidEnderium extends ItemPickaxeFlux implements IToolEnderium {

    public ItemPickaxeGelidEnderium(ToolMaterial toolMaterial) {
        super(toolMaterial);
        maxEnergy = GelidEnderiumEnergy.maxEnergy;
        energyPerUse = GelidEnderiumEnergy.energyPerUse;
        energyPerUseCharged = GelidEnderiumEnergy.energyPerUseCharged;
        maxTransfer = GelidEnderiumEnergy.maxTransfer;
    }

    @Override
    public boolean isEmpowered(ItemStack stack) {
        return super.isEmpowered(stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (!world.isRemote) {
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }

                TileEntity tile = world.getTileEntity(pos);

                NBTTagCompound compound = stack.getTagCompound();

                ITextComponent msg;

                if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
                    compound.setBoolean("Bound", true);
                    compound.setInteger("CoordX", pos.getX());
                    compound.setInteger("CoordY", pos.getY());
                    compound.setInteger("CoordZ", pos.getZ());
                    compound.setInteger("DimID", world.provider.getDimension());
                    compound.setInteger("Side", facing.getIndex());
                    msg = new TextComponentTranslation("info.redstonerepository.tooltip.linked");
                    msg.getStyle().setColor(TextFormatting.GREEN);
                } else {
                    compound.setBoolean("Bound", false);
                    compound.removeTag("CoordX");
                    compound.removeTag("CoordY");
                    compound.removeTag("CoordZ");
                    compound.removeTag("DimID");
                    compound.removeTag("Side");
                    msg = new TextComponentTranslation("info.redstonerepository.tooltip.unlinked");
                    msg.getStyle().setColor(TextFormatting.RED);
                }
                world.playSound(null, pos, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.BLOCKS, 1F, 1F);
                player.sendStatusMessage(msg, true);
            }
            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.redstonerepository.tooltip.bind"));
        super.addInformation(stack, world, tooltip, flagIn);
        if (stack.hasTagCompound()) {
            //coordX, coordY, coordZ, dimID, side
            int[] values = new int[5];
            boolean isBound = false;
            NBTTagCompound tags = stack.getTagCompound();

            if (tags.hasKey("CoordX"))
                values[0] = tags.getInteger("CoordX");
            if (tags.hasKey("CoordY"))
                values[1] = tags.getInteger("CoordY");
            if (tags.hasKey("CoordZ"))
                values[2] = tags.getInteger("CoordZ");
            if (tags.hasKey("DimID"))
                values[3] = tags.getInteger("DimID");
            if (tags.hasKey("Side"))
                values[4] = tags.getInteger("Side");
            if (tags.hasKey("Bound"))
                isBound = tags.getBoolean("Bound");

            String sideString = EnumFacing.getFront(values[4]).getName().toLowerCase();

            if (StringHelper.isControlKeyDown()) {
                if (isBound) {
                    tooltip.add(StringHelper.localize(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.redstonerepository.tooltip.bound") + StringHelper.LIGHT_GRAY + " " + values[0] + ", " + values[1] + ", " + values[2] + ". DimID: " + values[3]));
                    tooltip.add(StringHelper.localize(StringHelper.BRIGHT_BLUE + StringHelper.localize("info.redstonerepository.tooltip.side") + StringHelper.LIGHT_GRAY + " " + Character.toUpperCase(sideString.charAt(0)) + sideString.substring(1)));
                } else {
                    tooltip.add(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.redstonerepository.tooltip.notbound"));
                }
            } else {
                tooltip.add(StringHelper.localize("info.redstonerepository.tooltip.hold") + " " + StringHelper.YELLOW + StringHelper.ITALIC + StringHelper.localize("info.redstonerepository.tooltip.control") + " " + StringHelper.LIGHT_GRAY + StringHelper.localize("info.redstonerepository.tooltip.forDetails"));
            }
        }
    }

    // From ItemFluxHammer and ItemFluxPickaxe combined.
    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        IBlockState state = world.getBlockState(pos);

        if (state.getBlockHardness(world, pos) == 0.0F) {
            return false;
        }
        if (!canHarvestBlock(state, stack)) {
            if (!player.capabilities.isCreativeMode) {
                useEnergy(stack, false);
            }
            return false;
        }

        float refStrength = state.getPlayerRelativeBlockHardness(player, world, pos);
        if (refStrength != 0.0F) {
            boolean used = false;

            if (isEmpowered(stack) && canHarvestBlock(state, stack)) {
                RayTraceResult traceResult = RayTracer.retrace(player, false);

                if (traceResult == null || traceResult.sideHit == null) {
                    return false;
                }
                BlockPos adjPos;
                IBlockState adjState;
                float strength;

                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                int radius = 1;

                switch (traceResult.sideHit) {
                    case DOWN:
                    case UP:
                        for (int i = x - radius; i <= x + radius; i++) {
                            for (int k = z - radius; k <= z + radius; k++) {
                                adjPos = new BlockPos(i, y, k);
                                adjState = world.getBlockState(adjPos);
                                strength = adjState.getPlayerRelativeBlockHardness(player, world, adjPos);
                                if (strength > 0F && refStrength / strength <= 10F) {
                                    used |= harvestBlock(world, adjPos, player);
                                }
                            }
                        }
                        break;
                    case NORTH:
                    case SOUTH:
                        for (int i = x - radius; i <= x + radius; i++) {
                            for (int j = y - radius; j <= y + radius; j++) {
                                adjPos = new BlockPos(i, j, z);
                                adjState = world.getBlockState(adjPos);
                                strength = adjState.getPlayerRelativeBlockHardness(player, world, adjPos);
                                if (strength > 0F && refStrength / strength <= 10F) {
                                    used |= harvestBlock(world, adjPos, player);
                                }
                            }
                        }
                        break;
                    case WEST:
                    case EAST:
                        for (int j = y - radius; j <= y + radius; j++) {
                            for (int k = z - radius; k <= z + radius; k++) {
                                adjPos = new BlockPos(x, j, k);
                                adjState = world.getBlockState(adjPos);
                                strength = adjState.getPlayerRelativeBlockHardness(player, world, adjPos);
                                if (strength > 0F && refStrength / strength <= 10F) {
                                    used |= harvestBlock(world, adjPos, player);
                                }
                            }
                        }
                        break;
                }
            }
            if (used && !player.capabilities.isCreativeMode) {
                useEnergy(stack, false);
            }
        }
        return false;
    }

    @Override
    public ImmutableList<BlockPos> getAOEBlocks(ItemStack stack, BlockPos pos, EntityPlayer player) {
        ArrayList<BlockPos> area = new ArrayList<>();
        World world = player.getEntityWorld();

        RayTraceResult traceResult = RayTracer.retrace(player, false);
        if (traceResult == null || traceResult.sideHit == null || !isEmpowered(stack) || !canHarvestBlock(world.getBlockState(pos), stack)) {
            return ImmutableList.copyOf(area);
        }
        BlockPos harvestPos;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int radius = 1;

        switch (traceResult.sideHit) {
            case DOWN:
            case UP:
                for (int i = x - radius; i <= x + radius; i++) {
                    for (int k = z - radius; k <= z + radius; k++) {
                        if (i == x && k == z) {
                            continue;
                        }
                        harvestPos = new BlockPos(i, y, k);
                        if (canHarvestBlock(world.getBlockState(harvestPos), stack)) {
                            area.add(harvestPos);
                        }
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                for (int i = x - radius; i <= x + radius; i++) {
                    for (int j = y - radius; j <= y + radius; j++) {
                        if (i == x && j == y) {
                            continue;
                        }
                        harvestPos = new BlockPos(i, j, z);
                        if (canHarvestBlock(world.getBlockState(harvestPos), stack)) {
                            area.add(harvestPos);
                        }
                    }
                }
                break;
            case WEST:
            case EAST:
                for (int j = y - radius; j <= y + radius; j++) {
                    for (int k = z - radius; k <= z + radius; k++) {
                        if (j == y && k == z) {
                            continue;
                        }
                        harvestPos = new BlockPos(x, j, k);
                        if (canHarvestBlock(world.getBlockState(harvestPos), stack)) {
                            area.add(harvestPos);
                        }
                    }
                }
                break;
        }
        return ImmutableList.copyOf(area);
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
