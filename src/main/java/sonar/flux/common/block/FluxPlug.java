package sonar.flux.common.block;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import sonar.flux.common.tileentity.TileEntityPlug;

public class FluxPlug extends FluxSidedConnection {

	public FluxPlug() {
		super();
		this.setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityPlug();
	}

	@Override
    public void standardInfo(ItemStack stack, EntityPlayer player, List<String> list) {
		list.add("Sends Energy");
	}

    @Override
    public void standardInfo(ItemStack stack, World world, List<String> list) {
        list.add("Sends Energy");
    }
}
