package ca.kijiji.contest;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.regex.Pattern;
import java.io.BufferedReader;

public class ParkingTicketsStats {
	
    static final Pattern number = Pattern.compile("^[\\d\\-+%/!]+ ?");
    static final Pattern suffix = Pattern.compile(" (?:ST|STREET|AVE?|RD|BLVD|COURT|CR?T)$");
    static final Pattern direction = Pattern.compile(" [NSEW](?:[EA]ST)?$");

    public static HackMap sortStreetsByProfitability(InputStream parkingTicketStream) throws Exception{
      HackMap streetProfitability = new HackMap();
      try ( BufferedReader ticketReader =
         new BufferedReader(new InputStreamReader(parkingTicketStream));
      ) {    		 		 
      String ticket = ticketReader.readLine();
      ticket = ticketReader.readLine();
      while (ticket != null) {
        String[] ticketFields = ticket.split(",");
        int price = Integer.parseInt(ticketFields[4]);
        String name = extractStreetName(ticketFields[7]);
        streetProfitability.addToValue(name, price);
    	ticket = ticketReader.readLine();
    }
    return streetProfitability;
      }
    }
        
    private static String extractStreetName(String address) {
    	String street = number.matcher(address).replaceFirst("");
    	String streetName = direction.matcher(street).replaceFirst("");
    	String finalName = suffix.matcher(streetName).replaceFirst("");
    	return finalName;
    }
    
    public static void main(String... args) throws Exception {
	    try ( InputStream ticketReader = new java.io.FileInputStream(args[0]);
	    		FileWriter writer = new FileWriter(args[1], true);
	    ) {
	    long startTime = System.currentTimeMillis();
	    SortedMap<String, Integer> hackmap = sortStreetsByProfitability(ticketReader);
        long duration = System.currentTimeMillis() - startTime;
	    String key = hackmap.firstKey();
	    System.out.println(key + ": " + hackmap.get(key));
	    System.out.println("ST CLAIR : " + hackmap.get("ST CLAIR"));
	    System.out.println("KING : " + hackmap.get("KING"));
	    System.out.println("BLANK : " + hackmap.get(""));
	    System.out.println("NULL : " + hackmap.get(null));
	    System.out.println("DURATION : " + duration);
	    writer.write(key + ": " + hackmap.get(key));
	    writer.write("ST CLAIR : " + hackmap.get("ST CLAIR"));
	    writer.write("KING : " + hackmap.get("KING"));
	    writer.write("BLANK : " + hackmap.get(""));
	    writer.write("NULL : " + hackmap.get(null));
	    writer.write("DURATION : " + duration + "\n---------\n");
	  }
    }
}

