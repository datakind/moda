package Parser;
import java.util.ArrayList; 
import java.util.HashMap;
import java.lang.String;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Parser {
	
	private static Pattern pattern;
	private static Matcher matcher;
	public NameMatch StreetMatch, CityMatch;
	public HashMap<String,String> validStreets;
	private DataLookup editStreets, allStreets, allCities;
	
	public Parser(){
		editStreets = new DataLookup("/Parser/edit_streetnames.csv",",");
		StreetMatch = new NameMatch(editStreets, "boronum_g", "streetname_g");
		
		allStreets = new DataLookup("/Parser/all_streetnames.csv",",");
		validStreets = allStreets.KeyLookup("streetname_g", "streetname_g"); 		
		
		allCities = new DataLookup("/Parser/ny_cities.csv",",");
		CityMatch = new NameMatch(allCities, "name");
	}
	
	public static boolean anyalpha(String s){
		return (!s.replaceAll("[A-Za-z]","").equals(s));
	}
	
	public static boolean anydigit(String s){
		return (!s.replaceAll("[0-9]","").equals(s));
	}
	
	public static boolean notdigit(String s){
		return (!s.replaceAll("[^0-9]","").equals(s));
	}
	
	public static boolean notalpha(String s){
		return (!s.replaceAll("[^A-Za-z]","").equals(s));
	}
	
	public static String cleanStreet(String s){

		String t;
		String[] spl;
		In file;
		
		s = s.trim().toUpperCase();
		
		s = s.replaceAll("(^)(CORNER OF|CORNER|BETWEEN|BTWN)(\\b)", "");
		
		// remove apartment
		pattern = Pattern.compile("\\s#\\s*\\d");
		matcher = pattern.matcher(s);	
		if (matcher.find() && (matcher.start() > 2)) s = s.substring(0,matcher.start());
		
		s=compbl(s.replaceAll("[^A-Za-z0-9\\s]", " ")).trim();

		// replace st with saint where appropriate 
		file = new In("/Parser/saints.csv");
		while (!file.isEmpty() && s.indexOf("ST")>=0) {
            t = file.readLine();
            s = s.replaceAll("(\\b|^)ST " + t, "SAINT " + t);
        }		
		file.close();
		
		// expand directional prefix where appropriate
		if (s.indexOf("AVE E")<0 && s.indexOf("AVENUE E")<0) s = s.replaceAll("(\\b|^)E(\\b|$)", "EAST");
		if (s.indexOf("AVE W")<0 && s.indexOf("AVENUE W")<0) s = s.replaceAll("(\\b|^)W(\\b|$)", "WEST");
		if (s.indexOf("AVE N")<0 && s.indexOf("AVENUE N")<0) s = s.replaceAll("(\\b|^)N(\\b|$)", "NORTH");
		if (s.indexOf("AVE S")<0 && s.indexOf("AVENUE S")<0) s = s.replaceAll("(\\b|^)S(\\b|$)", "SOUTH");
		
		// fix 1 ST AVE etc
		s = s.replaceAll("1ST(\\b|$)", "1");
		s = s.replaceAll("2\\s*ND(\\b|$)", "2");
		s = s.replaceAll("3RD(\\b|$)", "3");
		s = s.replaceAll("(?<=[0456789]\\s{0,1})TH(\\b|$)", "");
		
		//fix beech 113 st
		s = s.replaceAll("BEECH(?=\\s+\\d+)","BEACH");
		
		// make sure beach street has suffix
		if (s.replaceAll("(\\b|^)ST","").equals(s) && s.indexOf("BEACH")>=0) {
			pattern = Pattern.compile("(BEACH|BEECH|B)\\s+\\d+\\s+");
			matcher = pattern.matcher(s);		
			if (matcher.find()) s = s.substring(0,matcher.end()) + "STREET" + s.substring(matcher.end()+1);
		}	
		
		// replace text numbers, e.g. FIRST with numerics in streetname
		String[] numberwords = "FIRST SECOND THIRD FOURTH FIFTH SIXTH SEVENTH EIGHTH NINTH TENTH ELEVENTH TWELFTH".split(" ");
		for (int i=0; i<numberwords.length; i++) s = s.replaceAll("(\\b|^)" + numberwords[i], Integer.toString(i+1) + " ");
		s = compbl(s);

		// expand suffixes
		file = new In("/Parser/suffixes.csv");
		while (!file.isEmpty()) {
            t = file.readLine();
            spl = t.split(",");
            s = s.replaceAll("(\\b|^)" + spl[1] + "(\\b|$)", spl[0]);
        }	
		file.close();
		
		// expand unit aliases
		file = new In("/Parser/units.csv");
		while (!file.isEmpty()) {
            t = file.readLine();
            spl = t.split(",");
            s = s.replaceAll("(\\b|^)" + spl[1] + "(\\b|$)", spl[0]);
        }
		file.close();
		
		// change misspellings
		file = new In("/Parser/misspellings.csv");
		while (!file.isEmpty()) {
            t = file.readLine();
            spl = t.split(",");
            s = s.replaceAll("(\\b|^)" + spl[1] + "(\\b|$)", spl[0]);
        }		
		file.close();
		
		//remove ordinal suffix
		s = s.replaceAll("(?<=\\d)(ST|ND|RD|TH)(\\b|$)", "");
		
		//remove apartment junk
		s = s.replaceAll("(APARTMENT|FLOOR|UNIT|AKA|ROOM|OFFICE|BASEMENT|SUITE)[A-Za-z\\d\\s]*","");
		return s.trim();
	}
	
	//collapse double blanks
	public static String compbl(String s){
		while (s.indexOf("  ") >= 0) {
			s=s.replaceAll("  "," ");
		}
		return s;
	}
		
	public static void main(String[] args) {
		Parser p = new Parser();
		String addy = "HOYT AVENUE NORTH & 23rd STREET";
		Intersector dividor = new Intersector(addy);
		ArrayList<String> boros = new ArrayList<String>();
		boros.add("4");
		if (dividor.exists) {
			String str1 = addy.substring(0, dividor.startPos-1);
			String str2 = addy.substring(dividor.endPos+1);
			Intersection i = new Intersection(p, boros, str1, str2);
			StdOut.println(i.street1.toString());
			StdOut.println(i.street2.toString());
		}
		else {
			Address a = new Address(p, boros, addy);
			StdOut.println(a.houses.toString());
			StdOut.println(a.street.toString());
		}
/*
		// parse houses 
		ArrayList<House> test = House.Houses("105-107 104a");
		for (House house : test) {
			StdOut.println(house.toString());
		}
		*/
	}

}
