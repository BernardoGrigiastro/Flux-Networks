package sonar.flux.network;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import sonar.flux.api.IFluxNetwork;
import sonar.flux.api.IFluxNetworkCache;
import sonar.flux.connection.EmptyFluxNetwork;

public class ClientNetworkCache implements IFluxNetworkCache {

	public ConcurrentHashMap<UUID, ArrayList<IFluxNetwork>> networks = new ConcurrentHashMap<UUID, ArrayList<IFluxNetwork>>();

	@Override
	public IFluxNetwork getNetwork(int iD) {
		for (Entry<UUID, ArrayList<IFluxNetwork>> entry : networks.entrySet()) {
			for (IFluxNetwork common : entry.getValue()) {
				if (!common.isFakeNetwork() && iD == common.getNetworkID()) {
					return common;
				}
			}
		}
		return EmptyFluxNetwork.INSTANCE;
	}

	@Override
	public ArrayList<IFluxNetwork> getAllowedNetworks(EntityPlayer player, boolean admin) {
		ArrayList<IFluxNetwork> available = Lists.newArrayList();
		for (IFluxNetwork network : getAllNetworks()) {
			if (network.getPlayerAccess(player).canConnect()) {
				available.add(network);
			}
		}
		return available;
	}

	@Override
	public ArrayList<IFluxNetwork> getAllNetworks() {
		ArrayList<IFluxNetwork> available = Lists.newArrayList();
		for (Entry<UUID, ArrayList<IFluxNetwork>> entry : networks.entrySet()) {
			available.addAll(entry.getValue());
		}
		return available;
	}
	
}
