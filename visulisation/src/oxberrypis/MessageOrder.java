package oxberrypis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import oxberrypis.net.proto.rpi.Rpi.StockEvent;
import oxberrypis.net.proto.setup.VisInit.SetupVisualisation;
import oxberrypis.net.proto.setup.VisInit.SetupVisualisation.Mapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MessageOrder {
	private NetworkPis network;
	private Map<Integer, Integer> idToQueue;
	private Map<Integer, Integer> idToRegion;
	private Map<Integer, String> idToName;
	private List<Queue<StockEvent>> queueList;
	private Map<Integer, Integer> denomPowers;
	private Map<Integer, Integer> lastSeqNum;

	private final String ARCAFILE = "";



	public MessageOrder() {
		network = new NetworkPis();
		idToQueue = new HashMap<Integer, Integer>();
		idToName = new HashMap<Integer, String>();
		queueList = new ArrayList<Queue<StockEvent>>();
		init();
	}


	public int getDenomPower(int stockId) {
		return denomPowers.get(stockId);
	}

	public String getName(int stockId) {
		return idToName.get(stockId);
	}

	





	private void init() {

		SetupVisualisation message = network.getInit(); // Get the initialisation
													// method from parser

		BufferedReader br = null;
		List<Integer> stocks = new ArrayList<Integer>();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(ARCAFILE));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] parts = sCurrentLine.split("\\|");
				int sId = Integer.parseInt(parts[2]);
				denomPowers.put(sId, Integer.parseInt(parts[7]));
				idToName.put(sId, parts[0]);
				idToQueue.put(sId, -1);
				stocks.add(sId);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		int i = 0;
		List<Mapping> mappings = message.getMappingsList();
		for (Mapping m : mappings) {
			List<Integer> mappingStocks = stocks.subList(m.getSymbolMapStart(),m.getSymbolMapEnd());
			for (int stock : mappingStocks) {
				idToRegion.put(stock,i);
			}
			i++;
		}
		


	}

	private void addMessage(StockEvent message) { // Find the queue and add the
													// message, update sequence
													// number
		int queueId = idToQueue.get(message.getStockId());

		if (queueId == -1) {
			// TODO :Find queue/add new queue 
		}

		if (lastSeqNum.get(message.getStockId()) < message.getSeqNum()) {
			queueList.get(queueId).add(message);
			lastSeqNum.put(message.getStockId(), message.getSeqNum());
		}
	}

	/**
	 * Get the next message to process
	 */
	public StockEvent getMessage() {
		while (anyEmptyQueue()) {
			addMessage(network.getMsg());
		}

		long bestTime = getTime(queueList.get(0).peek());
		Queue<StockEvent> bestQueue = queueList.get(0);
		for (Queue<StockEvent> q : queueList) {
			if (getTime(q.peek()) < bestTime)

				bestQueue = q;
		}
		return bestQueue.remove();
	}


	private long getTime(StockEvent s) {
		return ((long) (s.getTimestampS()) * 1000000000)
				+ (long) s.getTimestampNs();
	}


	private boolean anyEmptyQueue() {
		for (Queue<StockEvent> q : queueList) {
			if (q.isEmpty())
				return true;
		}
		return false;
	}
}
