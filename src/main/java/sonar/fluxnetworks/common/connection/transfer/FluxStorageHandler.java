package sonar.fluxnetworks.common.connection.transfer;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import sonar.fluxnetworks.api.misc.FluxConstants;
import sonar.fluxnetworks.common.tileentity.TileFluxStorage;

import javax.annotation.Nonnull;

public class FluxStorageHandler extends BasicTransferHandler<TileFluxStorage> {

    public FluxStorageHandler(TileFluxStorage fluxStorage) {
        super(fluxStorage);
    }

    private long added;
    private long removed;

    @Override
    public void onCycleStart() {

    }

    @Override
    public void onCycleEnd() {
        change = added - removed;
        added = 0;
        removed = 0;
    }

    @Override
    public void addToBuffer(long energy) {
        if (energy <= 0) {
            return;
        }
        buffer += energy;
        added += energy;
        device.markServerEnergyChanged();
    }

    @Override
    public long removeFromBuffer(long energy) {
        long a = Math.min(Math.min(energy, buffer), device.getLogicLimit() - removed);
        if (a <= 0) {
            return 0;
        }
        buffer -= a;
        removed += a;
        device.markServerEnergyChanged();
        return a;
    }

    @Override
    public long getRequest() {
        return Math.max(0, Math.min(device.getMaxTransferLimit() - buffer, device.getLogicLimit() - added));
    }

    @Override
    public void writeCustomNBT(CompoundNBT tag, int type) {
        if (type == FluxConstants.TYPE_TILE_UPDATE || type == FluxConstants.TYPE_CONNECTION_UPDATE) {
            super.writeCustomNBT(tag, type); // read by PhantomFluxDevice
        }
        if (type == FluxConstants.TYPE_SAVE_ALL || type == FluxConstants.TYPE_TILE_DROP) {
            tag.putLong(FluxConstants.ENERGY, buffer);
        }
    }

    @Override
    public void readCustomNBT(CompoundNBT tag, int type) {
        if (type == FluxConstants.TYPE_TILE_UPDATE) {
            super.readCustomNBT(tag, type);
        }
        if (type == FluxConstants.TYPE_SAVE_ALL || type == FluxConstants.TYPE_TILE_DROP) {
            buffer = tag.getLong(FluxConstants.ENERGY);
        }
    }

    @Override
    public void writePacket(@Nonnull PacketBuffer buffer, byte id) {
        if (id == FluxConstants.S2C_STORAGE_ENERGY) {
            buffer.writeLong(this.buffer);
        } else {
            super.writePacket(buffer, id);
        }
    }

    @Override
    public void readPacket(@Nonnull PacketBuffer buffer, byte id) {
        if (id == FluxConstants.S2C_STORAGE_ENERGY) {
            this.buffer = buffer.readLong();
        } else {
            super.readPacket(buffer, id);
        }
    }

    @Override
    public void updateTransfers(@Nonnull Direction... faces) {

    }
}
