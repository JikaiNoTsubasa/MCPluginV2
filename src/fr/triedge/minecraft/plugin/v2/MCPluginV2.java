package fr.triedge.minecraft.plugin.v2;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.triedge.minecraft.plugin.v2.custom.Custom;
import fr.triedge.minecraft.plugin.v2.custom.CustomManager;
import fr.triedge.minecraft.plugin.v2.detector.Detector;
import fr.triedge.minecraft.plugin.v2.exceptions.MCLoadingException;
import fr.triedge.minecraft.plugin.v2.magic.MagicManager;
import fr.triedge.minecraft.plugin.v2.task.SaveTask;
import fr.triedge.minecraft.plugin.v2.warp.WarpManager;

/**
 * Plugin features:
 * [x] v1.0 Build teleports [TEST OK]
 * [x] v1.1 New gold/diamond pickaxe with more durability [TEST OK]
 * [x] v1.2 Ultimate stick/bottle [TEST OK]
 * [x] v1.2 Display title at login [TEST OK]
 * [x] v1.2 Snow Wand [TEST OK]
 * [x] v1.2 Get more ore with custom pickaxe [TEST OK]
 * [X] v1.3 Update recipe to allow all LOGs for custom pickaxes
 * [x] v1.3 Creepers drop 1 Emerald [TEST OK]
 * [x] v1.3 Store more items [TO TEST]
 * [x] v1.3 Grenade -> no drop [TO TEST]
 * [x] v1.3 Added popo to prevent inventory drop when death occurs [TO TEST]
 * [x] v1.3 Fire Wand -> no xp [TO TEST]
 * [x] v1.3 Command save inventories / when connect check if inv already loaded / save inv when delogg
 * [x] v1.4 Custom Axe and Shovel GOLD [TO TEST]
 * [ ] v1.4 Bigger jump
 * [x] v1.4 Hidden TP with h_ [TO TEST]
 * [x] v1.4 Spawn mob Pack [TO TEST]
 * [x] v1.16 Detector in different directions
 * [x] v1.16 Added warp command
 * [ ] Custom mob drop emerald (BOSS)
 * [ ] Custom mob spawn (obscurity)
 * [ ] Popo to teleport from anywhere - custom craft
 * [ ] Popo REZ
 * [ ] Popo speed with scheduler
 * [ ] Spawn emerald chests
 *
 * [ ] Add glow effects to detector blocks
 *
 * Bug:
 * [x] v1.2 NullPointer - When break block with no item in hand
 * [x] v1.2 NullPointer - Magic config not loading
 * [x] v1.3 Double Snow Ball - Firing 2 snow balls instead of 1
 * [x] v1.3 Solved a bug about xp given to player when level up Magic
 * [x] v1.3 ULT STICK breaks block but no loot from block
 * [x] v1.3 ULT WATER fill up water in bottle and changes it's name with right click
 * [x] v1.3 Teleport could happen event if destination is not on diamond block
 * [x] v1.16 When several stacks of nuke in inventory, it decreases all stacks when using
 * [x] 20200706.0 v1.16.1 When nuke is sent and pickedup, stack of 64 is gained
 * [x] 20200706.1 v1.16.1 When nuke explose, now set in fire and break blocs
 * [x] 20200706.3 v1.16.1 Deported teleport code to dedicated listener
 * [x] 20200706.4 v1.16.2 Detector in different directions and added new blocks
 * [x] 20200706.5 Fixed version numbering
 *
 * Client:
 * [ ] Create laboratory
 * [ ] Terrasse double arches
 * [ ] Bouton fontaine
 * [x] Appart in mountain
 * [ ] Underwater house
 *
 * @author steph
 *
 */
public class MCPluginV2 extends JavaPlugin implements Listener{

	public static final String WARP_CONFIG_FILE								= "plugins/MCPluginV2/warp.xml";
	public static final String SPELL_CONFIG_FILE							= "plugins/MCPluginV2/magic.xml";
	public static final String VERSION										= "20200713.0";

