package fr.triedge.minecraft.plugin.v2.warp;

import java.io.File;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.triedge.minecraft.plugin.v2.MCPluginV2;
import fr.triedge.minecraft.plugin.v2.exceptions.MCLoadingException;
import fr.triedge.minecraft.plugin.v2.utils.Utils;

public class WarpManager {

	private MCPluginV2 plugin;
	private WarpList warpList = new WarpList();
	
	public WarpManager(MCPluginV2 plugin) {
		super();
		this.setPlugin(plugin);
	}
	
	public void onWarpCommand(Player player, String[] args) {
		if (args.length > 0) {
			switch(args[0]) {
			case "create":
				actionCreateWarp(player,args);
				break;
			case "delete":
				break;
			case "list":
				actionListWarp(player,args);
				break;
			}
		}else {
			// Display help
			player.sendMessage(ChatColor.RED+"Il manque un paramètre!");
		}
	}
	

	public void onWarpGroupCommand(Player player, String[] args) {
		if (args.length > 0) {
			switch(args[0]) {
			case "create":
				break;
			case "delete":
				break;
			}
		}else {
			// Display help
			player.sendMessage(ChatColor.RED+"Il manque un paramètre!");
		}
	}
	
	private void actionCreateWarp(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED+"Il manque le nom du TP!");
			return;
		}
		String groupName = "none";
		if (args.length == 3) {
			WarpGroup tmp = getWarpList().getGroup(args[2]);
			if (tmp != null)
				groupName = tmp.getName();
			else
				player.sendMessage(ChatColor.RED+"Le group "+args[2]+" n'existe pas!");
		}
		String name = args[1];
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (block.getType().equals(Material.DIAMOND_BLOCK)) {
			Vector vector = player.getLocation().toVector();
			String world = player.getWorld().getName();
			float pitch = player.getLocation().getPitch();
			float yaw = player.getLocation().getYaw();
			Warp warp = new Warp(name);
			warp.setWorld(world);
			warp.setLocationX(vector.getBlockX());
			warp.setLocationY(vector.getBlockY());
			warp.setLocationZ(vector.getBlockZ());
			warp.setPitch(pitch);
			warp.setYaw(yaw);
			warp.setGroup(groupName);
			getWarpList().addWarp(warp);
			
			player.sendMessage("Cible de teleportation sauvegardé: "+name);
			getPlugin().getLogger().info("# REGISTER TP: "+name+"["+vector.getBlockX()+"/"+vector.getBlockY()+"/"+vector.getBlockZ()+"]");
		}else {
			player.sendMessage("Vous devez etre sur un block de diamant pour cette commande.");
		}
		
	}
	
	private void actionListWarp(Player player, String[] args) {
		if (getWarpList().getWarps().isEmpty()) {
			player.sendMessage("List vide!");
			return;
		}
		StringBuilder tmp = new StringBuilder();
		for (Warp warp : getWarpList().getWarps()) {
			String name = warp.getName();
			if (!name.startsWith("h_")) {
				tmp.append(name);
				tmp.append(", ");
			}
		}
		player.sendMessage(tmp.toString());
	}

	public void save(String path) throws JAXBException {
		getPlugin().getLogger().log(Level.INFO,"Storing warps into "+path+"...");
		Utils.storeXml(getWarpList(), new File(path));
		getPlugin().getLogger().log(Level.INFO,"Warps stored");
	}
	
	public void loadWarps(String path) throws MCLoadingException {
		getPlugin().getLogger().log(Level.INFO,"Loading warps from file "+path+"...");
		File file = new File(path);
		if (file.exists()) {
			try {
				WarpList list = Utils.loadXml(WarpList.class, file);
				if (list == null) {
					getPlugin().getLogger().log(Level.SEVERE, "Warp loaded list is null!");
					throw new MCLoadingException("Warp loaded list is null");
				}
				getPlugin().getLogger().log(Level.INFO,"Warps loaded");
				setWarpList(list);
			} catch (JAXBException e) {
				getPlugin().getLogger().log(Level.SEVERE, "Cannot load config file: "+file.getAbsolutePath(), e);
			}
			
		}else {
			getPlugin().getLogger().log(Level.WARNING, "Config file doesn't exists, created empty in "+file.getAbsolutePath());
			setWarpList(new WarpList());
		}
	}

	public WarpList getWarpList() {
		return warpList;
	}

	public void setWarpList(WarpList warpList) {
		this.warpList = warpList;
	}

	public MCPluginV2 getPlugin() {
		return plugin;
	}

	public void setPlugin(MCPluginV2 plugin) {
		this.plugin = plugin;
	}
}
