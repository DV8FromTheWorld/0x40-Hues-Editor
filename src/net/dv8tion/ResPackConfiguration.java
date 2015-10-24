package net.dv8tion;

import java.util.ArrayList;

import net.dv8tion.types.ResPack;

public class ResPackConfiguration
{
	private ArrayList<ResPack> packs;
	
	public ResPackConfiguration()
	{
		packs = new ArrayList<ResPack>();
	}
	
	
	public ArrayList<ResPack> getResPacks()
	{
		return packs;
	}
}
