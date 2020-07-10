package fr.triedge.minecraft.plugin.v2;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.triedge.minecraft.plugin.v2.exceptions.MCLoadingException;
import fr.triedge.minecraft.plugin.v2.task.SaveTask;
import fr.triedge.minecraft.plugin.v2.warp.WarpManager;

public class MCPluginV2 extends JavaPlugin{
	
	public static final String WARP_CONFIG_FILE								= "plugins/MCPluginV2/warp.xml";
	
	private WarpManager warpManager;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender != null && sender instanceof Player) {
			Player player = (Player) sender;
			String cmd = command.getName();
			switch (cmd) {
			case "warp":
				getWarpManager().onWarpCommand(player, args);
				break;
			case "warpgroup":
				getWarpManager().onWarpGroupCommand(player, args);
				break;
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
		super.onEnable();
		// Load warps
		initWarp();
		
		// Scheduled tasks
		BukkitScheduler scheduler = getServer().getScheduler();
		int res = scheduler.scheduleSyncRepeatingTask(this, new SaveTask(this), 0L, 6000L);
		if (res == -1)
			getLogger().log(Level.SEVERE, "Cannot schedule SaveTask");
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		reloadConfig();
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

	public WarpManager getWarpManager() {
		return warpManager;
	}

	public void setWarpManager(WarpManager warpManager) {
		this.warpManager = warpManager;
	}
}
