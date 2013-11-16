package Parser;
import java.util.ArrayList; 

public class Intersection{
	public Street street1, street2;
	
	public Intersection(Street street1, Street street2) {
		this.street1=street1;
		this.street2=street2;
	}
	
	public Intersection(Parser p, ArrayList<String> boros, String unparsed_1, String unparsed_2){
		unparsed_1 = Parser.cleanStreet(unparsed_1);
		unparsed_2 = Parser.cleanStreet(unparsed_2);
		this.street1 = new Street(p, boros, unparsed_1);
		this.street2 = new Street(p, boros, unparsed_2);
	}		
	
	public String toString(){
		return street1.toString() + " & " + street2.toString();
	}	
	
	public static void main(String[] args){
		Parser p = new Parser();
		String street = "W KINGSBRIDGE RD & SEDGWICK AV";
		Intersector dividor = new Intersector(street);
		String str1 = street.substring(0, dividor.startPos);
		String str2 = street.substring(dividor.endPos);
		StdOut.println(str1);
		StdOut.println(str2);
		ArrayList<String> boros = new ArrayList<String>();
		boros.add("2");
		Intersection i = new Intersection(p, boros, str1, str2);		
		StdOut.println(i.street1.direction);
		StdOut.println(i.street1.name);
		StdOut.println(i.street1.suffix);
		StdOut.println(i.street2.direction);
		StdOut.println(i.street2.name);
		StdOut.println(i.street2.suffix);		
		
	}	
}	
