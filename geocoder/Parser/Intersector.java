package Parser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Intersector{
	private static Pattern pattern;
	private static Matcher matcher;
			
	public boolean exists;
	public String token = "";
	public int startPos, endPos;
	
	public Intersector(String unparsed) {
		unparsed = unparsed.toUpperCase();
		pattern = Pattern.compile("(\\s)(AND|&|\\+|AT)(\\s)");
		matcher = pattern.matcher(unparsed);	
		if (matcher.find() && matcher.end()>1 && unparsed.length()>matcher.end()) {
			this.token = matcher.group();
			this.exists = true;
			this.startPos = matcher.start();
			this.endPos = matcher.end();
		}

	}
	
	public static void main(String[] args) {
		Intersector dividor = new Intersector("W KINGSBRIDGE RD & SEDGWICK AV");
		System.out.println(dividor.token);
	}

}