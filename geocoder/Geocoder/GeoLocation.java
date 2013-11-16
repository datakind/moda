package Geocoder;

import java.util.ArrayList;
import Parser.House;
import Parser.Parser;
import Parser.StdOut;


public class GeoLocation {
	
	public String  addressID="", correctHouse ="", correctBoro="", correctFeatureName1 = "", correctFeatureName2="", 
					correctb7sc_1="", correctb7sc_2="";
	public String[] results;
	public boolean Found;
	
	public GeoLocation(GeoMatcher m, GeoSignature sig, ArrayList<String> boros) {

		ArrayList<House> hyphensRemoved = new ArrayList<House>();
		String possible_b7sc_1 = "", possible_b7sc_2 = "";

		if (sig.isAddress && sig.streetCodes1 != null) {
			
			//no boros, so try all housenum/streetname combos
			if (boros.size() == 0) {
				for (int boronum = 1; boronum<=5; boronum++) {
					if (sig.streetCodes1[boronum] != null) for (String b7sc : sig.streetCodes1[boronum]) {
						if (possible_b7sc_1.equals("")) possible_b7sc_1=b7sc;
						for (House h : sig.houses) {
							String code = Parser.compbl(String.valueOf(h.num) + " " + h.unit + " " + b7sc);
							if (m.addressMatch.containsKey(code)) {
								setParams(m, h, b7sc);
								return;							
							} else if (h.housenum.contains("-") && !hyphensRemoved.contains(h)) hyphensRemoved.add(h);
						}
					}							
				}
				if (hyphensRemoved.size()>0) for (int boronum = 1; boronum<=5; boronum++) {
					if (sig.streetCodes1[boronum] != null) for (String b7sc : sig.streetCodes1[boronum]) {
						for (House h : hyphensRemoved) {
							h.housenum = h.housenum.replaceAll("-", "");
							String code = Parser.compbl(h.housenum + " " + h.unit + " " + b7sc);
							if (m.addressMatch.containsKey(code)) {
								setParams(m, h, b7sc);
								return;							
							} 
						}
					}
				}
			}
			//try all housenum/streetname combos with valid boros
			else {
				for (String boro : boros) {
					int boronum = Integer.parseInt(boro);
					if (sig.streetCodes1[boronum] != null) for (String b7sc : sig.streetCodes1[boronum]) {
						if (possible_b7sc_1.equals("")) possible_b7sc_1=b7sc;
						for (House h : sig.houses) {
							String code = Parser.compbl(String.valueOf(h.num) + " " + h.unit + " " + b7sc);
							if (m.addressMatch.containsKey(code)) {
								setParams(m, h, b7sc);
								return;							
							} else if (h.housenum.contains("-") && !hyphensRemoved.contains(h)) hyphensRemoved.add(h);
						}
					}				
				}
				
				if (hyphensRemoved.size()>0) for (String boro : boros) {
					int boronum = Integer.parseInt(boro);
					if (sig.streetCodes1[boronum] != null) for (String b7sc : sig.streetCodes1[boronum]) {
						for (House h : hyphensRemoved) {
							h.housenum = h.housenum.replaceAll("-", "");
							String code = Parser.compbl(h.housenum + " " + h.unit + " " + b7sc);
							if (m.addressMatch.containsKey(code)) {
								setParams(m, h, b7sc);
								return;							
							} 
						}
					}				
				}
			}
			if (!Found) {
				if (sig.houses.size()>0) correctHouse = sig.houses.get(0).toString();
				//there arent valid addresses, but we still have street codes that could work
				if (!possible_b7sc_1.equals("")) {
					correctb7sc_1 = possible_b7sc_1;
					correctFeatureName1 = m.b7scMatch.get(correctb7sc_1);
					correctBoro = correctb7sc_1.substring(0,1);						
				} else if (boros.size()>0) correctBoro = boros.get(0);			
			}
		}
		//intersection 
		else if (sig.streetCodes1 != null && sig.streetCodes2 != null){
			//no boros, so try all housenum/streetname combos
			if (boros.size() == 0) {
				for (int boronum = 1; boronum<=5; boronum++) {
					if (sig.streetCodes1[boronum] != null) for (String b7sc_1 : sig.streetCodes1[boronum]) {
						if (possible_b7sc_1.equals("")) possible_b7sc_1=b7sc_1;
						if (sig.streetCodes2[boronum] != null) for (String b7sc_2 : sig.streetCodes2[boronum]) {
							if (possible_b7sc_2.equals("")) possible_b7sc_2=b7sc_2;
							String code = Parser.compbl(b7sc_1.substring(0,6) + b7sc_2.substring(0,6));
							if (m.intersectionMatch.containsKey(code)) {
								setParams(m, b7sc_1, b7sc_2);
								return;							
							} 
						}
					}							
				}			
			}
			else for (String boro : boros) {
				int boronum = Integer.parseInt(boro);
				if (sig.streetCodes1[boronum] != null) for (String b7sc_1 : sig.streetCodes1[boronum]) {
					if (possible_b7sc_1.equals("")) possible_b7sc_1=b7sc_1;
					if (sig.streetCodes2[boronum] != null) for (String b7sc_2 : sig.streetCodes2[boronum]) {
						if (possible_b7sc_2.equals("")) possible_b7sc_2=b7sc_2;
						String code = Parser.compbl(b7sc_1.substring(0,6) + b7sc_2.substring(0,6));
						if (m.intersectionMatch.containsKey(code)) {
							setParams(m, b7sc_1, b7sc_2);
							return;							
						} 
					}
				}							
			}
			if (!Found) {
				//there arent valid addresses, but we still have street codes that could work
				if (!possible_b7sc_1.equals("")) {
					correctb7sc_1 = possible_b7sc_1;
					correctFeatureName1 = m.b7scMatch.get(correctb7sc_1);
					correctBoro = correctb7sc_1.substring(0,1);						
				} 
				if (!possible_b7sc_2.equals("")) {
					correctb7sc_2 = possible_b7sc_2;
					correctFeatureName2 = m.b7scMatch.get(correctb7sc_2);
					correctBoro = correctb7sc_2.substring(0,1);						
				} 	
				if (correctBoro.equals("") && boros.size()>0) correctBoro = boros.get(0);	
			}			
						
			
		}

	}
		

	private void setParams(GeoMatcher m, House h, String b7sc) {
		addressID = Parser.compbl(String.valueOf(h.num) + " " + h.unit + " " + b7sc);
		correctHouse= h.toString();
		correctb7sc_1 = b7sc;
		correctFeatureName1 = m.b7scMatch.get(correctb7sc_1);
		correctBoro = correctb7sc_1.substring(0,1);		
		Found = true;		
	}
	
	private void setParams(GeoMatcher m, String b7sc_1, String b7sc_2) {
		addressID = Parser.compbl(b7sc_1.substring(0,6) + b7sc_2.substring(0,6));
		correctb7sc_1 = b7sc_1;
		correctb7sc_2 = b7sc_2;
		correctFeatureName1 = m.b7scMatch.get(correctb7sc_1);
		correctFeatureName2 = m.b7scMatch.get(correctb7sc_2);
		correctBoro = correctb7sc_1.substring(0,1);		
		Found = true;		
	}	
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeoMatcher m = new GeoMatcher();		

		ArrayList<String> boros = new ArrayList<String>();
		boros.add("3");
		GeoSignature sig = new GeoSignature(m, boros, "2617 86th STREET");
			
		GeoLocation loc = new GeoLocation(m, sig, boros);
		StdOut.println(loc.addressID);		

	}

}
