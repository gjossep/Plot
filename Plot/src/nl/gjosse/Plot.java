package nl.gjosse;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Plot implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2551899929799781827L;

	private String world;
	
	private boolean bought = false;
	
	//first cornor
	private double xloc1;
	private double yloc1;
	private double zloc1;
	
	//second cornoro
	private double xloc2;
	private double yloc2;
	private double zloc2;
	
	private int id;
	
	private String owner;
	
	private int price;

	public Plot(String world,double xloc1, double yloc1, double zloc1, double xloc2, double yloc2, double zloc2, int id, String owner, int price) {
		this.world = world;
		this.xloc1 = xloc1;
		this.yloc1 = yloc1;
		this.zloc1 = zloc1;
		this.xloc2 = xloc2;
		this.yloc2 = yloc2;
		this.zloc2 = zloc2;
		this.id = id;
		this.owner = owner;
		this.price = price;

	}
	
	public boolean isBought() {
		return bought;
	}
	
	public void setBought(boolean boughts) {
		bought = boughts;
	}
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int newPrice) {
		price = newPrice;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(owner);
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getWorld() {
		return world;
	}

	public double getXloc1() {
		return xloc1;
	}

	public double getYloc1() {
		return yloc1;
	}

	public double getZloc1() {
		return zloc1;
	}

	public double getXloc2() {
		return xloc2;
	}

	public double getYloc2() {
		return yloc2;
	}

	public double getZloc2() {
		return zloc2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		long temp;
		temp = Double.doubleToLongBits(xloc1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xloc2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yloc1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yloc2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(zloc1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(zloc2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Plot other = (Plot) obj;
		if (id != other.id)
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (Double.doubleToLongBits(xloc1) != Double
				.doubleToLongBits(other.xloc1))
			return false;
		if (Double.doubleToLongBits(xloc2) != Double
				.doubleToLongBits(other.xloc2))
			return false;
		if (Double.doubleToLongBits(yloc1) != Double
				.doubleToLongBits(other.yloc1))
			return false;
		if (Double.doubleToLongBits(yloc2) != Double
				.doubleToLongBits(other.yloc2))
			return false;
		if (Double.doubleToLongBits(zloc1) != Double
				.doubleToLongBits(other.zloc1))
			return false;
		if (Double.doubleToLongBits(zloc2) != Double
				.doubleToLongBits(other.zloc2))
			return false;
		return true;
	}

	public Location getMinLoc() {
		return new Location(Bukkit.getWorld(world), xloc1, yloc1, zloc1);
	}

	public Location getMaxLoc() {
		return new Location(Bukkit.getWorld(world), xloc2, yloc2, zloc2);
	}

}
