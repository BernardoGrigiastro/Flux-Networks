package sonar.fluxnetworks.common.integration.energy;
/* TODO GREGTECH INTEGRATION
import sonar.fluxnetworks.api.energy.IItemEnergyHandler;
import sonar.fluxnetworks.api.energy.ITileEnergyHandler;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class GTEnergyHandler implements ITileEnergyHandler, IItemEnergyHandler {

    public static final GTEnergyHandler INSTANCE = new GTEnergyHandler();

    @Override
    public boolean canRenderConnection(@Nonnull TileEntity tile, EnumFacing side) {
        return tile.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
    }

    @Override
    public boolean canAddEnergy(TileEntity tile, EnumFacing side) {
        if(canRenderConnection(tile, side)) {
            IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
            return container.inputsEnergy(side);
        }
        return false;
    }

    @Override
    public boolean canRemoveEnergy(TileEntity tile, EnumFacing side) {
        if(canRenderConnection(tile, side)) {
            IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
            return container.outputsEnergy(side);
        }
        return false;
    }

    @Override
    public long addEnergy(long amount, TileEntity tile, EnumFacing side, boolean simulate) {
        IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
        long demand = container.getEnergyCanBeInserted();
        if(demand == 0) {
            return 0;
        }
        long voltage = Math.min(container.getInputVoltage(), demand);
        if(simulate) {
            return Math.min(voltage << 2, amount);
        }
        voltage = Math.min(voltage, amount >> 2);
        if(voltage == 0) {
            return 0;
        }
        long energy = voltage * container.acceptEnergyFromNetwork(side, voltage, 1);
        return energy << 2;
    }

    @Override
    public long removeEnergy(long amount, TileEntity tile, EnumFacing side) {
        IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
        return container.removeEnergy(container.getOutputVoltage() * container.getOutputAmperage()) << 2;
    }

    @Override
    public boolean canAddEnergy(ItemStack stack) {
        return stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public boolean canRemoveEnergy(ItemStack stack) {
        return stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public long addEnergy(long amount, ItemStack stack, boolean simulate) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem.charge(amount >> 2, electricItem.getTier(), false, simulate) << 2;
    }

    @Override
    public long removeEnergy(long amount, ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem.discharge(amount >> 2, electricItem.getTier(), false, true, false) << 2;
    }
}
*/