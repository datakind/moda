package Parser;


import java.util.ArrayList; 
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Street {
	
	public String direction = "", name ="", suffix ="";
	private static Pattern pattern;
	private static Matcher matcher;
	
	public Street(String direction, String name, String suffix) {
		this.direction=direction;
		this.name=name;
		this.suffix=suffix;
	}
	
	//takes a cleaned street sequence and separates into components
	//does extra cleaning by making assumptions about components
	//also performs spellcheck on name portion of street
	public Street(Parser p, ArrayList<String> boros, String s) {
		name = Parser.compbl(s.trim()).toUpperCase();
		
		if (name.length()==0) return;
		
		//where a space obviously needs to be inserted
		name = name.replaceAll("(?<=\\S)(?=(STREET|AVENUE|ROAD|BOULEVARD))", " ");
		
		String[] spl;
		spl = name.split(" ");
		int nchunks = spl.length;
		
		//directional prefix
		if (spl[0].equals("EAST") || spl[0].equals("WEST") || spl[0].equals("NORTH") || spl[0].equals("SOUTH")) {
			direction = spl[0];
			name = name.substring(direction.length()).trim();
			spl = name.split(" ");
		} else if (spl[nchunks-1].equals("EAST") || spl[nchunks-1].equals("WEST") || spl[nchunks-1].equals("NORTH") || spl[nchunks-1].equals("SOUTH")) {
			direction = spl[nchunks-1];
			name = name.substring(0,name.length() - direction.length());
			spl = name.split(" ");
		}
		
		//sometimes there is stuff at the end and we want to get rid of it
		pattern = Pattern.compile("(?<=STREET )(EXTENSION|DRIVE|LANDING|AVENUE|BRANCH|PLAZA|BOULEVARD|COURT)(\\b|$)");
		matcher = pattern.matcher(name);			
		if (matcher.find()) {
			suffix = matcher.group(0);
			name = name.substring(0,matcher.start());
		} else {
			pattern = Pattern.compile("STREET ");
			matcher = pattern.matcher(name);	
			if (matcher.find()) {
				suffix = matcher.group(0);
				name = name.substring(0,matcher.start());
			} 			
		}
		
		//find suffix and pull it out
		search:
			if (suffix.length()==0) for (int i=spl.length-1; i>=0; i--){
				In file = new In("/Parser/unique_suffixes.csv");
				while(!file.isEmpty()) {
					String k = file.readLine().trim().toUpperCase();
					if (k.equals(spl[i].trim())) {
						suffix = spl[i];
						if (i == 0) name = name.substring(suffix.length()).trim();
						else name = name.substring(0, name.indexOf(suffix));
						break search;
					}
				}
			}
		
		// beach 111
		if (suffix.equals("BEACH") && !Parser.anyalpha(name)) {
			name = suffix + " " + name;
			suffix = "STREET";
			direction = "";
		}
		
		// something that looks like directional prefix or suffix is actually the streename
		if (name.equals("")){
			if (!direction.equals("")) {
				name = direction;
				direction = "";
			} else if (!suffix.equals("")) {
				name = suffix;
				suffix = "";
			}
		}
		
		name = name.replaceAll(" ", "").trim();
		
		// look for fuzzy matches on name portion
		if (!p.validStreets.containsKey(name)) {
			String shortname = name;
			String bestMatch = p.StreetMatch.BestMatch(boros, shortname).match;
			while (shortname.length()>6 && bestMatch.equals("")) {
				shortname = shortname.substring(0,shortname.length()-1).trim();
				bestMatch = p.StreetMatch.BestMatch(boros, shortname).match;
			}
			if (!bestMatch.equals("")) this.name = bestMatch;		
		}
		
		direction = direction.trim();
		name = name.replaceAll(" ", "").trim();
		suffix = suffix.trim();

		//StdOut.println(direction);
		//StdOut.println(name);
		//StdOut.println(suffix);
	}
	
	public String toString(){
		return (direction + " " + name + " " + suffix).trim();
	}		
	
	public static void main(String[] args){
		Parser p = new Parser();
		String st = "Paerdegat 10street";
		Street s = new Street(p, new ArrayList<String>(Arrays.asList("3")), st);
		StdOut.println(s.direction);
		StdOut.println(s.name);
		StdOut.println(s.suffix);
	}	
}