package Geocoder;

import javax.swing.SwingWorker;

public class GeocodeUIWorker extends SwingWorker<Void, Void> {

	String inputFile;
	String outputFile;
	String delimiterFile;
	String buildingNumberColumn;
	String streetNameColumn;
	String zipCodeColumn;
	String boroColumn;
	String cityColumn;
	
	public GeocodeUIWorker(String inputFile, String outputFile, String delimiterFile, 
			String buildingNumberColumn, String streetNameColumn, String zipCodeColumn, 
			String boroColumn, String cityColumn) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.delimiterFile = delimiterFile;
		this.buildingNumberColumn = buildingNumberColumn;
		this.streetNameColumn = streetNameColumn;
		this.zipCodeColumn = zipCodeColumn;
		this.boroColumn = boroColumn;
		this.cityColumn = cityColumn;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		try {
			Geocode geocode = new Geocode(
					this.inputFile,
					this.outputFile,
	//				"", "", // TODO error-log file and unmatched-address file
					this.delimiterFile,
					this.buildingNumberColumn,
					this.streetNameColumn,
					this.zipCodeColumn,
					this.boroColumn,
					this.cityColumn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void done() {
		//startGeocodeButton.setEnabled(true);
	}

}
