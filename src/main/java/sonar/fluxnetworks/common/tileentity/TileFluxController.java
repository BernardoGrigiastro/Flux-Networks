package sonar.fluxnetworks.common.tileentity;

import net.minecraft.item.ItemStack;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.device.IFluxController;
import sonar.fluxnetworks.api.network.FluxDeviceType;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.common.connection.transfer.FluxControllerHandler;
import sonar.fluxnetworks.common.misc.FluxGuiStack;
import sonar.fluxnetworks.common.registry.RegistryBlocks;

import javax.annotation.Nonnull;

public class TileFluxController extends TileFluxDevice implements IFluxController {

    private final FluxControllerHandler handler = new FluxControllerHandler(this);

    public TileFluxController() {
        super(RegistryBlocks.FLUX_CONTROLLER_TILE, "Flux Controller", FluxConfig.defaultLimit);
    }

    @Override
    public FluxDeviceType getDeviceType() {
        return FluxDeviceType.CONTROLLER;
    }

    @Nonnull
    @Override
    public ITransferHandler getTransferHandler() {
        return handler;
    }

    @Nonnull
    @Override
    public ItemStack getDisplayStack() {
        return FluxGuiStack.FLUX_CONTROLLER;
    }
}
