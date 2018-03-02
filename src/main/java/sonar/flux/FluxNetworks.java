package sonar.flux;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import sonar.core.SonarRegister;
import sonar.core.api.energy.ISonarEnergyContainerHandler;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.flux.api.FluxAPI;
import sonar.flux.common.block.FluxController;
import sonar.flux.common.block.FluxPlug;
import sonar.flux.common.block.FluxPoint;
import sonar.flux.common.block.FluxStorage;
import sonar.flux.common.entity.EntityFireItem;
import sonar.flux.common.item.AdminConfigurator;
import sonar.flux.common.item.FluxConfigurator;
import sonar.flux.common.item.FluxItem;
import sonar.flux.common.tileentity.TileEntityController;
import sonar.flux.common.tileentity.TileEntityPlug;
import sonar.flux.common.tileentity.TileEntityPoint;
import sonar.flux.common.tileentity.TileEntityStorage;
import sonar.flux.connection.FluxHelper;
import sonar.flux.network.ClientNetworkCache;
import sonar.flux.network.FluxCommon;
import sonar.flux.network.FluxNetworkCache;

@Mod(modid = FluxNetworks.modid, name = FluxNetworks.name, acceptedMinecraftVersions = FluxNetworks.mc_versions, version = FluxNetworks.version, dependencies = "required-after:sonarcore@[" + FluxNetworks.SONAR_VERSION + ",);")
public class FluxNetworks {

	public static final String name = "FluxNetworks";
	public static final String modid = "fluxnetworks";
	public static final String version = "1.3.9";
	public static final String mc_versions = "[1.9.4, 1.10.2]";
	public static final String SONAR_VERSION = "3.3.1";

	public static final int saveDimension = 0;

	@SidedProxy(clientSide = "sonar.flux.network.FluxClient", serverSide = "sonar.flux.network.FluxCommon")
	public static FluxCommon proxy;

	@Instance(modid)
	public static FluxNetworks instance;

	public FluxNetworkCache serverCache = new FluxNetworkCache();
	public ClientNetworkCache clientCache = new ClientNetworkCache();
	public static List<ISonarEnergyHandler> energyHandlers;
	public static List<ISonarEnergyContainerHandler> energyContainerHandlers;

	public static SimpleNetworkWrapper network;
	public static Logger logger = (Logger) LogManager.getLogger(modid);

	public static Item flux, fluxCore, fluxConfigurator, adminConfigurator;
	public static Block fluxBlock, fluxPlug, fluxPoint, fluxCable, fluxStorage, largeFluxStorage, massiveFluxStorage, fluxController;

	public static CreativeTabs tab = new CreativeTabs("Flux Networks") {
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(fluxPlug);
		}
	};
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger.info("Initialising API");
		FluxAPI.init();

		logger.info("Loading Config");
		FluxConfig.startLoading();

		logger.info("Loading Network");
		network = NetworkRegistry.INSTANCE.newSimpleChannel("Flux-Networks");

		logger.info("Loading Blocks/Items");
		fluxBlock = SonarRegister.addBlock(modid, tab, "FluxBlock", new Block(Material.ROCK));

		flux = SonarRegister.addItem(modid, tab, "Flux", new FluxItem());
		fluxCore = SonarRegister.addItem(modid, tab, "FluxCore", new Item());
		fluxConfigurator = SonarRegister.addItem(modid, tab, "FluxConfigurator", new FluxConfigurator());
		adminConfigurator = SonarRegister.addItem(modid, tab, "AdminConfigurator", new AdminConfigurator());

		// fluxCable = registerBlock("FluxCable", new
		// FluxCable().setHardness(0.4F).setResistance(20.0F));
		// GameRegistry.registerTileEntity(TileEntityCable.class, "FluxCable");

		fluxPlug = SonarRegister.addBlock(modid, tab, "FluxPlug", new FluxPlug().setHardness(0.4F).setResistance(20.0F));
		GameRegistry.registerTileEntity(TileEntityPlug.class, "FluxPlug");

		fluxPoint = SonarRegister.addBlock(modid, tab, "FluxPoint", new FluxPoint().setHardness(0.2F).setResistance(20.0F));
		GameRegistry.registerTileEntity(TileEntityPoint.class, "FluxPoint");

		fluxController = SonarRegister.addBlock(modid, tab, "FluxController", new FluxController().setHardness(0.6F).setResistance(20.0F));
		GameRegistry.registerTileEntity(TileEntityController.class, "FluxController");

		fluxStorage = SonarRegister.addBlock(modid, tab, "FluxStorage", new FluxStorage().setHardness(0.6F).setResistance(20.0F));
		GameRegistry.registerTileEntity(TileEntityStorage.Basic.class, "FluxStorage");

		largeFluxStorage = SonarRegister.addBlock(modid, tab, "HerculeanFluxStorage", new FluxStorage.Herculean().setHardness(0.6F).setResistance(20.0F));
		GameRegistry.registerTileEntity(TileEntityStorage.Herculean.class, "HerculeanFluxStorage");

		massiveFluxStorage = SonarRegister.addBlock(modid, tab, "GargantuanFluxStorage", new FluxStorage.Gargantuan().setHardness(0.6F).setResistance(20.0F));
		GameRegistry.registerTileEntity(TileEntityStorage.Gargantuan.class, "GargantuanFluxStorage");

		logger.info("Loading Entities");
		EntityRegistry.registerModEntity(EntityFireItem.class, "Flux", 0, instance, 64, 10, true);

		logger.info("Loading Recipes");
		FluxCrafting.addRecipes();

		logger.info("Loading Packets");
		FluxCommon.registerPackets();

		logger.info("Loading Renderers");
		proxy.registerRenderThings();

		logger.info("Finished Pre-Initialization");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.info("Loading Handlers");
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new FluxCommon());
		logger.info("Loaded Handlers");

		logger.info("Loading Handlers");
		MinecraftForge.EVENT_BUS.register(new FluxEvents());
		logger.info("Loaded Events");

		logger.info("Finished Initialization");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		FluxConfig.finishLoading();
		FluxNetworks.energyHandlers = FluxHelper.getEnergyHandlers();
		FluxNetworks.energyContainerHandlers = FluxHelper.getEnergyContainerHandlers();
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppedEvent event) {
		serverCache.clearNetworks();
		clientCache.clearNetworks();
		logger.info("Cleared Network Caches");
	}

	public static ClientNetworkCache getClientCache() {
		return FluxNetworks.instance.clientCache;
	}

	public static FluxNetworkCache getServerCache() {
		return FluxNetworks.instance.serverCache;
	}
}
