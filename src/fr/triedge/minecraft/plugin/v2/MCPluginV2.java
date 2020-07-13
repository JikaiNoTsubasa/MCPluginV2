package fr.triedge.minecraft.plugin.v2;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.triedge.minecraft.plugin.v2.detector.Detector;
import fr.triedge.minecraft.plugin.v2.exceptions.MCLoadingException;
import fr.triedge.minecraft.plugin.v2.task.SaveTask;
import fr.triedge.minecraft.plugin.v2.warp.WarpManager;

public class MCPluginV2 extends JavaPlugin implements Listener{
	
	public static final String WARP_CONFIG_FILE								= "plugins/MCPluginV2/warp.xml";
	public static final String VERSION										= "20200712.0";
	
	private WarpManager warpManager;
	
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
		// Load warps
		initWarp();
		
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(getWarpManager(), this);
		
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

	public WarpManager getWarpManager() {
		return warpManager;
	}

	public void setWarpManager(WarpManager warpManager) {
		this.warpManager = warpManager;
	}
}
