package thundr.redstonerepository.api;

import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.item.ItemStack;

public interface IToolEnderium extends IEnergyContainerItem {
    boolean isEmpowered(ItemStack stack);
}
