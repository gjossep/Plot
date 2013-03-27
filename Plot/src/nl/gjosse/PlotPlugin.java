package nl.gjosse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PlotPlugin extends JavaPlugin{
	
	public Logger log = Logger.getLogger("Minecraft");
	public WorldEditPlugin worldEdit;
	public WorldGuardPlugin worldGuard;
	public static Economy econ = null;
	public static Permission permission = null;

	
	public ArrayList<Plot> plots = new ArrayList<Plot>();
	public static int idlist = 1;
	public void onEnable() {
		if(!checkWorldEdit()) {
			log.severe("Can't find the plugin \"WorldEdit\"");
            getServer().getPluginManager().disablePlugin(this);
		}
		
		if(!checkWorldGaurd()) {
			log.severe("Can't find the plugin \"WorldGaurd\"");
            getServer().getPluginManager().disablePlugin(this);
		}
		
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		if (!setupPermissions() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		

		if(!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		} else {
			plots = readFiles(getDataFolder());
		}
		
		
		getCommand("plot").setExecutor(new PlotCommandExecutor(this));
	}
	
	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
	}

	public void onDisable() {
		writeFiles(getDataFolder(), plots);
	}
	
	public void saveFiles() {
		writeFiles(getDataFolder(), plots);
	}
	private boolean checkWorldGaurd() {
		worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
		 
	    // WorldGuard may not be loaded
	    if (worldGuard == null) {
	        return false; // Maybe you want throw an exception instead
	    } else {
	    	return true;
	    }
	}

	private boolean checkWorldEdit() {
		worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if(worldEdit==null) {
			return false;
		} else {
			return true;
		}
	}
	
	public ArrayList<Plot> getPlots(String name) {
		ArrayList<Plot> returnplot = new ArrayList<Plot>();
		
		for(Plot plot : plots) {
			if(plot.getOwner().equalsIgnoreCase(name)) {
				returnplot.add(plot);
			}
		}
		
		return returnplot;
	}
	
	public boolean hasPlots(String name) {
		for(Plot plot : plots) {
			if(plot.getOwner().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}

	public boolean checkIfInPlot(Player p) {
		Location ploc = p.getLocation();
		ArrayList<Plot> plots2 = getPlots(p.getName());
		for(Plot plot : plots2) {
			if(inArea(ploc, plot.getMinLoc(), plot.getMaxLoc(), true)) {
				return true;
			}
		}
		return false;
	}

	public Plot getStandingPlot(Player p) {
		Location ploc = p.getLocation();
		Plot returnplot = null;
		ArrayList<Plot> plots2 = getPlots(p.getName());
		for(Plot plot : plots2) {
			if(inArea(ploc, plot.getMinLoc(), plot.getMaxLoc(), true)) {
				returnplot = plot;
			}
		}	
		return returnplot;
	}

	public boolean inArea(Location targetLocation, Location inAreaLocation1, Location inAreaLocation2, boolean checkY){
	    if(inAreaLocation1.getWorld().getName() == inAreaLocation2.getWorld().getName()){ // Check for worldName location1, location2
	        if(targetLocation.getWorld().getName() == inAreaLocation1.getWorld().getName()){ // Check for worldName targetLocation, location1
	            if((targetLocation.getBlockX() >= inAreaLocation1.getBlockX() && targetLocation.getBlockX() <= inAreaLocation2.getBlockX()) || (targetLocation.getBlockX() <= inAreaLocation1.getBlockX() && targetLocation.getBlockX() >= inAreaLocation2.getBlockX())){ // Check X value
	                if((targetLocation.getBlockZ() >= inAreaLocation1.getBlockZ() && targetLocation.getBlockZ() <= inAreaLocation2.getBlockZ()) || (targetLocation.getBlockZ() <= inAreaLocation1.getBlockZ() && targetLocation.getBlockZ() >= inAreaLocation2.getBlockZ())){ // Check Z value
	                    if(checkY == true){ // If should check for Y value
	                        if((targetLocation.getBlockY() >= inAreaLocation1.getBlockY() && targetLocation.getBlockY() <= inAreaLocation2.getBlockY()) || (targetLocation.getBlockY() <= inAreaLocation1.getBlockY() && targetLocation.getBlockY() >= inAreaLocation2.getBlockY())){ // Check Y value
	                            return true;
	                        }
	                    }else{
	                        return true;
	                    }
	                }
	            }
	        }
	    }
	    return false;
	}

	public boolean removeMoney(Player p, int price, int id) {
		 EconomyResponse r = econ.withdrawPlayer(p.getName(), price);
         if(r.transactionSuccess()) {
             p.sendMessage(ChatColor.GREEN+"You have bought a plot[ID="+id+"] for a price of "+ChatColor.GOLD+price);
             return true;
         } else {
             p.sendMessage(ChatColor.RED+"An error occured:  "+r.errorMessage);
             return false;
         }
	}
	
	public boolean addMoney(Player p, int price, int id) {
		EconomyResponse r = econ.depositPlayer(p.getName(), price);
		if(r.transactionSuccess()) {
            p.sendMessage(ChatColor.GREEN+"You have sold a plot[ID="+id+"], and got back "+ChatColor.GOLD+price);
            return true;
		} else {
            p.sendMessage(ChatColor.RED+"An error occured:  "+r.errorMessage);
            return false;
		}
	}
	
	
	public static ArrayList<Plot> readFiles(File file) {
		ArrayList<Plot> plots = new ArrayList<Plot>();
		File FileFolder = new File(file, "plots");
		if(!FileFolder.exists()) {
			FileFolder.mkdir();
		}
		
		if(FileFolder.listFiles().length==0) {
			return plots;
		}
		for(File f : FileFolder.listFiles()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
				Object readObject = ois.readObject();
				if(readObject instanceof Plot) {
					Plot plot = (Plot) readObject;
					plots.add(plot);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		File otherdata = new File(file, "loc.txt");
		try {
			FileReader fr = new FileReader(otherdata);
			BufferedReader br = new BufferedReader(fr);
			
			idlist = Integer.parseInt(br.readLine());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return plots;
	}
	
	public static void writeFiles(File file, ArrayList<Plot> plots) {
		File FileFolder = new File(file, "plots");
		
		if(!FileFolder.exists()) {
			FileFolder.mkdir();
		}
		
		for(Plot plot : plots) {
	        File fileplot = new File(FileFolder, plot.getId()+".dat");
	        try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileplot));
				oos.writeObject(plot);
				oos.flush();
				oos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		File otherdata = new File(file, "loc.txt");
		if(!otherdata.exists()) {
			try {
				otherdata.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			BufferedWriter bw = null;
			FileWriter fw = null;
			fw = new FileWriter(otherdata, false);
			bw = new BufferedWriter(fw);
			
			bw.write(""+idlist);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
