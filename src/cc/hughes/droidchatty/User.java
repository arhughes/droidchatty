package cc.hughes.droidchatty;

import java.util.Arrays;
import java.util.HashSet;

import android.graphics.Color;

public class User {
	
	private final static HashSet<String> MODS = new HashSet<String>(Arrays.asList(new String[] { "ajax", "degenerate", "drucifer", "dante", "genjuro", "hirez", "lacker", "pupismyname", "thekidd", "zakk", "brickmatt", "carnivac", "edgewise", "filtersweep", "haiku", "multisync", "rauol duke", "ninjase", "tomservo", "busdriver3030", "cygnus x-1", "dognose", "edlin", "geedeck", "helvetica", "kaiser", "paranoid android", "portax", "sexpansion pack", "sgtsanity", "utilitymaximizer", "mikecyb", "dave-a" }));
	private final static HashSet<String> EMPLOYEES = new HashSet<String>(Arrays.asList(new String[] { "shacknews", "aaron linde", "alice o'conner", "jeff mattas", "garnet lee", "brian leahy", "ackbar2020", "greg-m", "xavdematos", "shugamom" }));
	
	public static int getColor(String userName)
	{
		if (isEmployee(userName))
			return Color.GREEN;
		else if (isModerator(userName))
			return Color.RED;
				
		return Color.rgb(0xf3, 0xe7, 0xb5);
	}
	
	public static Boolean isEmployee(String userName)
	{
		return EMPLOYEES.contains(userName.toLowerCase());
	}
	
	public static Boolean isModerator(String userName)
	{
		return MODS.contains(userName.toLowerCase());
	}

}
