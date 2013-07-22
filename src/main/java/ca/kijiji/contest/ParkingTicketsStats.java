package ca.kijiji.contest;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    static final int NUMTHREADS = 3;
    
    private static final HackMap streets = new HackMap(400000, (float)0.75, NUMTHREADS);

    public static HackMap sortStreetsByProfitability(InputStream parkingTicketStream) throws Exception{
      ExecutorService executor = Executors.newFixedThreadPool(3);
      class TicketParser implements Runnable {
    	  
    	  private final ArrayList<String> tickets;
    	  
    	  public TicketParser(ArrayList<String> tickets) {
    		  this.tickets = tickets;
    	  }

    		@Override
    		public void run() {
    			for (String ticket : tickets) {
	    	        String[] ticketFields = ticket.split(",");
	    	        int price = Integer.parseInt(ticketFields[4]);
	    	        String address = ticketFields[7];
	    	    	String street = number.matcher(address).replaceFirst("");
	    	    	String streetName = direction.matcher(street).replaceFirst("");
	    	    	String finalName = suffix.matcher(streetName).replaceFirst("");
	    	        streets.addToValue(finalName, price);
    			}
    		}
    	}
      try ( BufferedReader ticketReader =
         new BufferedReader(new InputStreamReader(parkingTicketStream));
      ) {    		 		 
      String ticket = ticketReader.readLine();
      ticket = ticketReader.readLine();
      ArrayList<String> lines = null;
      while (ticket != null) {
    	if (lines == null) {
    		lines = new ArrayList<String>(100000);
    	}
    	lines.add(ticket);
    	if (lines.size() >= 99999) {
        	executor.execute(new TicketParser(lines));
        	lines = null;
    	}
    	ticket = ticketReader.readLine();
    }
  	  executor.execute(new TicketParser(lines));
  	  executor.shutdown();
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

