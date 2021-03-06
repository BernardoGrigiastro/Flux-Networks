package sonar.fluxnetworks.common.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import sonar.fluxnetworks.api.misc.FeedbackInfo;
import sonar.fluxnetworks.api.misc.FluxConstants;
import sonar.fluxnetworks.api.misc.IMessage;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.common.registry.RegistryItems;
import sonar.fluxnetworks.common.storage.FluxNetworkData;

import javax.annotation.Nonnull;

public class CConfiguratorConnectMessage implements IMessage {

    private int id;
    private String password;

    public CConfiguratorConnectMessage() {
    }

    public CConfiguratorConnectMessage(int id, String password) {
        this.id = id;
        this.password = password;
    }

    @Override
    public void encode(@Nonnull PacketBuffer buffer) {
        buffer.writeVarInt(id);
        buffer.writeString(password, 256);
    }

    @Override
    public void handle(@Nonnull PacketBuffer buffer, @Nonnull NetworkEvent.Context context) {
        PlayerEntity player = NetworkHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        int networkID = buffer.readVarInt();
        IFluxNetwork network = FluxNetworkData.getNetwork(networkID);
        if (network.isValid()) {
            if (CSelectNetworkMessage.checkAccess(buffer, context, player, network))
                return;
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() == RegistryItems.FLUX_CONFIGURATOR) {
                CompoundNBT configs = stack.getOrCreateChildTag(FluxConstants.TAG_FLUX_CONFIG);
                configs.putInt(FluxConstants.NETWORK_ID, networkID);
            }
            NetworkHandler.INSTANCE.reply(new SFeedbackMessage(FeedbackInfo.SUCCESS), context);
        }
    }
}
