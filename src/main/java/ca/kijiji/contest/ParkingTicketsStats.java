package ca.kijiji.contest;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.io.BufferedReader;

public class ParkingTicketsStats {

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
    	String[] tokens = address.split(" ");
    	if (tokens.length <= 1) return address;
    	int last = tokens.length - 1;
    	int secondLast = last - 1;
    	tokens[0] = tokens[0].replaceFirst("\\d+-?\\d+", "");
    	
    	if (isSuffix(tokens[last])) {
    		tokens[last] = "";
    	} else if (isDirection(tokens[last])) {
    		if (isSuffix(tokens[secondLast])) {
    			tokens[secondLast] = "";
    		}
    		tokens[last] = "";
    	}
    	return join(tokens);
    }
    
    private static String join(String[] strings) {
    	StringBuilder builder = new StringBuilder(strings[0]);
    	for (String token : strings) {
    		if (token.length() > 0)
    			builder.append(' ').append(token);
    	}
    	if (builder.toString().length() == 0)
    		return builder.toString();
    	return builder.substring(1);
    }
    
    private static boolean isDirection(String token) {
    	if (token.length() == 1 || token.equals("EAST") || token.equals("WEST")) 
    		return true;
    	return false;
    }
    
    private static boolean isSuffix(String token) {
    	switch (token) {
	    	case "ST":
	    	case "STREET":
	    	case "AVE":
	    	case "AV":
	    	case "BLVD":
	    	case "RD":
	    	case "COURT":
	    	case "CRT":
	    	case "CT":
	    		return true;
    	}
    	return false;
    }
    
    public static void main(String... args) throws Exception {
	    try ( InputStream ticketReader = new java.io.FileInputStream(args[0]);
	    		FileWriter writer = new FileWriter(args[1]);
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

