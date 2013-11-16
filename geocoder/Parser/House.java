package Parser;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class House {
	public String housenum = "", unit = "";
	public long num = 0;
	
	private static Pattern pattern;
	private static Matcher matcher;
	
	public House(String housenum, String unit) {
		this.housenum=housenum;
		this.unit=unit;
		this.num = Long.parseLong(housenum.replaceAll("[^0-9]", ""));
	}
	
	//takes a single token and returns a single house object 
	public House(String unparsed) {
		
		//clean input
		unparsed = Parser.compbl(unparsed.replaceAll("[^0-9A-Za-z-/]", "").trim());
		
		if (unparsed.length()==0) return;
		if (!Parser.anydigit(unparsed) && !Parser.anyalpha(unparsed)) return;
		
		while(unparsed.indexOf("--")>=0) unparsed = unparsed.replaceAll("--","-");

		//see if there is an alpha suffix
		pattern = Pattern.compile("[A-Za-z]+");
		matcher = pattern.matcher(unparsed);		
		if (matcher.find()) {
			unit=matcher.group(0).replaceAll("[^A-Za-z]", "");
			if(unit.length()<unparsed.length()) housenum = unparsed.substring(0, unparsed.length()-unit.length());
			else housenum = "";
		}
		else housenum = unparsed; 
		
		//remove hyphen at end;
		if (housenum.length()>1 && housenum.substring(housenum.length()-1).equals("-")) housenum = housenum.substring(0, housenum.length()-1);
		
		//set numeric 
		if (housenum.replaceAll("[^0-9]", "").length()>0) num = Long.parseLong(housenum.replaceAll("[^0-9]", ""));
		
	}		
	public String toString(){
		if (unit.contains("/")) return (housenum + " " + unit).trim();
		else return (housenum + unit).trim();
	}
	
	//takes several tokens and figures out how to feed tokens to House constructor
	public static ArrayList<House> Houses(String unparsed) {
		
		House house;
		ArrayList<House> houses = new ArrayList<House>();
		
		//clean input
		unparsed = Parser.compbl(unparsed.replaceAll("[^0-9A-Za-z-/\\s]", "").trim());
		unparsed = unparsed.replaceAll(" -", "-");
		unparsed = unparsed.replaceAll("- ", "-");
		
		//empty
		if (unparsed.length() == 0) return houses;
		
		//only one token
		if (!unparsed.contains(" ")) {
			house = new House(unparsed);
			if (house.num>0 || house.unit.length()>0) houses.add(house);
			// hyphenated address could be a range
			if (house.housenum.contains("-") && house.unit.equals("")) {
				String s[] = house.housenum.split("-");
				house = new House(s[0]);
				if (house.num>0 || house.unit.length()>0) houses.add(house);				
				if (!Parser.notdigit(s[0]) && !Parser.notdigit(s[1]) 
						&& Long.parseLong(s[0]) <= Long.parseLong(s[1]) && Long.parseLong(s[1]) - Long.parseLong(s[0]) <= 12) {
					house = new House(s[1]);
					if (house.num>0 || house.unit.length()>0) houses.add(house);
				}
			}
		} 
		//multiple tokens
		else {
			String housesSplit[] = unparsed.split(" ");
			if (housesSplit[1].contains("/")) {
				house = new House(housesSplit[0]);
				house.unit = housesSplit[1];
				if (house.num>0 || house.unit.length()>0) houses.add(house);
			} else if (!Parser.notdigit(housesSplit[0]) && !Parser.notdigit(housesSplit[1]) && 
					(Long.parseLong(housesSplit[0]) > Long.parseLong(housesSplit[1]) || Long.parseLong(housesSplit[1]) - Long.parseLong(housesSplit[0]) > 12)) {
				//in case there is a space where there shouldnt be
				house = new House(unparsed.replaceAll(" ", ""));
				if (house.num>0 || house.unit.length()>0) houses.add(house);				
				
			} else for (int i=0; i < housesSplit.length; i++) {
				house = new House(housesSplit[i]);
				if (house.num>0 || house.unit.length()>0) houses.add(house);
				// hyphenated address could be a range
				if (house.housenum.contains("-") && house.unit.equals("")) {
					String s[] = house.housenum.split("-");
					house = new House(s[0]);
					if (house.num>0 || house.unit.length()>0) houses.add(house);					
					if (!Parser.notdigit(s[0]) && !Parser.notdigit(s[1]) &&
							Long.parseLong(s[0]) < Long.parseLong(s[1]) && Long.parseLong(s[1]) - Long.parseLong(s[0]) <= 12) {
						house = new House(s[1]);
						if (house.num>0 || house.unit.length()>0) houses.add(house);
					} 
				}
			}
		}
		return houses;
	}		
	public static void main(String[] args){
		String nums = "2-50";
		ArrayList<House> h = Houses(nums);
		StdOut.println(h.toString());	
	}	
}