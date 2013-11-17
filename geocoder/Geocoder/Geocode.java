package Geocoder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import Parser.DataLookup;
import Parser.In;
import Parser.Out;
import Parser.Parser;
import Parser.StdIn;
import Parser.StdOut;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
public class Geocode {
	
	public int count_obs=0, count_geomatch=0, count_x_y=0, count_bin=0, count_bbl=0, count_feature_name_match=0;
	
	private static boolean isIntersection(String id){
		return id.length()==12 && !id.contains(" ");
	}

	/**
	 * 
	 * @param inputFilePath
	 * @param outputFilePath If <code>null</code>, the output will overwrite
	 *   the input file.
	 * @param logFilePath
	 * @param unmatchedAddressFilePath
	 * @param columnCharDelimiter
	 * @param columnNameForBuildingNumber
	 * @param columnNameForStreetName
	 * @param columnNameForZipCode
	 * @param columnNameForBorough
	 * @param columnNameForCity
	 */
	public Geocode(String inputFilePath, String outputFilePath,
	        String logFilePath, String unmatchedAddressFilePath,
	        String columnCharDelimiter,
	        String columnNameForBuildingNumber, String columnNameForStreetName,
	        String columnNameForZipCode, String columnNameForBorough,
	        String columnNameForCity) {
		final double start = System.currentTimeMillis();
		double percent;
		DecimalFormat df = new DecimalFormat("#.0");

		// TODO Just make the temp-file an extra parameter?
		if (inputFilePath.equals("temp.txt")
		        || outputFilePath.equals("temp.txt")
		        || logFilePath.equals("temp.txt")
		        || unmatchedAddressFilePath.equals("temp.txt")) {
		    throw new RuntimeException("You cannot name any of the output files 'temp.txt', as this name is already in use.");
		}

		if (outputFilePath.equals("")) {
		    outputFilePath = inputFilePath;
		}

		File inputFile = new File(inputFilePath);
		if (!inputFile.exists()) {
		    throw new RuntimeException("The input file '" + inputFilePath + "' does not exist.");
		}

		String rawLine = "", zipCode = "", Borough ="",
		        City="", addr="", building="", stname="";
		Out outFile;

		int indexOfBuildingNumberColumn = -1,
		        indexOfStreetNumberColumn = -1,
		        indexOfZipCodeColumn = -1,
		        indexOfBoroughColumn = -1,
		        indexOfCityColumn = -1;

		DataLookup dataLookup = new DataLookup(inputFilePath,columnCharDelimiter);
		
		indexOfBuildingNumberColumn = dataLookup.varNum(columnNameForBuildingNumber);
		indexOfStreetNumberColumn = dataLookup.varNum(columnNameForStreetName);
		indexOfZipCodeColumn = dataLookup.varNum(columnNameForZipCode);
		indexOfBoroughColumn = dataLookup.varNum(columnNameForBorough);
		indexOfCityColumn = dataLookup.varNum(columnNameForCity);
		
		if (indexOfBuildingNumberColumn<0 && columnNameForBuildingNumber.length()>0) {
			StdOut.println("ERROR: " + columnNameForBuildingNumber + " not on input file.");
			return;
		}
		if (indexOfStreetNumberColumn<0 && columnNameForStreetName.length()>0) {
			StdOut.println("ERROR: " + columnNameForStreetName + " not on input file.");
			return;
		}
		if (indexOfZipCodeColumn<0 && columnNameForZipCode.length()>0) {
			StdOut.println("ERROR: " + columnNameForZipCode + " not on input file.");
			return;
		}
		if (indexOfBoroughColumn<0 && columnNameForBorough.length()>0) {
			StdOut.println("ERROR: " + columnNameForBorough + " not on input file.");
			return;
		}
		if (indexOfCityColumn<0 && columnNameForCity.length()>0) {
			StdOut.println("ERROR: " + columnNameForCity + " not on input file.");
			return;
		}
		if (indexOfBuildingNumberColumn<0 && indexOfStreetNumberColumn<0) {
			StdOut.println("ERROR: Neither house number nor street name specified");
			return;
		}

	    int N_args = 0;
		if (columnNameForBuildingNumber.length()>0) {
			StdOut.println("House number var: " + columnNameForBuildingNumber);
			N_args++;
		}
		if (columnNameForStreetName.length()>0) {
			StdOut.println("Street name var: " + columnNameForStreetName);
			N_args++;
		}
		if (columnNameForZipCode.length()>0) {
			StdOut.println("ZIP var: " + columnNameForZipCode);
			N_args++;
		}
		if (columnNameForBorough.length()>0) {
			StdOut.println("BORO var: " + columnNameForBorough);
			N_args++;
		}
		if (columnNameForCity.length()>0) {
			StdOut.println("Town/City/Neighborhood var: " + columnNameForCity);
			N_args++;
		}

		In file;
		File tempfile;
		tempfile =new File("temp.txt");

		file = new In(inputFilePath);
		rawLine = file.readLine();	

		outFile = new Out(tempfile.getAbsolutePath());		
		outFile.println("addressid"
		        + columnCharDelimiter + rawLine
		        + columnCharDelimiter + "parsed_house_number"
		        + columnCharDelimiter + "parsed_feature_name1" 
				+ columnCharDelimiter + "parsed_feature_name2"
		        + columnCharDelimiter + "parsed_boro");

		String[] line = rawLine.split(columnCharDelimiter);

		if (line.length < N_args) {
			StdOut.println("ERROR: '" + columnCharDelimiter + "' does not appear to be the correct delimiter for input file " + inputFilePath);
			file.close();
			return;
		}
		
		StdOut.println("Loading data...");
		
		//declare only one geomatcher because of the overhead associated
		GeoMatcher m = new GeoMatcher();
		
		StdOut.println("Geocode in progress...");
		
		//loop to assign address ID
		Reader reader = null;
        try {
            File dataFile = new File(inputFilePath);
            if (file.exists()) {
                reader = new FileReader(dataFile);
            } else {
                URL url = getClass().getResource(inputFilePath);

                reader = new InputStreamReader(url.openStream());
            }

            // TODO Header-extraction, and column-index analysis, should be done here

            CSVReader<String[]> csvParser = new CSVReaderBuilder<String[]>(reader)
                    .strategy(new CSVStrategy(columnCharDelimiter.charAt(0), '\"', '#', false, true))
                    .entryParser(new CSVEntryParser<String[]>() {
                        public String[] parseEntry(String... data) { return data; }
                    })
                    .build();
            while ((line = csvParser.readNext()) != null) {

                zipCode = "";
                Borough = "";
                City = "";
                building = "";
                stname = "";
                addr = "";
                
                if (indexOfZipCodeColumn>=0 && indexOfZipCodeColumn<line.length) {
                    zipCode = line[indexOfZipCodeColumn].replaceAll("[^0-9]","");
                }
                if (indexOfBoroughColumn>=0 && indexOfBoroughColumn<line.length) {
                    Borough = Parser.compbl(line[indexOfBoroughColumn].replaceAll("[^0-9A-Za-z\\s]","").toUpperCase().trim());
                }
                if (indexOfCityColumn>=0 && indexOfCityColumn<line.length) {
                    City = Parser.compbl(line[indexOfCityColumn].replaceAll("[^A-Za-z\\s]","").toUpperCase().trim());
                }

                //get possible boros from any combination of 3 inputs
                ArrayList<String> boros = m.getBoros(Borough, zipCode, City);

                GeoSignature sig;
                if (indexOfBuildingNumberColumn>=0 && indexOfStreetNumberColumn>=0 && indexOfBuildingNumberColumn !=indexOfStreetNumberColumn) {
                    if (indexOfBuildingNumberColumn<line.length) {
                        building = line[indexOfBuildingNumberColumn];
                    }
                    if (indexOfStreetNumberColumn<line.length) {
                        stname = line[indexOfStreetNumberColumn];
                    }
                    sig = new GeoSignature(m, boros, building, stname);     
                }
                else {
                    if (indexOfBuildingNumberColumn>=0 && indexOfBuildingNumberColumn<line.length) {
                        addr = line[indexOfBuildingNumberColumn];
                    }
                    else if (indexOfStreetNumberColumn>=0 && indexOfStreetNumberColumn<line.length) {
                        addr = line[indexOfStreetNumberColumn];
                    }
                    //get components of geo entry
                    sig = new GeoSignature(m, boros, addr);             
                }

                //assign legit identifiers
                GeoLocation loc = new GeoLocation(m, sig, boros);

                outFile.println(loc.addressID + columnCharDelimiter + rawLine
                        + columnCharDelimiter + loc.correctHouse
                        + columnCharDelimiter + loc.correctFeatureName1
                        + columnCharDelimiter + loc.correctFeatureName2
                        + columnCharDelimiter + loc.correctBoro);

                count_obs++;
                if (loc.addressID.length()>0) {
                    count_geomatch++;
                }
                if (loc.correctFeatureName1.length() > 0
                        && (sig.isAddress || loc.correctFeatureName2.length() > 0)) {
                    this.count_feature_name_match++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try { reader.close(); } catch (Exception e) { }
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
		outFile = new Out(outputFilePath);
		rawLine = file.readLine();
		outFile.println(rawLine + columnCharDelimiter + "x" + columnCharDelimiter + "y");		
		String[] x_y = new String[2];
		//loop to assign X-Y coords
		while(!file.isEmpty()) {
			String chunk = "", x="", y="";
			rawLine = file.readLine();
			String addressID = rawLine.substring(0, rawLine.indexOf(columnCharDelimiter));
			if (isIntersection(addressID)) chunk = intersection_x_y.get(addressID);
			else if (addressID.length()>0) chunk = address_x_y.get(addressID);
			x_y = chunk.split(Pattern.quote("|"));
			if (x_y.length==2) {
				x=x_y[0];
				y=x_y[1];
			}
			outFile.println(rawLine + columnCharDelimiter +x + columnCharDelimiter + y);	
			if (x.length()>0) count_x_y++;
		}
		
		percent = 100*count_x_y/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to X-Y coordinates");			
		address_x_y = null;
		intersection_x_y = null;
		
		file = new In(outputFilePath);
		outFile = new Out("temp.txt");
		rawLine = file.readLine();
		outFile.println(rawLine + columnCharDelimiter + "bin");	
		HashMap<String,String> address_bin = DataLookup.KeyLookup("/Geocoder/valid_addresses_bin.csv",",","address", "bin");
		//loop to assign bins
		while(!file.isEmpty()) {
			rawLine = file.readLine();
			String addressID = rawLine.substring(0, rawLine.indexOf(columnCharDelimiter));
			String imputed_bin = "";
			if (isIntersection(addressID)) imputed_bin=""; //intersections dont get bin....yet.
			else if (addressID.length()>0) imputed_bin = address_bin.get(addressID);
			outFile.println(rawLine + columnCharDelimiter + imputed_bin);	
			if (imputed_bin.length()>0) count_bin++;
		}
		percent = 100*count_bin/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to BINs");	
		outFile.close();
		file.close();		
		address_bin = null;		
		
		
		file = new In("temp.txt");
		outFile = new Out(outputFilePath);
		rawLine = file.readLine();
		outFile.println(rawLine + columnCharDelimiter + "bbl");		
		HashMap<String,String> address_bbl = DataLookup.KeyLookup("/Geocoder/valid_addresses_bbl.csv",",","address", "bbl");
		while(!file.isEmpty()) {
			rawLine = file.readLine();
			String addressID = rawLine.substring(0, rawLine.indexOf(columnCharDelimiter));
			String imputed_bbl = "";
			if (isIntersection(addressID)) imputed_bbl ="";
			else if (addressID.length()>0) imputed_bbl = address_bbl.get(addressID);
			outFile.println(rawLine + columnCharDelimiter + imputed_bbl);			
			if (imputed_bbl.length()>0) count_bbl++;
		}
		percent = 100*count_bbl/((double)count_obs);
		System.out.println(df.format(percent) + "% were matched to BBLs");		
		outFile.close();
		file.close();		
		address_bbl = null;		
		
		inputFile = new File("temp.txt");
		inputFile.delete();
		
		final double end = System.currentTimeMillis();
		double minutes = ((double)(end - start))/60000;
		System.out.println("Geocode completed after " + df.format(minutes) + " minutes.");
	}

	/**
	 * There are two options for using the Geocoder:
	 *   1) Supplying no command-line arguments -- this will trigger an
	 *     interactive console session, prompting the user for the config
	 *     parameters
	 *   2) Supplying the full set of command-line arguments -- this will
	 *     jump right into the geocoding process
	 * @param args Either 0 or 10 config parameters
	 */
	public static void main(String[] args) {

	    if (args.length > 0 && args.length != 10) {
	        StdOut.println("usage (without arguments): Geocode");
	        StdOut.println("usage (with arguments): Geocode inputFile outputFile logFile unmatchedAddressFile columnCharDelimiter "
	                + "columnNameForBuildingNumber columnNameForStreetName columnNameForZipCode columnNameForBorough columnNameForCity");
            StdOut.println("If any of the above parameters are not applicable, specify blank quotes.");
            return;
	    }

	    String inputFile,                      // 0
	            outputFile,                    // 1
	            logFile,                       // 2
                unmatchedAddressFile,          // 3
                columnCharDelimiter,           // 4
                columnNameForBuildingNumber,   // 5
                columnNameForStreetName,       // 6
                columnNameForZipCode,          // 7
                columnNameForBorough,          // 8
                columnNameForCity              // 9
                ;

	    // Interactively retrieving the config parameters from the user
		if (args.length==0) {

		    StdOut.println("Input file to geocode (can use an absolute filepath, or a relative path from the project root):");
			inputFile = StdIn.readLine().replace("\\", "/");		
			File inp = new File(inputFile);
			while (inp.exists()==false) {
				StdOut.println("File does not exist. Re-enter full file path");
				inputFile = StdIn.readLine().replace("\\", "/");		
				inp = new File(inputFile);			
			}

			StdOut.println("Do you want to create a new file with the output? Enter the full path of the new file to create:");
			outputFile = StdIn.readLine().replace("\\", "/");

			StdOut.println("File to write output/logging/error messages to:");
			logFile = StdIn.readLine().replace("\\", "/");

            StdOut.println("File to write unmatched addresses to:");
            unmatchedAddressFile = StdIn.readLine().replace("\\", "/");

			StdOut.println("What is the delimiter for the input file?");
			columnCharDelimiter = StdIn.readLine();

			StdOut.println("Which variable has the building number for the address?");
			columnNameForBuildingNumber = StdIn.readLine();

			StdOut.println("Which variable has the street name for the address? (Can be the same column as building number)");
			columnNameForStreetName = StdIn.readLine();

			StdOut.println("Is there a ZIP code variable? Enter it here, otherwise blank:");
			columnNameForZipCode = StdIn.readLine();

			StdOut.println("Is there a standardized BORO code/name variable? Enter it here, otherwise blank:");
			columnNameForBorough = StdIn.readLine();

			StdOut.println("Is there a neighborhood/town/city variable? Enter it here, otherwise blank:");
			columnNameForCity = StdIn.readLine();
		}
		// Alternatively, extracting the config parameters from the command-line
		else {
		    int i = 0;
	        inputFile = args[i++].replace("\\", "/");
	        outputFile = args[i++].replace("\\", "/");
	        logFile = args[i++].replace("\\", "/");
	        unmatchedAddressFile = args[i++].replace("\\", "/");
	        columnCharDelimiter = args[i++];
	        columnNameForBuildingNumber = args[i++];
	        columnNameForStreetName = args[i++];
	        columnNameForZipCode = args[i++];
	        columnNameForBorough = args[i++];
	        columnNameForCity = args[i++];
		}

		new Geocode(inputFile, outputFile,
                logFile, unmatchedAddressFile,
                columnCharDelimiter, columnNameForBuildingNumber,
                columnNameForStreetName, columnNameForZipCode,
                columnNameForBorough, columnNameForCity);
	}
}
