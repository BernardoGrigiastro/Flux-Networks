package sonar.flux.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import cofh.api.energy.IEnergyConnection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.energy.EnergyType;
import sonar.core.api.energy.ISonarEnergyContainerHandler;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.utils.ActionType;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.flux.FluxConfig;
import sonar.flux.FluxNetworks;
import sonar.flux.api.FluxListener;
import sonar.flux.api.network.FluxPlayer;
import sonar.flux.api.network.IFluxNetwork;
import sonar.flux.api.tiles.IFlux;
import sonar.flux.api.tiles.IFluxController;
import sonar.flux.api.tiles.IFluxController.PriorityMode;
import sonar.flux.api.tiles.IFluxController.TransmitterMode;
import sonar.flux.api.tiles.IFluxListenable;
import sonar.flux.common.tileentity.TileEntityStorage;
import sonar.flux.network.FluxNetworkCache;
import sonar.flux.network.PacketFluxConnectionsList;
import sonar.flux.network.PacketFluxNetworkList;

public class FluxHelper {

	public static void addConnection(IFluxListenable flux) {
		IFluxNetwork network = FluxNetworks.getServerCache().getNetwork(flux.getNetworkID());
		if (!network.isFakeNetwork()) {
			network.addConnection(flux);
		}
	}

	public static void removeConnection(IFluxListenable flux) {
		IFluxNetwork network = FluxNetworks.getServerCache().getNetwork(flux.getNetworkID());
		if (!network.isFakeNetwork()) {
			network.removeConnection(flux);
		}
	}

	public static UUID getOwnerUUID(EntityPlayer player) {
		return player.getGameProfile().getId();
	}

	public static void sortConnections(List<IFlux> flux, PriorityMode mode) {
		switch (mode) {
		case DEFAULT:
			break;
		case LARGEST:
			Collections.sort(flux, new Comparator<IFlux>() {
				public int compare(IFlux o1, IFlux o2) {
					return o2.getCurrentPriority() - o1.getCurrentPriority();
				}
			});
			break;
		case SMALLEST:
			Collections.sort(flux, new Comparator<IFlux>() {
				public int compare(IFlux o1, IFlux o2) {
					return o1.getCurrentPriority() - o2.getCurrentPriority();
				}
			});
			break;
		default:
			break;
		}
	}

	public static void sendPacket(IFluxNetwork network, ListenerTally<PlayerListener> tally) {
		for (int i = 0; i < tally.tallies.length; i++) {
			if (tally.tallies[i] > 0) {
				FluxListener type = FluxListener.values()[i];
				switch (type) {
				case CONNECTIONS:
					network.buildFluxConnections();
					FluxNetworks.network.sendTo(new PacketFluxConnectionsList(network.getClientFluxConnection(), network.getNetworkID()), tally.listener.player);
					break;
				case FULL_NETWORK:
					ArrayList<IFluxNetwork> toSend = FluxNetworkCache.instance().getAllowedNetworks(tally.listener.player, true);
					FluxNetworks.network.sendTo(new PacketFluxNetworkList(toSend), tally.listener.player);
					tally.removeTallies(1, type);
					tally.addTallies(1, FluxListener.SYNC_NETWORK);
					break;
				case STATISTICS:
					// TODO keep STATISTICS up-to-date
					break;
				case SYNC_NETWORK:
					// TODO sync parts have changed - update
					break;
				default:
					break;
				}
			}
		}
	}

	public static long pullEnergy(IFlux from, long maxTransferRF, ActionType actionType) {
		long extracted = 0;
		maxTransferRF = Math.min(maxTransferRF, from.getCurrentTransferLimit());
		if (from != null && maxTransferRF != 0) {
			switch (from.getConnectionType()) {
			case PLUG:
				TileEntity[] tiles = from.cachedTiles();
				for (int i = 0; i < 6; i++) {
					TileEntity tile = tiles[i];
					if (tile != null) {
						long remove = SonarAPI.getEnergyHelper().extractEnergy(tile, Math.min(maxTransferRF - extracted, from.getCurrentTransferLimit()), EnumFacing.values()[i].getOpposite(), actionType);
						if (!actionType.shouldSimulate())
							from.onEnergyRemoved(remove);
						extracted += remove;
					}
				}
				break;
			case STORAGE:
				TileEntityStorage tile = (TileEntityStorage) from;
				int remove = tile.storage.extractEnergy((int) Math.min(maxTransferRF - extracted, Integer.MAX_VALUE), actionType.shouldSimulate());
				if (!actionType.shouldSimulate())
					from.onEnergyRemoved(remove);
				extracted += remove;
				break;
			default:
				break;
			}
		}
		return extracted;
	}

