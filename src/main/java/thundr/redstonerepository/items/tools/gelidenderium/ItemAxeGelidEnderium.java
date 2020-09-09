package thundr.redstonerepository.items.tools.gelidenderium;

import cofh.core.init.CoreProps;
import cofh.core.util.helpers.StringHelper;
import cofh.redstonearsenal.item.tool.ItemAxeFlux;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import thundr.redstonerepository.api.IToolEnderium;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class ItemAxeGelidEnderium extends ItemAxeFlux implements IToolEnderium {
    public static int blocksPerTick;

    public ItemAxeGelidEnderium(Item.ToolMaterial toolMaterial, int axeBlocksCutPerTick) {
        super(toolMaterial);
        maxEnergy = GelidEnderiumEnergy.maxEnergy;
        energyPerUse = GelidEnderiumEnergy.energyPerUse;
        energyPerUseCharged = GelidEnderiumEnergy.energyPerUseCharged;
        maxTransfer = GelidEnderiumEnergy.maxTransfer;
        damage = 10;
        blocksPerTick = axeBlocksCutPerTick;
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

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        IBlockState state = world.getBlockState(pos);

        Block block = state.getBlock();

        float refStrength = state.getPlayerRelativeBlockHardness(player, world, pos);
        if (refStrength != 0.0F) {
            if (isEmpowered(stack) && block.isWood(world, pos) && canHarvestBlock(state, stack)) {
                if (!world.isRemote) {
                    MinecraftForge.EVENT_BUS.register(new CutTreeTask(stack, pos, player));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return CoreProps.RGB_DURABILITY_ENDER;
    }

    @Override
    protected boolean harvestBlock(World world, BlockPos pos, EntityPlayer player) {
        if (world.isAirBlock(pos)) {
            return false;
        }
        EntityPlayerMP playerMP = null;
        if (player instanceof EntityPlayerMP) {
            playerMP = (EntityPlayerMP) player;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // only effective materials
        if (!(toolClasses.contains(state.getBlock().getHarvestTool(state)) || canHarvestBlock(state, player.getHeldItemMainhand()))) {
            return false;
        }
        if (!ForgeHooks.canHarvestBlock(block, player, world, pos)) {
            return false;
        }

        if (!world.isRemote) {
            // send the blockbreak event
            int xpToDrop = 0;
            if (playerMP != null) {
                xpToDrop = ForgeHooks.onBlockBreakEvent(world, playerMP.interactionManager.getGameType(), playerMP, pos);
                if (xpToDrop == -1) {
                    return false;
                }
            }

            if (block.removedByPlayer(state, world, pos, player, !player.capabilities.isCreativeMode)) {
                block.onBlockDestroyedByPlayer(world, pos, state);

                if (!player.capabilities.isCreativeMode) {
                    block.harvestBlock(world, player, pos, state, world.getTileEntity(pos), player.getHeldItemMainhand());
                    if (xpToDrop > 0) {
                        block.dropXpOnBlockBreak(world, pos, xpToDrop);
                    }
                }
            }
            // always send block update to client
            playerMP.connection.sendPacket(new SPacketBlockChange(world, pos));
        } else {
            if (block.removedByPlayer(state, world, pos, player, !player.capabilities.isCreativeMode)) {
                block.onBlockDestroyedByPlayer(world, pos, state);
            }
            Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, Minecraft.getMinecraft().objectMouseOver.sideHit));
        }
        return true;
    }

    public static class CutTreeTask {
        public World world;
        public ItemStack tool;
        public ItemAxeGelidEnderium axe;
        public BlockPos pos;
        public EntityPlayer player = null;
        public int maxIterations = 10000;
        public int iterationCount = 0;

        public Queue<BlockPos> candidates = new LinkedList<BlockPos>();
        public HashSet<BlockPos> visited = new HashSet<BlockPos>();

        public CutTreeTask(@Nonnull ItemStack stack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
            if (stack.getItem() instanceof ItemAxeGelidEnderium) {
                this.tool = stack;
                // Cast to correct item
                this.axe = (ItemAxeGelidEnderium) stack.getItem();
            } else {
                // We're not a Gelid Enderium Axe!! GAAHHHH
                unregister();
                return;
            }
            this.pos = pos;
            this.player = player;
            this.world = player.getEntityWorld();

            candidates.add(pos); // add starting block
        }

        @SubscribeEvent
        public void cutTree(TickEvent.WorldTickEvent event) {
            if (event.side.isClient()) {
                //Only run on server.
                unregister();
                return;
            }

            // Ignore ticks from other dimensions.
            if (event.world.provider.getDimension() != world.provider.getDimension()) {
                return;
            }

            if (axe == null) {
                unregister();
                return;
            }

            // Check to see if we have enough energy to cut the log.
            if (axe.getEnergyStored(tool) < axe.getEnergyPerUse(tool)) {
                unregister();
                return;
            }

            int blocksIter = axe.blocksPerTick;
            // Loop through the blocks in the candidates until we break one.
            while (blocksIter > 0) {
                // check if any blocks in queue. If there are none, stop.
                if (candidates.isEmpty()) {
                    unregister();
                    return;
                }

                // Get current block position
                BlockPos curPos = candidates.remove();
                // Check to see if we've visited it before.
                // If we have, continue to the next block.
                // Else look at current block.
                if (!visited.add(curPos)) {
                    continue;
                }

                IBlockState state = world.getBlockState(curPos);
                if (!state.getBlock().isWood(world, curPos)) {
                    // Not a log, try the next one.
                    continue;
                }

                //TODO: Verify that this hardness check works
                float refStrength = state.getPlayerRelativeBlockHardness(player, world, curPos);
                if (refStrength != 0.0F) {

                    // Only check cardinal direction
                    BlockPos newPos;
                    EnumFacing[] cardinals = {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
                    for (EnumFacing face : cardinals) {
                        newPos = curPos.offset(face);
                        if (!visited.contains(newPos)) {
                            candidates.add(newPos);
                        }
                    }
                    // Top layer: 3x3 check
                    int y = 1; // y offset
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            newPos = curPos.add(x, y, z);
                            if (!visited.contains(newPos)) {
                                candidates.add(newPos);
                            }
                        }
                    }

                    //If we get here, we should actually break the block.
                    axe.harvestBlock(world, curPos, player);
                    world.playSound(null, curPos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!player.capabilities.isCreativeMode) {
                        //Use energy and check to see if we have enough.
                        if (axe.useEnergy(tool, false) == 0) {
                            //Stop, we've run out of energy!
                            break;
                        }
                    }

                    iterationCount++;
                    if (iterationCount > maxIterations) {
                        unregister();
                        return;
                    }
                    blocksIter--;
                }
            }
        }

        private void unregister() {
            // goodbye cruel world
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }
}

