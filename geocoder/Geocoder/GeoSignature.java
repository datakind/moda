package Geocoder;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Parser.Address;
import Parser.House;
import Parser.Intersection;
import Parser.Intersector;
import Parser.Parser;
import Parser.StdOut;

public class GeoSignature {
	
	public ArrayList<House> houses;
	public ArrayList<String>[] streetCodes1, streetCodes2;
	private static Pattern pattern;
	private static Matcher matcher;
	public boolean isIntersection, isAddress;
	
	private Intersection i;
	private Address a;
	
	private static ArrayList<String>[] streetCodes(GeoMatcher g, String s){
		if (!g.streetMatch.containsKey(s)) return new ArrayList[6];
		return g.streetMatch.get(s);
	}
	
	public GeoSignature(GeoMatcher g, ArrayList<String> boros, String addrLine){
		
		//remove quotations that sometimes occur with CSVs
		addrLine = addrLine.replaceAll("\"","").trim().toUpperCase();
		
		pattern = Pattern.compile("(\\b)(BETWEEN|BTWN)(\\b)");
		matcher = pattern.matcher(addrLine);	
		if (matcher.find() && matcher.start()>0) addrLine = addrLine.substring(0, matcher.start());
		if (addrLine.length()==0) return;
		
		String full_street1, full_street2;
		Intersector dividor = new Intersector(addrLine);

		//intersections
		if (dividor.exists) {
			String str1 = addrLine.substring(0, dividor.startPos);
			String str2 = addrLine.substring(dividor.endPos);
			//System.out.println(str1 + "," + str2);
			i = new Intersection(g.parser, boros, str1, str2);
			full_street1 = i.street1.direction + i.street1.name + i.street1.suffix;
			streetCodes1 = streetCodes(g, full_street1);
			full_street2 = i.street2.direction + i.street2.name + i.street2.suffix;
			streetCodes2 = streetCodes(g, full_street2);		
			isIntersection = true;
		}
		//addresses
		else {
			a = new Address(g.parser, boros, addrLine);
			full_street1 = a.street.direction + a.street.name + a.street.suffix;
			streetCodes1 = streetCodes(g, full_street1);
			houses = a.houses;
			isAddress = true;
		}	
	}
	
	public GeoSignature(GeoMatcher g, ArrayList<String> boros, String building, String street){
		String full_street1, full_street2;
		
		pattern = Pattern.compile("(\\b)(BETWEEN|BTWN)(\\b)");
		matcher = pattern.matcher(street);	
		if (matcher.find() && matcher.start()>0) street = street.substring(0, matcher.start());
		if (street.length()==0) return;
		
		Intersector dividor = new Intersector(street);
		
		//remove quotations that sometimes occur with CSVs
		building = building.replaceAll("\"","");
		street = street.replaceAll("\"","");
		
		building = Parser.compbl(building).trim().toUpperCase();
		//intersections
		if (dividor.exists && building.length()==0) {
			String str1 = street.substring(0, dividor.startPos);
			String str2 = street.substring(dividor.endPos);
			i = new Intersection(g.parser, boros, str1, str2);
			full_street1 = i.street1.direction + i.street1.name + i.street1.suffix;
			streetCodes1 = streetCodes(g, full_street1);
			full_street2 = i.street2.direction + i.street2.name + i.street2.suffix;
			streetCodes2 = streetCodes(g, full_street2);
			isIntersection = true;
		}
		//addresses
		else {
			a = new Address(g.parser, boros, building, street);
			full_street1 = a.street.direction + a.street.name + a.street.suffix;
			streetCodes1 = streetCodes(g, full_street1);
			houses = a.houses;
			isAddress = true;
		}	
	}
	
	public String toString(){
		if (a != null) return a.toString();
		else if (i != null) return i.toString();
		else return "ERROR: No address or intersection";
	}
	public static void main(String[] args) {
		GeoMatcher m = new GeoMatcher();		

		ArrayList<String> boros = new ArrayList<String>();
		boros.add("3");
		GeoSignature sig = new GeoSignature(m, boros, "2617 86th STREET");
		StdOut.println(sig.toString());		
		
        /*
		ArrayList<String> boros = m.getBoros("3", "", "");
		System.out.println(boros.toString());
		
		GeoSignature sig = new GeoSignature(m, boros, "BETWEEN 15TH & 16TH ON AVE Y");
		System.out.println(sig.isAddress);
		if (sig.streetCodes1 != null) System.out.println(sig.streetCodes1[3]);
		if (sig.isAddress) System.out.println(String.valueOf(sig.houses.get(0).num) + " " + sig.houses.get(0).unit);
		*/
		GeoLocation loc = new GeoLocation(m, sig, boros);
		
		if (loc.Found) System.out.println("Correctly geocoded:");
		System.out.println("House : " + loc.correctHouse);
		System.out.println("Street1 : " + loc.correctFeatureName1 + " -->" + loc.correctb7sc_1);
		System.out.println("Street2 : " + loc.correctFeatureName2 + " -->" + loc.correctb7sc_2);
		System.out.println("Boro : " + loc.correctBoro);
		

	}

}
