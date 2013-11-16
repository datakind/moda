package Geocoder;
import Parser.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

public class GeoMatcher {
	
	public HashMap<String, ArrayList<String>[]> streetMatch;
	public HashMap<String, String> zipMatch, neighborhoodMatch, b7scMatch, addressMatch, intersectionMatch;
	public Parser parser;
	
	public GeoMatcher() {

		//for checking the validity of addresses
		addressMatch = DataLookup.KeyLookup("/Geocoder/valid_addresses_.csv",",","address", "");
		
		//for checking the validity of intersections
		intersectionMatch = DataLookup.KeyLookup("/Geocoder/lion_intersections.csv",",","intersection", "");
		
		ArrayList<String>[] b7sc_lists;
		//for looking up street codes
		try {
			BufferedReader streetFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/Geocoder/all_combos_b7sc.csv")));
			streetMatch = new HashMap<String, ArrayList<String>[]>();
	        String line = streetFile.readLine();
	        while ((line = streetFile.readLine()) != null) {
		        	
		        //read and parse data lines
		        String[] data = line.split(Pattern.quote(","));
		            
		        String name = data[0]; 
		        String b7sc = data[1];
		        int boro = Integer.parseInt(b7sc.substring(0,1));
		        
		        //add values to key
		        if (!streetMatch.containsKey(name))  b7sc_lists = new ArrayList[6]; 
		        else b7sc_lists = streetMatch.get(name); 
		        if (b7sc_lists[boro] == null) b7sc_lists[boro] = new ArrayList<String>();
		        if (!b7sc_lists[boro].contains(b7sc)) {
		        	b7sc_lists[boro].add(b7sc);
		        	streetMatch.put(name, b7sc_lists);
		        }
	        } 
	        streetFile.close();
		} catch(Exception e) {System.out.print(e);}		

		//for looking up boros by zip code
		zipMatch = DataLookup.KeyLookup("/Geocoder/zip_to_boro.csv",",","zip", "boronum");
		
		//for looking up boros by neighborhood
		neighborhoodMatch = DataLookup.KeyLookup("/Geocoder/neighborhood_to_boro.csv",",","name", "boronum");
				
		//for looking up feature name of street
		b7scMatch = DataLookup.KeyLookup("/Geocoder/stname_b7sc.csv",",","b7sc", "feature_name");
		
		parser = new Parser();
	}
	
	public void close() {
		streetMatch = null;
		b7scMatch = null;
		intersectionMatch = null;
		addressMatch = null;
		neighborhoodMatch = null;
		zipMatch = null;
	}
	