	private WarpManager warpManager;
	private CustomManager customManager;
	private MagicManager magicManager;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender != null && sender instanceof Player) {
			Player player = (Player) sender;
			String cmd = command.getName();
			switch (cmd) {
			case "warp":
				getWarpManager().onWarpCommand(player, args);
				return true;
			case "warpgroup":
				getWarpManager().onWarpGroupCommand(player, args);
				return true;
			case "detector":
				Detector.detect(player);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		getServer().getScheduler().cancelTasks(this);
	}

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO,"Enable plugin");
		super.onEnable();
		// Init warps
		initWarp();
		
		// Init customs
		initCustoms();
		
		// Init magic
		initMagic();

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(getWarpManager(), this);
		getServer().getPluginManager().registerEvents(getCustomManager(), this);
		getServer().getPluginManager().registerEvents(getMagicManager(), this);

		// Scheduled tasks
		BukkitScheduler scheduler = getServer().getScheduler();
		int res = scheduler.scheduleSyncRepeatingTask(this, new SaveTask(this), 0L, 6000L);
		if (res == -1)
			getLogger().log(Level.SEVERE, "Cannot schedule SaveTask");

		// Create custom recipes
		getLogger().log(Level.INFO, "Creating custom recipes...");
		getServer().addRecipe(Custom.createImprovedGoldPickaxeRecipe(this, Material.ACACIA_LOG));
		getServer().addRecipe(Custom.createImprovedGoldPickaxeRecipe(this, Material.BIRCH_LOG));
		getServer().addRecipe(Custom.createImprovedGoldPickaxeRecipe(this, Material.DARK_OAK_LOG));
		getServer().addRecipe(Custom.createImprovedGoldPickaxeRecipe(this, Material.JUNGLE_LOG));
		getServer().addRecipe(Custom.createImprovedGoldPickaxeRecipe(this, Material.OAK_LOG));
		getServer().addRecipe(Custom.createImprovedGoldPickaxeRecipe(this, Material.SPRUCE_LOG));

		getServer().addRecipe(Custom.createImprovedGoldAxeRecipe(this, Material.ACACIA_LOG));
		getServer().addRecipe(Custom.createImprovedGoldAxeRecipe(this, Material.BIRCH_LOG));
		getServer().addRecipe(Custom.createImprovedGoldAxeRecipe(this, Material.DARK_OAK_LOG));
		getServer().addRecipe(Custom.createImprovedGoldAxeRecipe(this, Material.JUNGLE_LOG));
		getServer().addRecipe(Custom.createImprovedGoldAxeRecipe(this, Material.OAK_LOG));
		getServer().addRecipe(Custom.createImprovedGoldAxeRecipe(this, Material.SPRUCE_LOG));

		getServer().addRecipe(Custom.createImprovedGoldShovelRecipe(this, Material.ACACIA_LOG));
		getServer().addRecipe(Custom.createImprovedGoldShovelRecipe(this, Material.BIRCH_LOG));
		getServer().addRecipe(Custom.createImprovedGoldShovelRecipe(this, Material.DARK_OAK_LOG));
		getServer().addRecipe(Custom.createImprovedGoldShovelRecipe(this, Material.JUNGLE_LOG));
		getServer().addRecipe(Custom.createImprovedGoldShovelRecipe(this, Material.OAK_LOG));
		getServer().addRecipe(Custom.createImprovedGoldShovelRecipe(this, Material.SPRUCE_LOG));

		getServer().addRecipe(Custom.createImprovedDiamondPickaxeRecipe(this, Material.ACACIA_LOG));
		getServer().addRecipe(Custom.createImprovedDiamondPickaxeRecipe(this, Material.BIRCH_LOG));
		getServer().addRecipe(Custom.createImprovedDiamondPickaxeRecipe(this, Material.DARK_OAK_LOG));
		getServer().addRecipe(Custom.createImprovedDiamondPickaxeRecipe(this, Material.JUNGLE_LOG));
		getServer().addRecipe(Custom.createImprovedDiamondPickaxeRecipe(this, Material.OAK_LOG));
		getServer().addRecipe(Custom.createImprovedDiamondPickaxeRecipe(this, Material.SPRUCE_LOG));

		getServer().addRecipe(Custom.createUltimateBottleRecipe(this));
		getServer().addRecipe(Custom.createSnowWandRecipe(this));
		getServer().addRecipe(Custom.createFireWandRecipe(this));
		getServer().addRecipe(Custom.createGrenadeRecipe(this));
		getServer().addRecipe(Custom.createNukeRecipe(this));
		getServer().addRecipe(Custom.createInventoryPotionRecipe(this));
		getLogger().log(Level.INFO, "Custom recipes created");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		reloadConfig();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		p.sendMessage(ChatColor.GREEN + "MCPlugin v2 "+VERSION);
		p.sendMessage(ChatColor.GREEN + "Les info sur triedge.ovh");
	}

	private void initWarp() {
		getLogger().log(Level.INFO,"Initializing warps configuration");
		setWarpManager(new WarpManager(this));
		try {
			getWarpManager().loadWarps(WARP_CONFIG_FILE);
		} catch (MCLoadingException e) {
			getLogger().log(Level.SEVERE, "Cannot load config file: "+WARP_CONFIG_FILE, e);
		}
		getLogger().log(Level.INFO,"Initialization of warps completed");
	}
	
	private void initCustoms() {
		getLogger().log(Level.INFO,"Initializing custom configuration");
		setCustomManager(new CustomManager(this));
		getLogger().log(Level.INFO,"Initialization of custom completed");
	}
	
	private void initMagic() {
		getLogger().log(Level.INFO,"Initializing magic configuration");
		setMagicManager(new MagicManager(this));
		getLogger().log(Level.INFO,"Initialization of magic completed");
	}

	public WarpManager getWarpManager() {
		return warpManager;
	}

	public void setWarpManager(WarpManager warpManager) {
		this.warpManager = warpManager;
	}

	public CustomManager getCustomManager() {
		return customManager;
	}

	public void setCustomManager(CustomManager customManager) {
		this.customManager = customManager;
	}

	public MagicManager getMagicManager() {
		return magicManager;
	}

	public void setMagicManager(MagicManager magicManager) {
		this.magicManager = magicManager;
	}
}
