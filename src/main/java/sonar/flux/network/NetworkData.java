package sonar.flux.network;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.CustomColour;
import sonar.flux.FluxEvents;
import sonar.flux.FluxNetworks;
import sonar.flux.api.AccessType;
import sonar.flux.api.network.IFluxNetwork;
import sonar.flux.connection.BasicFluxNetwork;

public class NetworkData extends WorldSavedData {

	public static final String IDENTIFIER = "sonar.flux.networks.configurations";
	public static String TAG_LIST = "networks";
	public static String UNIQUE_ID = "uniqueID";
	public static String NETWORK_ID = "id";
	public static String COLOUR = "colour";
	public static String OWNER_UUID = "ownerUUID";
	public static String CACHE_PLAYER = "cachePName";
	public static String NETWORK_NAME = "name";
	public static String ACCESS = "access";
	public static String PLAYER_LIST = "playerList";

    public NetworkData(String name) {
        super(name);
    }

	public NetworkData() {
		this(IDENTIFIER);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	    FluxNetworkCache cache = FluxNetworks.getServerCache();
		cache.uniqueID = nbt.getInteger(UNIQUE_ID);
		if (nbt.hasKey(TAG_LIST)) {
			NBTTagList list = nbt.getTagList(TAG_LIST, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				int networkID = tag.getInteger(NETWORK_ID);
				String networkName = tag.getString(NETWORK_NAME);
				UUID ownerUUID = tag.getUniqueId(OWNER_UUID);
				String cachedPlayer = tag.getString(CACHE_PLAYER);
				CustomColour colour = NBTHelper.instanceNBTSyncable(CustomColour.class, tag.getCompoundTag(COLOUR));
				AccessType type = AccessType.valueOf(tag.getString(ACCESS));
				BasicFluxNetwork network = new BasicFluxNetwork(networkID, ownerUUID, networkName, colour, type);
				network.getPlayers().readData(tag.getCompoundTag(PLAYER_LIST), SyncType.SAVE);
				cache.addNetwork(network);
				FluxEvents.logLoadedNetwork(network);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        FluxNetworkCache cache = FluxNetworks.getServerCache();
		nbt.setInteger(UNIQUE_ID, cache.uniqueID);
		if(cache.getAllNetworks().size() > 0)
		{
    		NBTTagList list = new NBTTagList();
    		for (IFluxNetwork network : FluxNetworks.getServerCache().getAllNetworks()) {
    			NBTTagCompound tag = new NBTTagCompound();
    			tag.setInteger(NETWORK_ID, network.getNetworkID());
    			tag.setUniqueId(OWNER_UUID, network.getOwnerUUID());
    			tag.setString(CACHE_PLAYER, network.getCachedPlayerName());
    			tag.setString(NETWORK_NAME, network.getNetworkName());
    			tag.setTag(COLOUR, network.getNetworkColour().writeData(new NBTTagCompound(), SyncType.SAVE));
    			tag.setString(ACCESS, network.getAccessType().name());
    			tag.setTag(PLAYER_LIST, network.getPlayers().writeData(new NBTTagCompound(), SyncType.SAVE));
    			list.appendTag(tag);
    		}
    		nbt.setTag(TAG_LIST, list);
    		FluxNetworks.logger.debug("ALL " + list.tagCount() + " Networks were saved successfully");
		}
		return nbt;
	}

	public boolean isDirty() {
		return true;
	}
}