	public static long pushEnergy(IFlux to, long maxTransferRF, ActionType actionType) {
		long received = 0;
		maxTransferRF = Math.min(maxTransferRF, to.getCurrentTransferLimit());
		if (to != null && maxTransferRF != 0 && to.canTransfer()) {
			// BlockCoords coords = to.getCoords();
			switch (to.getConnectionType()) {
			case POINT:
				TileEntity[] tiles = to.cachedTiles();
				for (int i = 0; i < 6; i++) {
					TileEntity tile = tiles[i];
					if (tile != null) {
						long added = SonarAPI.getEnergyHelper().receiveEnergy(tile, Math.min(maxTransferRF - received, to.getCurrentTransferLimit()), EnumFacing.values()[i].getOpposite(), actionType);
						if (!actionType.shouldSimulate())
							to.onEnergyAdded(added);
						received += added;
					}
				}
				break;
			case STORAGE:
				TileEntityStorage tile = (TileEntityStorage) to;
				int added = tile.storage.receiveEnergy((int) Math.min(maxTransferRF - received, Integer.MAX_VALUE), actionType.shouldSimulate());
				if (!actionType.shouldSimulate())
					to.onEnergyAdded(added);
				received += added;
				break;
			case CONTROLLER:
				IFluxController controller = (IFluxController) to;
				if (controller.getTransmitterMode() == TransmitterMode.OFF) {
					break;
				}
				ArrayList<FluxPlayer> playerNames = (ArrayList<FluxPlayer>) controller.getNetwork().getPlayers().clone();
				ArrayList<EntityPlayer> players = Lists.newArrayList();
				for (FluxPlayer player : playerNames) {
					Entity entity = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(player.id);
					if (entity != null && entity instanceof EntityPlayer) {
						players.add((EntityPlayer) entity);
					}
				}
				for (EntityPlayer player : players) {
					long receive = 0;
					switch (controller.getTransmitterMode()) {
					case HELD_ITEM:
						ItemStack stack = player.getHeldItemMainhand();
						if (stack != null && FluxHelper.canTransferEnergy(stack) != null) {
							receive = SonarAPI.getEnergyHelper().receiveEnergy(stack, maxTransferRF - received, actionType);
							received += receive;
							if (!actionType.shouldSimulate())
								to.onEnergyRemoved(receive);
							if (maxTransferRF - received <= 0) {
								break;
							}
						}
						break;
					case HOTBAR:
					case ON:
						IInventory inv = player.inventory;
						for (int i = 0; i < ((controller.getTransmitterMode() == TransmitterMode.ON) ? inv.getSizeInventory() : 9); i++) {
							ItemStack itemStack = inv.getStackInSlot(i);
							if (itemStack != null && FluxHelper.canTransferEnergy(itemStack) != null) {
								receive = SonarAPI.getEnergyHelper().receiveEnergy(itemStack, maxTransferRF - received, actionType);
								received += receive;
								if (!actionType.shouldSimulate())
									to.onEnergyRemoved(receive);
								if (maxTransferRF - received <= 0) {
									break;
								}
							}
						}
						break;
					default:
						break;
					}
				}

				break;
			default:
				break;
			}
		}
		return received;
	}

	public static List<ISonarEnergyHandler> getEnergyHandlers() {
		ArrayList<ISonarEnergyHandler> handlers = Lists.newArrayList();
		for (ISonarEnergyHandler handler : SonarCore.energyHandlers) {
			if (FluxConfig.transfers.get(handler.getProvidedType()).a) {
				handlers.add(handler);
			}
		}
		return handlers;
	}

	public static List<ISonarEnergyContainerHandler> getEnergyContainerHandlers() {
		ArrayList<ISonarEnergyContainerHandler> handlers = Lists.newArrayList();
		for (ISonarEnergyContainerHandler handler : SonarCore.energyContainerHandlers) {
			if (FluxConfig.transfers.get(handler.getProvidedType()).b) {
				handlers.add(handler);
			}
		}
		return handlers;
	}

	public static boolean canConnect(TileEntity tile, EnumFacing dir) {
		if (tile != null && !(tile instanceof IFlux)) {
			if (canTransferEnergy(tile, dir) != null) {
				return true;
			}
			return tile instanceof IEnergyConnection && FluxConfig.transfers.get(EnergyType.RF).a;
		}
		return false;
	}

	public static ISonarEnergyHandler canTransferEnergy(TileEntity tile, EnumFacing dir) {
		List<ISonarEnergyHandler> handlers = FluxNetworks.energyHandlers;
		for (ISonarEnergyHandler handler : handlers) {
			if (handler.canProvideEnergy(tile, dir)) {
				return handler;
			}
		}
		return null;
	}

	public static ISonarEnergyContainerHandler canTransferEnergy(ItemStack stack) {
		List<ISonarEnergyContainerHandler> handlers = FluxNetworks.energyContainerHandlers;
		for (ISonarEnergyContainerHandler handler : handlers) {
			if (handler.canHandleItem(stack)) {
				return handler;
			}
		}
		return null;
	}
}