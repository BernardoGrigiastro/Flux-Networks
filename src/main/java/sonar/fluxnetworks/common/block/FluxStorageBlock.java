package sonar.fluxnetworks.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.misc.EnergyType;
import sonar.fluxnetworks.api.text.FluxTranslate;
import sonar.fluxnetworks.common.tileentity.TileFluxStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class FluxStorageBlock extends FluxDeviceBlock {

    public FluxStorageBlock(Properties props) {
        super(props);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add(FluxTranslate.FLUX_STORAGE_TOOLTIP.getTextComponent());
        tooltip.add(FluxTranslate.FLUX_STORAGE_TOOLTIP_2.getTextComponent(EnergyType.storage(getEnergyCapacity())));
    }

    public abstract long getEnergyCapacity();

    public static class Basic extends FluxStorageBlock {

        public Basic(Properties props) {
            super(props);
        }

        @Override
        public long getEnergyCapacity() {
            return FluxConfig.basicCapacity;
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new TileFluxStorage.Basic();
        }
    }

    public static class Herculean extends FluxStorageBlock {

        public Herculean(Properties props) {
            super(props);
        }

        @Override
        public long getEnergyCapacity() {
            return FluxConfig.herculeanCapacity;
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new TileFluxStorage.Herculean();
        }
    }

    public static class Gargantuan extends FluxStorageBlock {

        public Gargantuan(Properties props) {
            super(props);
        }

        @Override
        public long getEnergyCapacity() {
            return FluxConfig.gargantuanCapacity;
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return new TileFluxStorage.Gargantuan();
        }
    }
}
