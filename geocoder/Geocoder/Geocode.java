package Geocoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.File;
import java.util.HashMap;
import Parser.DataLookup;
import Parser.In;
import Parser.Out;
import Parser.Parser;
import Parser.StdOut;
import Parser.StdIn;
public class Geocode {
	
	public int count_obs=0, count_geomatch=0, count_x_y=0, count_bin=0, count_bbl=0, count_feature_name_match=0;
	
	private static boolean isIntersection(String id){
		return id.length()==12 && !id.contains(" ");
	}
	public Geocode( String filenameIn, String filenameOut, String splitchar, String houseNumberVar, String streetNameVar, 
					String zipCodeVar, String boroVar, String townVar) {
		final double start = System.currentTimeMillis();
		int N_args = 0;
		double percent;
		DecimalFormat df = new DecimalFormat("#.0");
		
		if (filenameOut.equals("temp.txt")) throw new RuntimeException("You cannot name the outfile temp.txt, as this name is already in use.");
		else if (filenameOut.equals("")) filenameOut = filenameIn;
		File tfile = new File(filenameIn);
		if (tfile.exists()==false) throw new RuntimeException(filenameIn + " does not exist.");
		
		String rawLine = "", zipCode = "", Borough ="", City="", addr="", building="", stname="";
		Out outFile;
		
		int hIndex =-1, sIndex=-1, zIndex=-1, bIndex=-1, cIndex=-1;

		DataLookup f = new DataLookup(filenameIn,splitchar);
		
		hIndex = f.varNum(houseNumberVar);
		sIndex = f.varNum(streetNameVar);
		zIndex = f.varNum(zipCodeVar);
		bIndex = f.varNum(boroVar);
		cIndex = f.varNum(townVar);
		
		if (hIndex<0 && houseNumberVar.length()>0) {
			StdOut.println("ERROR: " + houseNumberVar + " not on input file.");
			return;
		}
		if (sIndex<0 && streetNameVar.length()>0) {
			StdOut.println("ERROR: " + streetNameVar + " not on input file.");
			return;
		}
		if (zIndex<0 && zipCodeVar.length()>0) {
			StdOut.println("ERROR: " + zipCodeVar + " not on input file.");
			return;
		}
		if (bIndex<0 && boroVar.length()>0) {
			StdOut.println("ERROR: " + boroVar + " not on input file.");
			return;
		}
		if (cIndex<0 && townVar.length()>0) {
			StdOut.println("ERROR: " + townVar + " not on input file.");
			return;
		}
		if (hIndex<0 && sIndex<0) {
			StdOut.println("ERROR: Neither house number nor street name specified");
			return;
		}

		if (houseNumberVar.length()>0) {
			StdOut.println("House number var: " + houseNumberVar);
			N_args++;
		}
		if (streetNameVar.length()>0) {
			StdOut.println("Street name var: " + streetNameVar);
			N_args++;
		}
		if (zipCodeVar.length()>0) {
			StdOut.println("ZIP var: " + zipCodeVar);
			N_args++;
		}
		if (boroVar.length()>0) {
			StdOut.println("BORO var: " + boroVar);
			N_args++;
		}
		if (townVar.length()>0) {
			StdOut.println("Town/City/Neighborhood var: " + townVar);
			N_args++;
		}
		In file;
		File tempfile;
		tempfile =new File("temp.txt");

		file = new In(filenameIn);
		rawLine = file.readLine();	

		outFile = new Out(tempfile.getAbsolutePath());		
		outFile.println("addressid" + splitchar + rawLine + splitchar + "parsed_house_number" + splitchar + "parsed_feature_name1" 
				+ splitchar + "parsed_feature_name2" + splitchar + "parsed_boro");
		
		String[] line = rawLine.split(splitchar);
		
		if (line.length < N_args) {
			StdOut.println("ERROR: '" + splitchar + "' does not appear to be the correct delimiter for input file " + filenameIn);
			file.close();
			return;
		}
		
		StdOut.println("Loading data...");
		
		//declare only one geomatcher because of the overhead associated
		GeoMatcher m = new GeoMatcher();
		
		StdOut.println("Geocode in progress...");
		
		//loop to assign address ID
		while (!file.isEmpty()){
			
			rawLine = file.readLine();	
			line = rawLine.split(Pattern.quote(splitchar));
			
			zipCode = "";
			Borough = "";
			City = "";
			building = "";
			stname = "";
			addr = "";
			
			if (zIndex>=0 && zIndex<line.length) zipCode = line[zIndex].replaceAll("[^0-9]","");
			if (bIndex>=0 && bIndex<line.length) Borough = Parser.compbl(line[bIndex].replaceAll("[^0-9A-Za-z\\s]","").toUpperCase().trim());
			if (cIndex>=0 && cIndex<line.length) City = Parser.compbl(line[cIndex].replaceAll("[^A-Za-z\\s]","").toUpperCase().trim());
			
			//get possible boros from any combination of 3 inputs
			ArrayList<String> boros = m.getBoros(Borough, zipCode, City);
			
			GeoSignature sig;
			if (hIndex>=0 && sIndex>=0 && hIndex !=sIndex) {
				if (hIndex<line.length) building = line[hIndex];
				if (sIndex<line.length) stname = line[sIndex];
				sig = new GeoSignature(m, boros, building, stname);		
			}
			else {
				if (hIndex>=0 && hIndex<line.length) addr = line[hIndex];
				else if (sIndex>=0 && sIndex<line.length) addr = line[sIndex];
				//get components of geo entry
				sig = new GeoSignature(m, boros, addr);				
			}

			//assign legit identifiers
			GeoLocation loc = new GeoLocation(m, sig, boros);
			
			outFile.println(loc.addressID + splitchar + rawLine + splitchar + loc.correctHouse + splitchar + loc.correctFeatureName1 + 
					splitchar + loc.correctFeatureName2 + splitchar + loc.correctBoro);
			
			count_obs++;
			if (loc.addressID.length()>0) count_geomatch++;
			if (loc.correctFeatureName1.length()>0 && (sig.isAddress || loc.correctFeatureName2.length()>0)) this.count_feature_name_match++;
		}
		percent = 100* count_geomatch/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to valid geographic locations");
		percent = 100*count_feature_name_match/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to valid feature names");		
		outFile.close();
		file.close();
		m.close(); //close the geomatcher, which has a lot of overhead
    	
		
		//hashes for intersections and addresses
		HashMap<String,String> address_x_y = DataLookup.KeyLookup("/Geocoder/valid_addresses_x_y.csv",",","address", "x_y");
		HashMap<String,String> intersection_x_y = DataLookup.KeyLookup("/Geocoder/lion_intersections.csv",",","intersection", "x_y");
		file = new In("temp.txt");
		outFile = new Out(filenameOut);
		rawLine = file.readLine();
		outFile.println(rawLine + splitchar + "x" + splitchar + "y");		
		String[] x_y = new String[2];
		//loop to assign X-Y coords
		while(!file.isEmpty()) {
			String chunk = "", x="", y="";
			rawLine = file.readLine();
			String addressID = rawLine.substring(0, rawLine.indexOf(splitchar));
			if (isIntersection(addressID)) chunk = intersection_x_y.get(addressID);
			else if (addressID.length()>0) chunk = address_x_y.get(addressID);
			x_y = chunk.split(Pattern.quote("|"));
			if (x_y.length==2) {
				x=x_y[0];
				y=x_y[1];
			}
			outFile.println(rawLine + splitchar +x + splitchar + y);	
			if (x.length()>0) count_x_y++;
		}
		
		percent = 100*count_x_y/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to X-Y coordinates");			
		address_x_y = null;
		intersection_x_y = null;
		
		file = new In(filenameOut);
		outFile = new Out("temp.txt");
		rawLine = file.readLine();
		outFile.println(rawLine + splitchar + "bin");	
		HashMap<String,String> address_bin = DataLookup.KeyLookup("/Geocoder/valid_addresses_bin.csv",",","address", "bin");
		//loop to assign bins
		while(!file.isEmpty()) {
			rawLine = file.readLine();
			String addressID = rawLine.substring(0, rawLine.indexOf(splitchar));
			String imputed_bin = "";
			if (isIntersection(addressID)) imputed_bin=""; //intersections dont get bin....yet.
			else if (addressID.length()>0) imputed_bin = address_bin.get(addressID);
			outFile.println(rawLine + splitchar + imputed_bin);	
			if (imputed_bin.length()>0) count_bin++;
		}
		percent = 100*count_bin/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to BINs");	
		outFile.close();
		file.close();		
		address_bin = null;		
		
		
		file = new In("temp.txt");
		outFile = new Out(filenameOut);
		rawLine = file.readLine();
		outFile.println(rawLine + splitchar + "bbl");		
		HashMap<String,String> address_bbl = DataLookup.KeyLookup("/Geocoder/valid_addresses_bbl.csv",",","address", "bbl");
		while(!file.isEmpty()) {
			rawLine = file.readLine();
			String addressID = rawLine.substring(0, rawLine.indexOf(splitchar));
			String imputed_bbl = "";
			if (isIntersection(addressID)) imputed_bbl ="";
			else if (addressID.length()>0) imputed_bbl = address_bbl.get(addressID);
			outFile.println(rawLine + splitchar + imputed_bbl);			
			if (imputed_bbl.length()>0) count_bbl++;
		}
		percent = 100*count_bbl/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to BBLs");		
		outFile.close();
		file.close();		
		address_bbl = null;		
		
		tfile = new File("temp.txt");
		tfile.delete();
		
		final double end = System.currentTimeMillis();
		double minutes = ((double)(end - start))/60000;
		System.out.println("Geocode completed after " + df.format(minutes) + " minutes.");
	}

