package com.mystictri.neotexture;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.Vector;

import com.mystictri.neotexture.TextureGraphNode.ConnectionPoint;

import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.ChannelChangeListener;

/**
 * A full texture graph as created and editied by the NeoTextureEditor.
 * @author Holger Dammertz
 *
 */
public final class TextureGraph {
	public TextureGraphListener graphListener = null;

	// currently all operations on nodes with the mouse expect that the clicked node is the selected node
	public final Vector<TextureGraphNode> selectedNodes = new Vector<TextureGraphNode>();
	public final Vector<TextureGraphNode> allNodes = new Vector<TextureGraphNode>();
	public final Vector<TextureNodeConnection> allConnections = new Vector<TextureNodeConnection>();

	public void addNode(TextureGraphNode node) {
		allNodes.add(node);
	}
	

	
	public void addNode(TextureGraphNode node, int x, int y) {
		node.setLocation(x, y);
		addNode(node);
		//setSelectedNode(node);
	}
	
	// first saving version: simple ascii test
	public void save(Writer w) throws IOException {
			w.write("#NTEVersion 6\n");
			w.write("#BeginNodes " + allNodes.size() + "\n");
			// first save all the nodes
			for (TextureGraphNode n : allNodes) {
				n.save(w, n);
			}
			w.write("#EndNodes\n");
			// now all the connections
			w.write("#BeginConnections " + allConnections.size()+"\n");
			for (TextureNodeConnection c : allConnections) {
				w.write(allNodes.indexOf(c.source.parent)+ " ");
				w.write(allNodes.indexOf(c.target.parent)+ " ");
				w.write(c.target.channelIndex+ "\n");
			}
			w.write("#EndConnections\n");
	}

	/**
	 * Loads (and appends) a propperly formatted texture graph from the given scanner
	 * @param s
	 * @return false if something failed during loading, but the test does not guarantee that
	 *         the loaded graph was correct
	 */
	public boolean load(Scanner s) {
		int offset = allNodes.size();
		while (!s.next().equals("#NTEVersion")) System.out.println("ParseWarning 0 in TextureGraph.load");
		int nteVersion = s.nextInt();
		if (nteVersion != 6) System.err.println("WARNING: loading incompatible texture graph of version "+nteVersion); 
		while (!s.next().equals("#BeginNodes")) System.out.println("ParseWarning A in TextureGraph.load");
		int numNodes = s.nextInt();
		for (int i = 0; i < numNodes; i++) {
			TextureGraphNode n = TextureGraphNode.load(s);
			addNode(n);
		}
		while (!s.next().equals("#EndNodes")) System.out.println("ParseWarning B in TextureGraph.load");
		while (!s.next().equals("#BeginConnections")) System.out.println("ParseWarning C in TextureGraph.load");
		int numConnections = s.nextInt();
		for (int i = 0; i < numConnections; i++) {
			TextureGraphNode.ConnectionPoint sourcePoint = allNodes.get(offset + s.nextInt()).getOutputConnectionPoint();
			TextureGraphNode.ConnectionPoint targetPoint = allNodes.get(offset + s.nextInt()).getInputConnectionPointByChannelIndex(s.nextInt());
			addConnection(new TextureNodeConnection(sourcePoint, targetPoint));
		}
		while (!s.next().equals("#EndConnections")) System.out.println("ParseWarning D in TextureGraph.load");
		return true;
	}
	
	
	public TextureGraphNode getNodeAtPosition(int x, int y) {
		TextureGraphNode ret = null;
		for (TextureGraphNode n : allNodes) {
			if (n.containsPoint(x, y)) ret = n;
		}
		return ret;
	}
	
	public void deleteFullGraph() {
		removeConnections(allConnections);
		selectedNodes.clear();
		allNodes.clear();

	}
	
	/**
	 * Replaces oldNode in the texture graph with newNode and trys to reconnect all
	 * TextureNodeConnection as meaningfull as possible. newNode should not yet
	 * have been added or have any connections! 
	 * The basic process is:
	 *   First newNode is inserted; then all outputConnections from oldNode are removed
	 *   and added to newNode; finally the same process is done linearly of the input connections.
	 *   Afterwards oldNode is deleted by calling deleteNode.
	 * @param oldNode
	 * @param newNode
	 */
	public void replaceNode(TextureGraphNode oldNode, TextureGraphNode newNode) {
		addNode(newNode, oldNode.getX(), oldNode.getY());
		
		Vector<TextureNodeConnection> outConns = getAllConnectionsAtOutputPoint(oldNode.getOutputConnectionPoint());
		removeConnections(outConns);
		for (TextureNodeConnection c : outConns) {
			c.source = newNode.getOutputConnectionPoint();
			addConnection(c);
		}
		
		// the first point in 
		Vector<ConnectionPoint> oldNodeallCPs =  oldNode.getAllConnectionPointsVector();
		Vector<ConnectionPoint> newNodeallCPs =  newNode.getAllConnectionPointsVector();
		for (int i = 1; i < oldNodeallCPs.size(); i++) { 
			TextureNodeConnection c = getConnectionAtInputPoint(oldNodeallCPs.get(i));
			if (c != null) { 
				removeConnection(c);
				if (i < newNodeallCPs.size()) {
					c.target = newNodeallCPs.get(i);
					addConnection(c);
				}
			}
		}
		
		_deleteNode(oldNode, true);
	}
	
	
	
