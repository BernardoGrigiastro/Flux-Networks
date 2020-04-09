package sonar.fluxnetworks.common.connection.handler;

import com.google.common.collect.Lists;
import net.minecraft.util.Direction;
import sonar.fluxnetworks.api.network.IFluxTransfer;
import sonar.fluxnetworks.api.tiles.IFluxEnergy;
import sonar.fluxnetworks.common.connection.FluxTransferHandler;

import java.util.List;

public class SingleTransferHandler extends FluxTransferHandler<IFluxEnergy> {

    public final IFluxTransfer transfer;

    public SingleTransferHandler(IFluxEnergy tile, IFluxTransfer transfer) {
        super(tile);
        this.transfer = transfer;
    }

    @Override
    public void onLastEndTick() {
        super.onLastEndTick();
        transfer.onServerStartTick();
    }

    /**
     * Discharge Flux Storage
     * @param maxAmount
     * @return
     */
    @Override
    public long addToNetwork(long maxAmount) {
        if(!fluxConnector.isActive()) {
            return 0;
        }
        //long canAdd = Math.min(getConnectorLimit(), maxAmount);
        long add = transfer.addToNetwork(maxAmount, false);
        added += add;
        return add;
    }

    /**
     * Charge Flux Storage or Wireless
     * @param maxAmount
     * @param simulate
     * @return
     */
    @Override
    public long removeFromNetwork(long maxAmount, boolean simulate) {
        if(!fluxConnector.isActive()) {
            return 0;
        }
        long canRemove = Math.min(getConnectorLimit(), maxAmount);
        long remove = transfer.removeFromNetwork(canRemove, simulate);
        if(simulate) {
            request = remove;
        } else {
            request -= remove;
            removed += remove;
        }
        return remove;
    }

    @Override
    public void updateTransfers(Direction... faces) {

    }

    @Override
    public List<IFluxTransfer> getTransfers() {
        return Lists.newArrayList(transfer);
    }

    @Override
    public long getBuffer() {
        return Math.min(fluxConnector.getEnergy(), fluxConnector.getCurrentLimit());
    }

    @Override
    public long getEnergyStored() {
        return fluxConnector.getEnergy();
    }
}
