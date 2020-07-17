package thundr.redstonerepository.blocks;

import cofh.core.block.BlockCore;
import cofh.core.render.IModelRegister;
import cofh.core.util.core.IInitializer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thundr.redstonerepository.RedstoneRepository;
import thundr.redstonerepository.items.blocks.ItemBlockStorage;

import java.util.Locale;

import static cofh.core.util.helpers.ItemHelper.registerWithHandlers;
import static cofh.core.util.helpers.RecipeHelper.addStorageRecipe;

public class BlockStorage extends BlockCore implements IInitializer, IModelRegister {

    public static final PropertyEnum<Type> VARIANT = PropertyEnum.create("type", Type.class);

    public static ItemStack blockGelidEnderium;
    public static ItemStack blockGelidGem;

    public BlockStorage() {

        super(Material.IRON, "redstonerepository");

        setUnlocalizedName("storage");
        setCreativeTab(RedstoneRepository.tabCommon);

        setHardness(25.0F);
        setResistance(120.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, Type.GELID_ENDERIUM));

        setHarvestLevel("pickaxe", 2);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (Type t : Type.values()) {
            items.add(new ItemStack(this, 1, t.metadata));
        }
    }

    /* TYPE METHODS */
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, Type.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return true;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(VARIANT).getLight();
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        return state.getValue(VARIANT).getHardness();
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        return state.getValue(VARIANT).getResistance();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (Type t : Type.values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), t.metadata, new ModelResourceLocation("redstonerepository:" + name, "type=" + t.getName()));
        }
    }

    /* IInitializer */
    @Override
    public boolean preInit() {

        this.setRegistryName("storage");
        ForgeRegistries.BLOCKS.register(this);

        ItemBlockStorage itemBlock = new ItemBlockStorage(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);

        blockGelidEnderium = new ItemStack(this, 1, Type.GELID_ENDERIUM.getMetadata());
        blockGelidGem = new ItemStack(this, 1, Type.GELID_GEM.getMetadata());


        registerWithHandlers("blockGelidEnderium", blockGelidEnderium);
        registerWithHandlers("blockGelidGem", blockGelidGem);

        RedstoneRepository.proxy.addIModelRegister(this);

        return true;
    }

    @Override
    public boolean initialize() {
        addStorageRecipe(blockGelidEnderium, "ingotGelidEnderium");
        addStorageRecipe(blockGelidGem, "gemGelid");
        return true;
    }

    public enum Type implements IStringSerializable {
        GELID_ENDERIUM(0, "blockGelidEnderium", blockGelidEnderium, 7),
        GELID_GEM(1, "blockGelidGem", blockGelidGem);

        private final int metadata;
        private final String name;
        private final ItemStack stack;
        private final int light;
        private final float hardness;
        private final float resistance;
        private final EnumRarity rarity;

        Type(int metadata, String name, ItemStack stack, int light, float hardness, float resistance, EnumRarity rarity) {
            this.metadata = metadata;
            this.name = name;
            this.stack = stack;

            this.light = light;
            this.hardness = hardness;
            this.resistance = resistance;
            this.rarity = rarity;
        }

        //TODO: not all of these types are needed
        Type(int metadata, String name, ItemStack stack, int light, float hardness, float resistance) {

            this(metadata, name, stack, light, hardness, resistance, EnumRarity.RARE);
        }

        Type(int metadata, String name, ItemStack stack, float hardness, float resistance) {
            this(metadata, name, stack, 0, hardness, resistance, EnumRarity.RARE);
        }

        Type(int metadata, String name, ItemStack stack, int light) {

            this(metadata, name, stack, light, 25.0F, 120.0F, EnumRarity.RARE);
        }

        Type(int metadata, String name, ItemStack stack) {
            this(metadata, name, stack, 0, 25.0F, 120.0F, EnumRarity.RARE);
        }

        public static Type byMetadata(int metadata) {
            return values()[metadata % values().length];
        }

        public int getMetadata() {
            return metadata;
        }

        @Override
        public String getName() {
            return name.toLowerCase(Locale.US);
        }

        public String getNameRaw() {
            return name;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getLight() {
            return light;
        }

        public float getHardness() {
            return hardness;
        }

        public float getResistance() {
            return resistance;
        }

        public EnumRarity getRarity() {
            return rarity;
        }
    }
}
