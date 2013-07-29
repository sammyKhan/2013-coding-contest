package ca.kijiji.contest;

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

	/*
	 * Regular Expressions are used to extract the street name in three steps:
	 *   - if found, match and remove the street number
	 *   - if found, match and remove the direction (at the end of the string)
	 *   - if found, match and remove the suffix (guaranteed to now be at the end of the string)
	 */
	
	// matches the street number along with characters % / ! - and an optional space
	static final Pattern NUM_PATTERN = Pattern.compile("^[\\d%/!\\-]+ ?");
	
	// matches the directions N S E W EAST WEST -- at the END of the end of string
	static final Pattern DIRECTN_PATTERN = Pattern.compile(" [NSEW](?:[EA]ST)?$");
	
	// matches the suffixes ST STREET AVE AV RD BLVD COURT CRT CT -- at the end of string
	static final Pattern SUFX_PATTERN = Pattern.compile(" (?:ST|STREET|AVE?|RD|BLVD|COURT|CR?T)$");

	// removes the matches, returning just the name of the street
	private static String extractStreetName(String address) {
		String numberRemoved = NUM_PATTERN.matcher(address).replaceFirst("");
		String directnRemoved = DIRECTN_PATTERN.matcher(numberRemoved).replaceFirst("");
		String finalName = SUFX_PATTERN.matcher(directnRemoved).replaceFirst("");
		return finalName;
	}
	
	/*
	 * The main thread reads the InputStream into a line buffer.
	 * When the line buffer reaches BUFFER_SIZE lines, it passes
	 * the buffer to one of the executor threads to process.
	 */
	
	// number of lines read before passing to thread for processing
	private static final int BUFFER_SIZE = 10_000; //lines
	
	// number of worker threads to process csv records
	private static final int NUM_THREADS = 3;

	/*
	 * Sorts the streets in descending order of profitability
	 * 
	 * @param parkingTicketStream    parking ticket data in csv format
	 * @return                       mapping of street name to amount ticketed, 
	 *                                 sorted in descending order of amount ticketed
	 * @see                          ValueSortedMap
	 */
	public static SortedMap<String,Integer> sortStreetsByProfitability(InputStream parkingTicketStream) throws Exception {	

		// the street to profitability mapping to return
		final ValueSortedMap<String> streets = new ValueSortedMap<String>(20_000, 0.75f, NUM_THREADS);
		
		// executor manages the threads that do the processing
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

		// process the data in batches
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
					streets.add(extractStreetName(address), price);
				}
			}
		}

		// wrap the input stream in a BufferedReader
		try (BufferedReader csvReader = 
				new BufferedReader(new InputStreamReader(parkingTicketStream));) {
			
			//ignore the first line (containing the csv definitions)
			String ticket = csvReader.readLine();
				   ticket = csvReader.readLine();
				   
			//buffer to store the data to pass to executor  
			ArrayList<String> lineBuffer = new ArrayList<String>(BUFFER_SIZE);
			
			while (ticket != null) {
				lineBuffer.add(ticket);
				
				//when full, pass buffer to executor to process
				if (lineBuffer.size() >= BUFFER_SIZE) {
					executor.execute(new TicketParser(lineBuffer));
					lineBuffer = new ArrayList<String>(BUFFER_SIZE);
				}
				
				ticket = csvReader.readLine();
			}
			
			//process the last few lines that were stored in the buffer
			executor.execute(new TicketParser(lineBuffer));
			
			//wait for threads to finish processing the data
			executor.shutdown();
			executor.awaitTermination(20, TimeUnit.SECONDS);
			
			return streets;
		}
	}
}
