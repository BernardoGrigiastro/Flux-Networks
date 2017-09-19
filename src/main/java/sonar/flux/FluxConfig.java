package sonar.flux;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import sonar.core.SonarCore;
import sonar.core.api.energy.EnergyType;
import sonar.core.utils.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FluxConfig extends FluxNetworks {

	public static long defaultLimit;
	public static boolean banHyper, banGod;
	public static int basicCapacity, herculeanCapacity, gargantuanCapacity;
	public static int basicTransfer, herculeanTransfer, gargantuanTransfer;
	public static int hyper = 4, god = 10;

    public static Map<EnergyType, Pair<Boolean, Boolean>> transfers = new HashMap<>();
	public static Configuration config;

	public static void startLoading() {
		config = new Configuration(new File("config/flux_networks.cfg"));
		config.load();
		defaultLimit = (long) config.getFloat("Default Transfer Limit", "energy", 256000, 0, Long.MAX_VALUE, "the default transfer limit of a flux connection");

		basicCapacity = config.getInt("Basic Storage Capacity", "energy", 256000, 0, Integer.MAX_VALUE, "");
		basicTransfer = config.getInt("Basic Storage Transfer", "energy", 6400, 0, Integer.MAX_VALUE, "");
		herculeanCapacity = config.getInt("Herculean Storage Capacity", "energy", 12800000, 0, Integer.MAX_VALUE, "");
		herculeanTransfer = config.getInt("Herculean Storage Transfer", "energy", 12800, 0, Integer.MAX_VALUE, "");
		gargantuanCapacity = config.getInt("Gargantuan Storage Capacity", "energy", 128000000, 0, Integer.MAX_VALUE, "");
		gargantuanTransfer = config.getInt("Gargantuan Storage Transfer", "energy", 256000, 0, Integer.MAX_VALUE, "");

		hyper = config.getInt("Hyper Mode Multiplier", "energy", 4, 1, 16, "the multiplier for hyper mode - for how much energy is transfer compared to normal rate");
		god = config.getInt("God Mode Multiplier", "energy", 10, 1, 16, "the multiplier god mod - for how much energy is transfer compared to normal rate");

		banHyper = config.getBoolean("Ban Hyper Mode", "settings", false, "prevents the use of Hyper Mode");
		banGod = config.getBoolean("Ban God Mode", "settings", false, "prevents the use of God Mode");
		config.save();
	}

	public static void finishLoading() {
		for (EnergyType type : SonarCore.energyTypes.getObjects()) {
			boolean item, block;
			block = getEnergyTypeBoolean(type.getName() + " Transfer: Blocks", "energy types", true);
			item = getEnergyTypeBoolean(type.getName() + " Transfer: Items", "energy types", true);
			transfers.put(type, new Pair(item, block));
		}
		config.save();
	}

	public static boolean getEnergyTypeBoolean(String name, String category, boolean defaultValue) {
		Property prop = config.get(category, name, defaultValue);
		prop.setLanguageKey(name);
		return prop.getBoolean(defaultValue);
	}
}
