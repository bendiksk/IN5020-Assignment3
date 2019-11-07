package gossip;

import java.util.ArrayList;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;


/**
 * @author Lucas Provensi
 *
 * Basic Shuffling protocol template
 *
 * The basic shuffling algorithm, introduced by Stavrou et al in the paper:
 * "A Lightweight, Robust P2P System to Handle Flash Crowds", is a simple
 * peer-to-peer communication model. It forms an overlay and keeps it
 * connected by means of an epidemic algorithm. The protocol is extremely
 * simple: each peer knows a small, continuously changing set of other peers,
 * called its neighbors, and occasionally contacts a random one to exchange
 * some of their neighbors.
 *
 * This class is a template with instructions of how to implement the shuffling
 * algorithm in PeerSim.
 * Should make use of the classes Entry and GossipMessage:
 *    Entry - Is an entry in the cache, contains a reference to a neighbor node
 *  		  and a reference to the last node this entry was sent to.
 *    GossipMessage - The message used by the protocol. It can be a shuffle
 *    		  request, reply or reject message. It contains the originating
 *    		  node and the shuffle list.
 *
 */
public class BasicShuffle  implements Linkable, EDProtocol, CDProtocol{

	private static final String PAR_CACHE = "cacheSize";
	private static final String PAR_L = "shuffleLength";
	private static final String PAR_TRANSPORT = "transport";

	private final int tid;

	// The list of neighbors known by this node, or the cache.
	private List<Entry> cache;

	// The maximum size of the cache;
	private final int maxSize;

	// The maximum length of the shuffle exchange;
	private final int l;

	// To check intermediate waiting state between SHUFFLE_REQUEST and SHUFFLE_REPLY / SHUFFLE_REJECTED
	private boolean awaitingReply;

	// Remember last subset of nodes created using createRandomSubset() (indices to cache)
	ArrayList<Integer> swapSetIndices;

	/**
	 * Constructor that initializes the relevant simulation parameters and
	 * other class variables.
	 *
	 * @param n simulation parameters
	 */
	public BasicShuffle(String n)
	{
		this.awaitingReply = false;
		this.swapSetIndices = new ArrayList<>();
		this.maxSize = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

		cache = new ArrayList<Entry>(maxSize);
	}

	/* START YOUR IMPLEMENTATION FROM HERE
	 *
	 * The simulator engine calls the method nextCycle once every cycle
	 * (specified in time units in the simulation script) for all the nodes.
	 *
	 * You can assume that a node initiates a shuffling operation every cycle.
	 *
	 * @see peersim.cdsim.CDProtocol#nextCycle(peersim.core.Node, int)
	 */
	@Override
	public void nextCycle(Node node, int protocolID) {

		// Implement the shuffling protocol using the following steps (or
		// you can design a similar algorithm):
		// Let's name this node as P
		// 1. If P is waiting for a response from a shuffling operation initiated in a previous cycle, return;
		// 2. If P's cache is empty, return;
		if(awaitingReply || cache.isEmpty()){
			return;
		}

		// 3. Select a random neighbor (named Q) from P's cache to initiate the shuffling;
		//	  - You should use the simulator's common random source to produce a random number: CommonState.r.nextInt(cache.size())
		// Initially everyone sends to Node 0
		int qIndex = CommonState.r.nextInt(cache.size());
		Entry Q = cache.get(qIndex);

		// 4. If P's cache is full, remove Q from the cache;
		if(cache.size() >= maxSize) {
			cache.remove(qIndex);
		}

		// 5. Select a subset of other l - 1 random neighbors from P's cache;
		//	  - l is the length of the shuffle exchange
		//    - Do not add Q to this subset
        ArrayList<Entry> subset = createRandomSubset(Q.getNode());

        // 6. Add P to the subset;
		subset.add(new Entry(node));

		// 7. Send a shuffle request to Q containing the subset;
		//	  - Keep track of the nodes sent to Q
		//	  - Example code for sending a message:
		GossipMessage message = new GossipMessage(node, subset);
		message.setType(MessageType.SHUFFLE_REQUEST);
		Transport tr = (Transport) node.getProtocol(tid);

		// 8. From this point on P is waiting for Q's response and will not initiate a new shuffle operation;
		awaitingReply = true;

		tr.send(node, Q.getNode(), message, protocolID);

	}

	private ArrayList<Entry> createRandomSubset(Node nodeToAvoid) {
		swapSetIndices.clear();
		ArrayList<Entry> subset = new ArrayList<>();
		if(cache.size() == 1){
			return subset;
		} else if (cache.size() < l - 1) {
			for (int i = 0; i < cache.size(); i++) {
				swapSetIndices.add(i);
				Entry newEntry = new Entry(cache.get(i).getNode());
				newEntry.setSentTo(nodeToAvoid);
				subset.add(newEntry);
			}
		} else {
			for (int i = 0; i < l - 1; i++) {
				// Get a new random Node from the cache which is not already used, and not Q
				int randomIndex = CommonState.r.nextInt(cache.size());
				while (swapSetIndices.contains(randomIndex) || cache.get(randomIndex).getNode().equals(nodeToAvoid)) {
					randomIndex = CommonState.r.nextInt(cache.size());
				}
				swapSetIndices.add(randomIndex);
				Entry newEntry = new Entry(cache.get(randomIndex).getNode());
				newEntry.setSentTo(nodeToAvoid);
				subset.add(newEntry);
			}
		}
		return subset;
	}

