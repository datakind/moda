package Parser;

import java.util.ArrayList; 
import java.util.Arrays;
import java.lang.String;


public class Address{
	public ArrayList<House> houses;
	public Street street;
		
	public Address(House house, Street street) {
		houses = new ArrayList<House>();
		houses.add(house);
		this.street=street;
	}
	
	// when address is a single var
	public Address(Parser p, ArrayList<String> boros, String unparsed_address){
		String h = "", s = Parser.compbl(unparsed_address.trim().toUpperCase());
		String[] spl;
		
		if (s.length()==0) return;
		
		// separate out house and street
		if (s.contains(" ") && Parser.anydigit(s.substring(0, 1)) ) {
			spl = s.split(" ");
			h = spl[0];
			//fractional house number
			if (spl[1].contains("/") && !Parser.notdigit(spl[1].replaceAll("/", "")) && spl[1].substring(0,1).equals("1")) h = h + " " + spl[1];
				
			//alpha house number suffix split from house number
			if (spl[1].length() ==1  && Parser.anyalpha(spl[1]) && !Arrays.asList("N","S","E","W").contains(spl[1]) && spl.length > 2
					&& !Arrays.asList("RD","ROAD","ST","STREET","AVE","AV","AVENUE").contains(spl[2])) {
				h = h + spl[1];
				s = s.substring(h.length()+1).trim();
			} else s = s.substring(h.length());
			
		}
		
		//remove stray hyphens at the end of housenum
		while (h.length()>1 && h.substring(h.length()-1, h.length()).equals("-")) h = h.substring(0, h.length()-1);
			
		// clean street
		s = Parser.cleanStreet(s);
		
		// set values of houses
		houses=House.Houses(h.trim().toUpperCase());
		
		// see if house number snuck into street. if so, fix that crap
		spl = s.split(" ");
		// first sign: first chunk is all numbers, there are at least 2 chunks, there is only one house number
		if (!Parser.anyalpha(spl[0]) && spl.length>1 && this.houses.size()==1) {
			// second chunk is not a street suffix that would occur with a numeric street name
			// second chunk is also not a unit
			if (spl[1].replaceAll("AVENUE|STREET|ROAD|DRIVE|PLACE|BOULEVARD|APARTMENT|BASEMENT|BUILDING|FLOOR|FRONT|SUITE|LANE", "").equals(spl[1])) {
				House firstHouse = this.houses.get(0);
				h = h + "-" + spl[0];
				this.houses=House.Houses(h.trim().toUpperCase());
				
				//if it is not a range, allow for the possibility that it was not supposed to be hyphenated
				if (firstHouse.num >= Long.parseLong(spl[0]) || Long.parseLong(spl[0]) - firstHouse.num>= 10) houses.add(firstHouse);

				s = s.substring(spl[0].length());
			}
		}

		this.street=new Street(p, boros, s);			
	}
	
	// when address is 2 vars
	public Address(Parser p, ArrayList<String> boros, String unparsed_house, String unparsed_street){
		unparsed_street = Parser.cleanStreet(unparsed_street);
		this.houses=House.Houses(unparsed_house.trim().toUpperCase());
		this.street=new Street(p, boros, unparsed_street.trim());
	}	
	
	public String toString(){
		return houses.toString() + " " + street.toString();
	}		
	
	public static void main(String[] args){
		Parser p = new Parser();
		Address a = new Address(p, new ArrayList<String>(Arrays.asList("3")), "245 - 01","149th Road");
		StdOut.println(a.houses.toString());
		StdOut.println(a.street.direction);
		StdOut.println(a.street.name);
		StdOut.println(a.street.suffix);
		
	}
}