	public static void main(String[] args) {
		/*
		Geocode g = new Geocode("F:/SAS Temporary Files/for_geocode.txt",
				"", "|", 
				"ADDRESS", "ADDRESS", "zip_code", "BORO", "");		


		
		Geocode g = new Geocode("Z:\\Hurricane Sandy\\FEMA 1126\\2013_03_22_0700_FEMA_Regs_NYC_test.txt", 
								"Z:\\Hurricane Sandy\\FEMA 1126\\my glorious test.txt", "|",
						"damaged_street", "damaged_street", "damaged_zip", "", "damaged_city");
		*/
		if (args.length==0) {
			//interactive call
			StdOut.println("What is the full path of the file to geocode? ");
			String infile = StdIn.readLine().replace("\\", "/");		
			File inp = new File(infile);
			while (inp.exists()==false) {
				StdOut.println("File does not exist. Re-enter full file path");
				infile = StdIn.readLine().replace("\\", "/");		
				inp = new File(infile);			
			}
			StdOut.println("Do you want to create a new file with the output? Enter the full path of the new file to create:");
			String outfile = StdIn.readLine().replace("\\", "/");		
			StdOut.println("What is the delimiter for the input file?");
			String delim = StdIn.readLine();			
			StdOut.println("Which variable has the building number for the address?");
			String housenum = StdIn.readLine();		
			StdOut.println("Which variable has the street name for the address? (Can be the same column as building number)");
			String stname = StdIn.readLine();				
			StdOut.println("Is there a ZIP code variable? Enter it here, otherwise blank:");
			String zip = StdIn.readLine();			
			StdOut.println("Is there a standardized BORO code/name variable? Enter it here, otherwise blank:");
			String boro = StdIn.readLine();				
			StdOut.println("Is there a neighborhood/town/city variable? Enter it here, otherwise blank:");
			String city = StdIn.readLine();				
			Geocode g = new Geocode(infile,outfile,delim,housenum, stname, zip, boro, city);	
			return;
		}
		
		else if (args.length != 8) {
			StdOut.println("ERROR: All parameters must be specified in the following order:");
			StdOut.println("Infile, Outfile, Delimiter, HouseNum Var, Street Name Var, Zipcode Var, Boro Var, Town/Neighborhood Var");
			StdOut.println("If any of the above parameters are not applicable, specify blank quotes.");
			return;
		}
		
		String infile = args[0].replace("\\", "/");		
		String outfile = args[1].replace("\\", "/");		
		String delim = args[2];			
		String housenum = args[3];
		String stname = args[4];				
		String zip = args[5];			
		String boro = args[6];		
		String city = args[7];	
		Geocode g = new Geocode(infile,outfile,delim,housenum, stname, zip, boro, city);		

	}
}
