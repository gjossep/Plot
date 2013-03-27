
package nl.gjosse;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public class PlotCommandExecutor implements CommandExecutor {

	PlotPlugin plugin;
	
	public PlotCommandExecutor(PlotPlugin plotPlugin) {
		plugin = plotPlugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(cmd.getName().equalsIgnoreCase("plot")) {
			System.out.println("argslength: "+args.length );
			if(args.length>=1) {
				if(args[0].equalsIgnoreCase("add") && plugin.permission.has(sender, "plot.add")) {
					if(args.length==2) {
						if(sender instanceof Player) {
							int price = 0;
							try {
								price = Integer.parseInt(args[1]);
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED+"Make sure that price is a number!");
							}
							Player p = (Player) sender;
							p.sendMessage(ChatColor.GOLD+"Trying to add your selection as a plot!");
							Selection selection = plugin.worldEdit.getSelection(p);
							if (selection != null) {
						    	World world = selection.getWorld();
						    	Location min = selection.getMinimumPoint();
						    	Location max = selection.getMaximumPoint();
						    
						    	Plot plot = new Plot(world.getName(),min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), plugin.idlist, p.getName(), price);
						    	addProtection(plot);
						    	plugin.plots.add(plot);
						    	
						    	plugin.idlist++;
						    
						    	p.sendMessage(ChatColor.GREEN+"Plot Added! ID = "+ChatColor.GOLD+plot.getId());
							    plugin.saveFiles();

						    
							} else {
								p.sendMessage(ChatColor.RED+"You have no selection!");
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED+"Wrong Argument! Do /plot add price");
					}
				}
				
				if(args[0].equalsIgnoreCase("invite") && plugin.permission.has(sender, "plot.basic")) {
					if(args.length==2) {
						String playername = args[1];
						Player p;
						try{
							p = Bukkit.getPlayer(playername.toLowerCase());
						} catch(Exception e) {
							sender.sendMessage(ChatColor.RED+"Can't find player!");
						}
						
						
					} else {
						sender.sendMessage(ChatColor.GOLD+"Help invite");
					}
				}
				
				if(args[0].equalsIgnoreCase("border")) {
					Player p = (Player) sender;
					Selection selection = plugin.worldEdit.getSelection(p);
					if (selection != null) {
				    	World world = selection.getWorld();
				    	Location min = selection.getMinimumPoint();
				    	Location max = selection.getMaximumPoint();
				    	
				    	world.getBlockAt(min).setTypeId(1);
				    	world.getBlockAt(max).setTypeId(1);
				    	
				    	for(int x = min.getBlockX(); x<= max.getBlockX(); x++) {
				    		for(int z = min.getBlockZ(); z<=max.getBlockZ(); z++) {
				    			if(x == 1 || z == 1) {
				    				Block blockAt = world.getBlockAt(x, min.getBlockY(), z);
				    				blockAt.setTypeId(1);
				    			}
				    		}
				    	}
					}
				}
				
				if(args[0].equalsIgnoreCase("buy") && plugin.permission.has(sender, "plot.buy")) {
					if(args.length==2) {
						int id = 0;
						try {
							id = Integer.parseInt(args[1]);
						} catch(Exception e) {
							sender.sendMessage(ChatColor.RED+"Make sure that [ID] is a number!");
						}
						
						Plot plot = searchPlots(id);
				        Player p = (Player) sender;
						if(plot!=null) {
							if(plot.isBought()) {
								sender.sendMessage(ChatColor.RED+"This plot is already bought!");
							} else {
							
							if(plugin.removeMoney(p, plot.getPrice(), plot.getId())) {
								ApplicableRegionSet set = plugin.worldGuard.getRegionManager(Bukkit.getWorld(plot.getWorld())).getApplicableRegions(new Location(Bukkit.getWorld(plot.getWorld()), plot.getXloc1(), plot.getYloc1(), plot.getZloc1()));
								for (ProtectedRegion region : set) {
									if(set.size()==1) {
									sender.sendMessage(ChatColor.GOLD+"The price of this plot is "+plot.getPrice());
							        DefaultDomain owners = region.getOwners();
							        owners.addPlayer(sender.getName());
							        region.setOwners(owners);
							    	plot.setBought(true);
							        plot.setOwner(sender.getName());
							        try {
							          plugin.worldGuard.getRegionManager(Bukkit.getWorld(plot.getWorld())).save();
							        } catch (ProtectionDatabaseException ex) {
							            plugin.log.log(Level.SEVERE, null, ex);
							        }
							        
							        plugin.plots.remove(plot);
							        plugin.plots.add(plot);
								    plugin.saveFiles();

								} 
							}
						}
						}
						} else {
							sender.sendMessage(ChatColor.RED+"Can't find a plot with that ID");
						}
					} else {
						sender.sendMessage(ChatColor.RED+"To buy a plot do /plot buy [ID]! Do /plot check [ID] to check the price!");
					}
				}
				
				if(args[0].equalsIgnoreCase("sell") && plugin.permission.has(sender, "plot.sell")) {
					if(plugin.hasPlots(sender.getName())) {
						Player p = (Player) sender;
						if (plugin.checkIfInPlot(p)) {
							Plot inplot = plugin.getStandingPlot(p);
							p.teleport(Bukkit.getWorld(inplot.getWorld()).getSpawnLocation());
							addItems(inplot, p);
							ApplicableRegionSet set = plugin.worldGuard.getRegionManager(Bukkit.getWorld(inplot.getWorld())).getApplicableRegions(new Location(Bukkit.getWorld(inplot.getWorld()), inplot.getXloc1(), inplot.getYloc1(), inplot.getZloc1()));
							for (ProtectedRegion region : set) {
								if(set.size()==1) {
									 DefaultDomain owners = region.getOwners();
									 owners.removePlayer(p.getName());
								     region.setOwners(owners);
								     
								     try {
								          plugin.worldGuard.getRegionManager(Bukkit.getWorld(inplot.getWorld())).save();
								        } catch (ProtectionDatabaseException ex) {
								            plugin.log.log(Level.SEVERE, null, ex);
								        }
								     
								     plugin.addMoney(p, inplot.getPrice()/2, inplot.getId());
								     plugin.plots.remove(inplot);
								     plugin.plots.add(inplot);
								     plugin.saveFiles();
								}
							}
						} else {
							sender.sendMessage(ChatColor.RED+"Make sure you are standing in the plot you want to sell!");
						}
					} else {
						sender.sendMessage(ChatColor.RED+"You dont have any plots!");
					}
				}
				
			} else {
				sender.sendMessage(ChatColor.GOLD+"Help!");
			}
			return true;
		}
		return false;
	}
	
	private void addItems(Plot inplot, Player p) {
		for(double x = inplot.getXloc1(); x<=inplot.getXloc2(); x++) {
			for(double y = inplot.getYloc1(); y<=inplot.getYloc2(); y++) {
				for(double z = inplot.getZloc1(); z<=inplot.getZloc2(); z++) {
					Block blockAt = Bukkit.getWorld(inplot.getWorld()).getBlockAt((int)x,(int)y,(int)z);
					if(blockAt.getTypeId()!=0) {
						p.getInventory().addItem(new ItemStack(blockAt.getType(), 1));
					}
					blockAt.setTypeId(0);
				}
			}
		}
	}

	private Plot searchPlots(int id) {
		Plot returnPlot = null;
		for(Plot plot : plugin.plots) {
			if(plot.getId()==id) {
				returnPlot = plot;
				return returnPlot;
			}
		}
		return null;
	}

	private void addProtection(Plot plot) {
		ProtectedRegion region = new ProtectedCuboidRegion(""+plot.getId(), getProtectionVectorLeft(plot), getProtectionVectorRight(plot));
		DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(plot.getOwner());
        if(plot.getOwner().equalsIgnoreCase("sanchezDeBeast")) {
        	owners.addPlayer("gjosse");
        } else if(plot.getOwner().equalsIgnoreCase("gjosse")) {
        	owners.addPlayer("SanchezDeBeast");
        }
        region.setOwners(owners);
        
        addPref(plot, region);
        try {
			plugin.worldGuard.getRegionManager(Bukkit.getWorld(plot.getWorld())).save();
		} catch (ProtectionDatabaseException e) {
			plugin.log.log(Level.SEVERE, "Failed to add protection to plot! Failed to Save");
		}

	}

	private void addPref(Plot plot, ProtectedRegion region) {
		try {
			region.setParent(plugin.worldGuard.getRegionManager(Bukkit.getWorld(plot.getWorld())).getRegion("__Global__"));
			region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(plugin.worldGuard, plot.getPlayer(), "You are entering a protected island area."));
		    region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(plugin.worldGuard, plot.getPlayer(), "You are leaving a protected island area."));
		    region.setFlag(DefaultFlag.ENTRY, DefaultFlag.ENTRY.parseInput(plugin.worldGuard, plot.getPlayer(), "deny"));

		    plugin.worldGuard.getRegionManager(Bukkit.getWorld(plot.getWorld())).addRegion(region);		
		} catch (CircularInheritanceException e) {
			plugin.log.log(Level.SEVERE, "Failed to add protection to plot! CircularInheritanceException");
		} catch (InvalidFlagFormat e) {
			plugin.log.log(Level.SEVERE, "Failed to add protection to plot! InvalidFlagFormat");
		}
       
	}

	private BlockVector getProtectionVectorRight(Plot plot) {
		return new BlockVector(plot.getXloc1(), plot.getYloc1(), plot.getZloc1());
	}

	private BlockVector getProtectionVectorLeft(Plot plot) {
		return new BlockVector(plot.getXloc2(), plot.getYloc2(), plot.getZloc2());
	}

}
