package thundr.redstonerepository.init;

import cofh.core.gui.CreativeTabCore;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thundr.redstonerepository.RedstoneRepository;

public class RedstoneRepositoryProps {
    //textures
    public static final String PATH_GUI = "redstonerepository:textures/gui/";
    public static final ResourceLocation FEEDER_GUI_STORAGE = new ResourceLocation(PATH_GUI + "storage_1.png");

    private RedstoneRepositoryProps() {

    }

    public static void preInit() {

        configCommon();
        configClient();
    }

    /* HELPERS */
    private static void configCommon() {

    }

    private static void configClient() {
        RedstoneRepository.tabCommon = new CreativeTabCore("redstonerepository") {
            @Override
            @SideOnly(Side.CLIENT)
            public ItemStack getIconItemStack() {
                return new ItemStack(RedstoneRepositoryEquipment.ToolSet.GELID.itemSword);
            }
        };
    }

}