	/* The simulator engine calls the method processEvent at the specific time unit that an event occurs in the simulation.
	 * It is not called periodically as the nextCycle method.
	 *
	 * You should implement the handling of the messages received by this node in this method.
	 *
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node node, int pid, Object event) {
		//System.out.printf("\nProcess event called\nsize: %s\ntype: %s", cache.size(), event.getClass());

		// Let's name this node as Q;
		// Q receives a message from P;
		// - Cast the event object to a message:
		GossipMessage message = (GossipMessage) event;
		ArrayList<Entry> shuffleList = (ArrayList<Entry>) message.getShuffleList();
		Node sender = message.getNode();

		System.out.println("Receiver: " + node.getID());
		System.out.println("Receiver subset size:" + message.getShuffleList().size());
		for(Entry e : shuffleList){
			System.out.println("Node in shuffleList: " + e.getNode().getID());
		}

		switch (message.getType()) {
		// If the message is a shuffle request:
		case SHUFFLE_REQUEST:
		//	  1. If Q is waiting for a response from a shuffling initiated in a previous cycle, send back to P a message rejecting the shuffle request;
			if(awaitingReply){
				GossipMessage replyMessage = new GossipMessage(node, null);
				message.setType(MessageType.SHUFFLE_REJECTED);
				Transport tr = (Transport) node.getProtocol(tid);
				tr.send(node, sender, replyMessage, pid);
			}

		//	  2. Q selects a random subset of size l of its own neighbors;
			ArrayList<Entry> subset = createRandomSubset(sender);

		//	  3. Q reply P's shuffle request by sending back its own subset;
			GossipMessage replyMessage = new GossipMessage(node, subset);
			message.setType(MessageType.SHUFFLE_REPLY);
			Transport tr = (Transport) node.getProtocol(tid);
			tr.send(node, sender, replyMessage, pid);
		//	  4. Q updates its cache to include the neighbors sent by P:
		//		 - No neighbor appears twice in the cache
		//		 - Use empty cache slots to add the new entries
		//		 - If the cache is full, you can replace entries among the ones sent to P with the new ones
			addToCache(shuffleList);
			break;

		// If the message is a shuffle reply:
		case SHUFFLE_REPLY:
		//	  1. In this case Q initiated a shuffle with P and is receiving a response containing a subset of P's neighbors
		//	  2. Q updates its cache to include the neighbors sent by P:
			addToCache(shuffleList);
			//	  3. Q is no longer waiting for a shuffle reply;
			awaitingReply = false;
			break;

		// If the message is a shuffle rejection:
		case SHUFFLE_REJECTED:
		//	  1. If P was originally removed from Q's cache, add it again to the cache.
            if(!this.contains(sender)) {
            	if(cache.size() >= maxSize) {
            		// TODO: Maybe fix in elegant way
            		throw new IllegalStateException("Cache does not have space for rejected node");
				}
				cache.add(new Entry(sender));
            }
		//	  2. Q is no longer waiting for a shuffle reply;
			awaitingReply = false;
			break;

		default:
			break;
		}

	}

	private void addToCache(ArrayList<Entry> shuffleList) {
		for (Entry entry : shuffleList) {
			if (this.contains(entry.getNode())) {
			//		 - No neighbor appears twice in the cache
				continue;
			} else if(cache.size() < maxSize) {
			//		 - Use empty cache slots to add new entries
				cache.add(new Entry(entry.getNode()));
			} else {
			//		 - If the cache is full, you can replace entries among the ones originally sent to P with the new ones
				if (swapSetIndices.size() == 0) {
					// TODO: Maybe put node in a random cache spot instead of throwing error
					throw new IllegalStateException("Subset sent not same size as subset received");
				}
				Integer swapIndex = swapSetIndices.remove(0);
				cache.set(swapIndex, new Entry(entry.getNode()));
			}
		}
	}

	/* The following methods are used only by the simulator and don't need to be changed */

	@Override
	public int degree() {
		return cache.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return cache.get(i).getNode();
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		if (contains(neighbour))
			return false;

		if (cache.size() >= maxSize)
			return false;

		Entry entry = new Entry(neighbour);
		cache.add(entry);

		return true;
	}

	@Override
	public boolean contains(Node neighbor) {
		return cache.contains(new Entry(neighbor));
	}

	public Object clone()
	{
		BasicShuffle gossip = null;
		try {
			gossip = (BasicShuffle) super.clone();
		} catch( CloneNotSupportedException e ) {

		}
		gossip.cache = new ArrayList<Entry>();

		return gossip;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub
	}
}