	public void _deleteNode(TextureGraphNode node, boolean removeFromSelected) {
		removeConnections(getAllConnectionsAtOutputPoint(node.getOutputConnectionPoint()));
		removeConnections(getConnectionsAtAllInputPoints(node));
		allNodes.remove(node);
		if (removeFromSelected) selectedNodes.remove(node);
		if (graphListener != null) graphListener.nodeDeleted(node);
	}
	

	public void deleteSelection() {
		for (TextureGraphNode n : selectedNodes) {
			/*removeConnections(getAllConnectionsAtOutputPoint(n.outputConnectionPoint));
			removeConnections(getConnectionsAtAllInputPoints(n));
			allNodes.remove(n);
			remove(n);*/
			_deleteNode(n, false);
		}
		selectedNodes.clear();
	}

	public void setSelectedNode(TextureGraphNode node) {
		selectedNodes.clear();
		if (node != null) {
			selectedNodes.add(node);
			//!!TODO: refactoring artifact setComponentZOrder(node, 0);
		}
	}
	
	/**
	 * Adds the given node to the selection if it is not already contained. Else it
	 * is removed from the selection.
	 * @param node
	 */
	public void addOrRemoveNodeToSelection(TextureGraphNode node) {
		if (node != null) {
			if (!selectedNodes.contains(node)) selectedNodes.add(node);
			else selectedNodes.remove(node);
		}
	}
	
	public TextureNodeConnection getConnectionAtInputPoint(TextureGraphNode.ConnectionPoint input) {
		for (TextureNodeConnection c : allConnections) {
			if (c.target == input) return c;
		}
		return null;
	}
	
	public Vector<TextureNodeConnection> getAllConnectionsAtOutputPoint(ConnectionPoint output) {
		Vector<TextureNodeConnection> ret = new Vector<TextureNodeConnection>();
		for (TextureNodeConnection c : allConnections) {
			if (c.source == output) ret.add(c);
		}
		return ret;
	}

	public Vector<TextureNodeConnection> getConnectionsAtAllInputPoints(TextureGraphNode node) {
		Vector<TextureNodeConnection> ret = new Vector<TextureNodeConnection>();
		Vector<ConnectionPoint> allCPs = node.getAllConnectionPointsVector();
		for (int i = 0; i < allCPs.size(); i++) {
			TextureNodeConnection c = getConnectionAtInputPoint(allCPs.get(i));
			if (c != null) ret.add(c);
		}
		return ret;
	}
	
	public void removeConnections(Vector<TextureNodeConnection> conns) {
		if (conns == allConnections) {
			conns = new Vector<TextureNodeConnection>(allConnections);
		}
		for (int i = 0; i < conns.size(); i++) {
			removeConnection(conns.get(i));
		}
	}
	
	public void removeConnection(TextureNodeConnection c) {
		if (c == null) return;
		if (allConnections.remove(c)) {
			c.source.parent.getChannel().removeChannelChangeListener(c);
			c.target.parent.getChannel().setInputChannel(c.target.channelIndex, null);
		} else {
			System.err.println("ERROR in removeConnection: got invalid connection " + c);
		}
	}
	
	public boolean checkForCycle(ConnectionPoint source, ConnectionPoint target) {
		if (target == null) return false;
		if (source == null) return false;
		if (source == target.parent.getOutputConnectionPoint()) return true;
		
		boolean cycle = false;
		
		Vector<TextureNodeConnection> conns = getAllConnectionsAtOutputPoint(target.parent.getOutputConnectionPoint());
		for (int i = 0; i < conns.size(); i++) {
			cycle |= checkForCycle(source, conns.get(i).target);
		}
		
		return cycle;
	}
	
	public boolean addConnection(TextureNodeConnection c) {
		if (checkForCycle(c.source, c.target)) {
			System.out.println("WARNING: cycles not allowed!");
			return false;
		}
		
		// remove a possible connection at the target input connection point
		TextureNodeConnection inputConnection = getConnectionAtInputPoint(c.target);
		if (inputConnection != null) removeConnection(inputConnection); 
		
		c.target.parent.getChannel().setInputChannel(c.target.channelIndex, c.source.parent.getChannel());
		c.source.parent.getChannel().addChannelChangeListener(c);
		allConnections.add(c);
		return true;
	}
	
	public Vector<TextureGraphNode> getAllNodes() {
		return allNodes;
	}
	
	
	/**
	 * This connection node only holds the meta data; the actual connection of
	 * the channels is done in the add/remove methods from the TextureGraphEditorPanel
	 * @author Holger Dammertz
	 * 
	 */
	public static class TextureNodeConnection implements ChannelChangeListener {
		public TextureGraphNode.ConnectionPoint source; // this is an output-node
		public TextureGraphNode.ConnectionPoint target; // this is an input-node
		
		public TextureNodeConnection(TextureGraphNode.ConnectionPoint s, TextureGraphNode.ConnectionPoint t) {
			if (s.channelIndex != -1) {
				System.err.println("ERROR in TextureNodeConnection: source of " + s.parent + " is not an output node");
				return;
			}
			if (t.channelIndex == -1) {
				System.err.println("ERROR in TextureNodeConnection: target of " + t.parent + " is not an input node");
				return;
			}
			source = s;
			target = t;
		}

		public void channelChanged(Channel channelSource) {
			if (source.parent.getChannel() != channelSource) {
				System.err.println("ERROR in TextureNodeConnection: got change event from unexpexted Channel.");
				return;
			}
			target.parent.getChannel().parameterChanged(null);
		}
	}
	
}
