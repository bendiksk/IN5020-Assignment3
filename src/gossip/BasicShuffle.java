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
	public void nextCycle(Node thisNode, int protocolID) {
		// Implement the shuffling protocol using the following steps (or
		// you can design a similar algorithm):
		// Let's name this node as P
		
		// 1. If P is waiting for a response from a shuffling operation initiated in a previous cycle, return;
		// 2. If P's cache is empty, return;
		if(awaitingReply || cache.isEmpty()) return;
		// 3. Select a random neighbor (named Q) from P's cache to initiate the shuffling;
		//	  - You should use the simulator's common random source to produce a random number: CommonState.r.nextInt(cache.size())
		int qIndex = CommonState.r.nextInt(cache.size());
		Node thatNode = cache.get(qIndex).getNode();

		// 4. If P's cache is full, remove Q from the cache;
		if(cache.size() >= maxSize) {
			cache.remove(qIndex);
		}

		// 5. Select a subset of other l - 1 random neighbors from P's cache;
		//	  - l is the length of the shuffle exchange
		//    - Do not add Q to this subset
        ArrayList<Entry> subset = createRandomSubset(thatNode, l-1);
        // 6. Add P to the subset;
		subset.add(new Entry(thisNode));

		// 7. Send a shuffle request to Q containing the subset;
		//	  - Keep track of the nodes sent to Q
		//	  - Example code for sending a message:
        GossipMessage message = new GossipMessage(thisNode, subset);
        message.setType(MessageType.SHUFFLE_REQUEST);
        Transport tr = (Transport) thisNode.getProtocol(tid);
        awaitingReply = true;
        tr.send(thisNode, thatNode, message, protocolID);
		//
		// 8. From this point on P is waiting for Q's response and will not initiate a new shuffle operation;
		//
		// The response from Q will be handled by the method processEvent.
	}

    /* The simulator engine calls the method processEvent at the specific time unit that an event occurs in the simulation.
	 * It is not called periodically as the nextCycle method.
	 * 
	 * You should implement the handling of the messages received by this node in this method.
	 * 
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
    public void processEvent(Node thisNode, int pid, Object event) {
        // Let's name this node as Q;
        // Q receives a message from P;
        //	  - Cast the event object to a message:
        GossipMessage message = (GossipMessage) event;
        ArrayList<Entry> shuffleList = (ArrayList<Entry>) message.getShuffleList();
        Node thatNode = message.getNode();
        GossipMessage replyMessage;
        Transport tr = (Transport) thisNode.getProtocol(tid);
        switch (message.getType()) {
            // If the message is a shuffle request:
            case SHUFFLE_REQUEST:
				//	  1. If Q is waiting for a response from a shuffling initiated in a previous cycle, send back to P a message rejecting the shuffle request;
				if (awaitingReply) {
//					System.out.printf("Event: %s, NodeID: %s\t-REJECTED-\n", message.getType(), thisNode.getID());
					replyMessage = new GossipMessage(thisNode, new ArrayList<>());
					replyMessage.setType(MessageType.SHUFFLE_REJECTED);
					tr.send(thisNode, thatNode, replyMessage, pid);
					break;
				}
//				System.out.printf("Event: %s, NodeID: %s, Shuffle/Nbs: %s/%s\n", message.getType(), thisNode.getID(), shuffleList.size(),degree());
				//	  2. Q selects a random subset of size l of its own neighbors;
				ArrayList<Entry> subset = createRandomSubset(thatNode, l);

				//	  3. Q reply P's shuffle request by sending back its own subset;
				replyMessage = new GossipMessage(thisNode, subset);
				replyMessage.setType(MessageType.SHUFFLE_REPLY);

				tr.send(thisNode, thatNode, replyMessage, pid);
				//	  4. Q updates its cache to include the neighbors sent by P:
				//		 - No neighbor appears twice in the cache
				//		 - Use empty cache slots to add the new entries
				//		 - If the cache is full, you can replace entries among the ones sent to P with the new ones
				addToCache(shuffleList);
				break;

            // If the message is a shuffle reply:
            case SHUFFLE_REPLY:
//				System.out.printf("Event: %s, NodeID: %s, Shuffle/Nbs: %s/%s\n", message.getType(), thisNode.getID(), shuffleList.size(),degree());
                //	  1. In this case Q initiated a shuffle with P and is receiving a response containing a subset of P's neighbors
                //	  2. Q updates its cache to include the neighbors sent by P:
                addToCache(shuffleList);
                //	  3. Q is no longer waiting for a shuffle reply;
                awaitingReply = false;
                break;

            // If the message is a shuffle rejection:
            case SHUFFLE_REJECTED:
//				System.out.printf("Event: %s, NodeID: %s\n", message.getType(), thisNode.getID());
                //	  1. If P was originally removed from Q's cache, add it again to the cache.
                if (!this.contains(thatNode)) {
                    if (cache.size() >= maxSize) {
                        throw new IllegalStateException("NodeID: " + thisNode.getID() + ":Cache does not have space for node rejecting shuffle request");
                    }
                    cache.add(new Entry(thatNode));
                }
                //	  2. Q is no longer waiting for a shuffle reply;
                awaitingReply = false;
                break;

            default:
                break;
        }
    }

	/**
	 * Create a random subset from the cache.
	 * Will not contain duplicates or the sender of the message (nodeToAvoid)
	 *
	 * Pre-condition:
	 *  - subsetSize <= cache.size()
	 *
	 * @param nodeToAvoid will skip this if contained in the cache
	 * @param subsetSize
	 * @return arrayList with entries containing random nodes from the cache
	 */
	private ArrayList<Entry> createRandomSubset(Node nodeToAvoid, int subsetSize) {
		swapSetIndices.clear();
		ArrayList<Entry> subset = new ArrayList<>();

		if (cache.size() <= subsetSize) {
			// Add whole cache to subset, avoid Q
			for (int i = 0; i < cache.size(); i++) {
				if (cache.get(i).getNode().getID() == nodeToAvoid.getID()) continue;
				swapSetIndices.add(i);
				Entry newEntry = new Entry(cache.get(i).getNode());
				newEntry.setSentTo(nodeToAvoid);
				subset.add(newEntry);
			}
		} else {
			// Fill subset with random nodes from cache which is not Q
			ArrayList<Integer> cacheIndices = getCacheIndices(nodeToAvoid);
			for (int i = 0; i < subsetSize; i++) {
				int nextIndex = CommonState.r.nextInt(cacheIndices.size());
				int randomIndex = cacheIndices.remove(nextIndex);
				// Add node to subset and remember index
				swapSetIndices.add(randomIndex);
				Entry newEntry = new Entry(cache.get(randomIndex).getNode());
				// TODO: Is this needed?
				newEntry.setSentTo(nodeToAvoid);
				subset.add(newEntry);
			}
		}
		return subset;
	}

	/**
	 * Get a list of indices for the cache
	 *
	 * @param nodeToAvoid
	 * @return arrayList with indices
	 */
	private ArrayList<Integer> getCacheIndices(Node nodeToAvoid) {
		ArrayList<Integer> temp = new ArrayList<>();
		for (int i = 0; i < cache.size(); i++) {
			if(cache.get(i).getNode().getID() == nodeToAvoid.getID()) {
				continue;
			}
			temp.add(i);
		}
		return temp;
	}

	/**
	 * Adds a list of entries to the cache using the rules of the shuffle routine.
	 *
	 * @param shuffleList
	 */
	private void addToCache(ArrayList<Entry> shuffleList) {
		int swapped = 0;
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
				    // CASE: No entries left to replace with the new neighbour
                    // TODO: Maybe put node in a random cache spot instead of dismissing incoming entry?
				    continue;
				}
				Integer swapIndex = swapSetIndices.remove(0);
				cache.set(swapIndex, new Entry(entry.getNode()));
				swapped ++;
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
