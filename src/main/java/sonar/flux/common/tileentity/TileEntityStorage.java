package sonar.flux.common.tileentity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncEnergyStorage;
import sonar.core.utils.IGuiTile;
import sonar.flux.FluxConfig;
import sonar.flux.api.network.FluxCache;
import sonar.flux.api.tiles.IFluxStorage;
import sonar.flux.client.GuiFlux;
import sonar.flux.common.containers.ContainerFlux;

public class TileEntityStorage extends TileEntityFlux implements IGuiTile, IFluxStorage {

	public final SyncEnergyStorage storage;
	public int maxTransfer;

	public static class Basic extends TileEntityStorage {
		public Basic() {
			super(FluxConfig.basicCapacity, FluxConfig.basicTransfer);
			customName.setDefault("Basic Storage");
		}
	}

	public static class Herculean extends TileEntityStorage {
		public Herculean() {
			super(FluxConfig.herculeanCapacity, FluxConfig.herculeanTransfer);
			customName.setDefault("Herculean Storage");
		}
	}

	public static class Gargantuan extends TileEntityStorage {
		public Gargantuan() {
			super(FluxConfig.gargantuanCapacity, FluxConfig.gargantuanTransfer);
			customName.setDefault("Gargantuan Storage");
		}
	}

	public TileEntityStorage(int capacity, int transfer) {
		super(ConnectionType.STORAGE);
		maxTransfer = transfer;
		storage = new SyncEnergyStorage(capacity, maxTransfer);
		syncList.addPart(storage);
	}

	public long lastStorageUpdate;
	public boolean updateStorage;
	public int targetEnergy;

	public void update() {
		super.update();
		if (isServer()) {
			if (updateStorage && lastStorageUpdate == 0) {
				lastStorageUpdate = getWorld().getWorldTime(); //stops it jumping on first receive
			} else if (updateStorage && getWorld().getWorldTime() > lastStorageUpdate + 20) {
				SonarCore.sendPacketAround(this, 128, 10);
				lastStorageUpdate = getWorld().getWorldTime();
				updateStorage = false;
			}
		} else if (updateStorage && storage.getEnergyStored() != targetEnergy) {
			int inc = storage.getMaxEnergyStored() / 50;
			int dif = Math.abs(storage.getEnergyStored() - targetEnergy);
			if (dif < inc * 2) {
				inc = inc / 4; // slows when it gets closer
			}
			if (storage.getEnergyStored() < targetEnergy) {
				storage.setEnergyStored(Math.min(storage.getEnergyStored() + inc, targetEnergy));
			} else {
				storage.setEnergyStored(Math.max(storage.getEnergyStored() - inc, targetEnergy));
			}
		} else {
			updateStorage = false;
		}
	}

	@Override
	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
		if (getWorld() != null && this.isServer()) {
			if (part == storage) {
				network.markTypeDirty(FluxCache.storage);
				updateStorage = true;
			} else if (part == colour) {
				SonarCore.sendPacketAround(this, 128, 11);
			}
		}
	}

	@Override
	public long getMaxEnergyStored() {
		return storage.getFullCapacity();
	}

	@Override
	public long getEnergyStored() {
		return storage.getEnergyLevel();
	}

	public boolean canTransfer() {
		return true;
	}

	@Override
	public Object getGuiContainer(EntityPlayer player) {
		return new ContainerFlux(player, this, false);
	}

	@Override
	public Object getGuiScreen(EntityPlayer player) {
		return new GuiFlux((Container) getGuiContainer(player), this, player);
	}

	@Override
	public long getTransferLimit() {
		return storage.getMaxExtract();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return storage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return storage.getMaxEnergyStored();
	}

	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		if (type.isType(SyncType.DROP))
			this.storage.setEnergyStored(nbt.getInteger("energy"));
	}

	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		if (type.isType(SyncType.DROP)) {
			nbt.setInteger("energy", this.storage.getEnergyStored());
		}
		return nbt;
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		switch (id) {
		case 10:
			buf.writeInt(storage.getEnergyStored());
			break;
		case 11:
			colour.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		switch (id) {
		case 10:
			targetEnergy = buf.readInt();
			updateStorage = true;
			break;
		case 11:
			colour.readFromBuf(buf);
			break;
		}
	}
}