	public ArrayList<String> getBoros(String Borough, String zipCode, String City) {
		String boro = "";
		Integer boronum;
		ArrayList<String> boros = new ArrayList<String>();
		Borough = Borough.trim().toUpperCase();
		City = Parser.compbl(City.trim().toUpperCase());
		
		//get boro from zip
		if (zipCode.length()>0) {
			if (zipCode.length()==5 && zipMatch.containsKey(zipCode)) {
				boro = zipMatch.get(zipCode);
				if (boro.length()>0 && !Parser.notdigit(boro)) boros.add(boro);
			}
		}
		//get boro from boro field
		if (Borough.length()>0) {
			if (!Parser.notdigit(Borough)) {
				boronum = Integer.parseInt(Borough);
				if (Arrays.asList(1,2,3,4,5).contains(boronum) && !boros.contains(Borough)) boros.add(Borough);
			} 
			else if (Arrays.asList("MANHATTAN","NEW YORK","MN","BRONX","BX","BROOKLYN","KINGS","BK","QUEENS","QN","STATEN ISLAND","RICHMOND","SI").contains(Borough)) {
				boronum = Math.max(Arrays.asList("MANHATTAN","BRONX","BROOKLYN","QUEENS","STATEN ISLAND").indexOf(Borough) + 1, 
						Math.max(Arrays.asList("NEW YORK","BRONX","KINGS","QUEENS","RICHMOND").indexOf(Borough) + 1,
						Arrays.asList("MN","BX","BK","QN","SI").indexOf(Borough) + 1));
				if (!boros.contains(String.valueOf(boronum))) boros.add(String.valueOf(boronum));
			}
		}
		//get boro from city field
		if (City.length()>0) {
			if (neighborhoodMatch.containsKey(City)) {
				boro = neighborhoodMatch.get(City);
				if (boro.length()>0 && !Parser.notdigit(boro)) {
					if (!boros.contains(boro)) boros.add(boro);
				}
			} else {
				String bestMatch = parser.CityMatch.BestMatch(City).match;
				if (bestMatch.length()>0 && neighborhoodMatch.containsKey(bestMatch)) {
					boro = neighborhoodMatch.get(bestMatch);
					if (boro.length()>0 && !Parser.notdigit(boro)) {
						if (!boros.contains(boro)) boros.add(boro);
					}					
				}
			}
		}		
		
		return boros;
	}
	
	
	public static void main(String[] args) {
		
		String rawLine;
		
		int hIndex =-1, sIndex=-1, zIndex=-1, bIndex=-1, cIndex=-1;
		
		String filename = "z:\\Hurricane Sandy\\Dataset for Contractor Teams\\Rapid Repair signups 02FEB2013 16.00.csv";
		//String filename = "Z:\\Hurricane Sandy\\FEMA 1126\\2013_05_22_0700_FEMA_Regs_NYC.txt";
		String splitchar = ",";
		DataLookup f = new DataLookup(filename,splitchar);
		
		hIndex = f.varNum("BuildingNumber");
		sIndex = f.varNum("StreetName");
		zIndex = f.varNum("ZipCode");
		bIndex = f.varNum("Borough");
		cIndex = f.varNum("City");

		int found=0, notfound=0;
		/*
		hIndex = f.varNum("damaged_street");
		sIndex = f.varNum("damaged_street");
		zIndex = f.varNum("damaged_zip");
		cIndex = f.varNum("damaged_city");
		*/
		
		if (hIndex<0 && sIndex<0) throw new RuntimeException("house number and street name fields are not valid");
		
		String zipCode = "", Borough ="", City="", addr="";

		//declare only one geomatcher because of the overhead associated
		GeoMatcher m = new GeoMatcher();

		//read in file
		In file = new In(filename);
		rawLine = file.readLine();
		String[] line = rawLine.split(splitchar);
		while (!file.isEmpty()){
			
			rawLine = file.readLine();
			line = rawLine.split(Pattern.quote(splitchar));
			
			if (zIndex>=0) zipCode = line[zIndex].replaceAll("[^0-9]","");
			if (bIndex>=0) Borough = Parser.compbl(line[bIndex].replaceAll("[^0-9A-Za-z\\s]","").toUpperCase().trim());
			if (cIndex>=0) City = Parser.compbl(line[cIndex].replaceAll("[^A-Za-z\\s]","").toUpperCase().trim());
			if (hIndex>=0 && sIndex>=0 && hIndex !=sIndex) addr = line[hIndex] + " " + line[sIndex];
			else if (hIndex>=0) addr = line[hIndex];
			else if (sIndex>=0) addr = line[sIndex];
			
			ArrayList<String> boros = m.getBoros(Borough, zipCode, City);

			//get components of geo entry
			GeoSignature sig = new GeoSignature(m, boros, addr);
			
			//assign legit identifiers
			GeoLocation loc = new GeoLocation(m, sig, boros);
			
			if (loc.Found) found++;
			else {
				notfound++;
				//if (!line[0].trim().equals(".")) StdOut.println(sig.toString());
				StdOut.println(sig.toString());
			}
		}
		
		StdOut.println("not found:" + notfound + " found:" + found);

	}

}
