package ca.kijiji.contest;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.io.BufferedReader;

public class ParkingTicketsStats {
	
    static final Pattern number = Pattern.compile("^[\\d\\-+%/!]+ ?");
    static final Pattern suffix = Pattern.compile(" (?:ST|STREET|AVE?|RD|BLVD|COURT|CR?T)$");
    static final Pattern direction = Pattern.compile(" [NSEW](?:[EA]ST)?$");
    
    private static final HackMap streets = new HackMap();

    public static HackMap sortStreetsByProfitability(InputStream parkingTicketStream) throws Exception{
      ExecutorService executor = Executors.newFixedThreadPool(3);
      class TicketParser implements Runnable {
    	  
    	  private final String ticket;
    	  
    	  public TicketParser(String ticket) {
    		  this.ticket = ticket;
    	  }

    		@Override
    		public void run() {
    	        String[] ticketFields = ticket.split(",");
    	        int price = Integer.parseInt(ticketFields[4]);
    	        String address = ticketFields[7];
    	    	String street = number.matcher(address).replaceFirst("");
    	    	String streetName = direction.matcher(street).replaceFirst("");
    	    	String finalName = suffix.matcher(streetName).replaceFirst("");
    	        streets.addToValue(finalName, price);
    		}
    	}
      try ( BufferedReader ticketReader =
         new BufferedReader(new InputStreamReader(parkingTicketStream));
      ) {    		 		 
      String ticket = ticketReader.readLine();
      ticket = ticketReader.readLine();
      while (ticket != null) {
    	executor.execute(new TicketParser(ticket));
    	ticket = ticketReader.readLine();
    }
      executor.awaitTermination(20, TimeUnit.SECONDS);
    return streets;
      }
      
      
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
	    System.out.println("DURATION : " + duration);
	    writer.write(key + ": " + hackmap.get(key));
	    writer.write("ST CLAIR : " + hackmap.get("ST CLAIR"));
	    writer.write("KING : " + hackmap.get("KING"));
	    writer.write("BLANK : " + hackmap.get(""));
	    writer.write("DURATION : " + duration + "\n---------\n");
	  }
    }
}

