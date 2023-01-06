package student_pack;

import java.util.ArrayList;

public class PlaylistTree {
	
	public PlaylistNode primaryRoot;		//root of the primary B+ tree
	public PlaylistNode secondaryRoot;	//root of the secondary B+ tree
	public PlaylistTree(Integer order) {
		PlaylistNode.order = order;
		primaryRoot = new PlaylistNodePrimaryLeaf(null);
		primaryRoot.level = 0;
		secondaryRoot = new PlaylistNodeSecondaryLeaf(null);
		secondaryRoot.level = 0;
	}
	
	public void addSong(CengSong song) {
		// TODO: Implement this method
		// add methods to fill both primary and secondary tree


		return;
	}
	
	public CengSong searchSong(Integer audioId) {
		// TODO: Implement this method
		// find the song with the searched audioId in primary B+ tree
		// return value will not be tested, just print according to the specifications
		return null;
	}
	
	
	public void printPrimaryPlaylist() {
		// TODO: Implement this method
		// print the primary B+ tree in Depth-first order

		return;
	}
	
	public void printSecondaryPlaylist() {
		// TODO: Implement this method
		// print the secondary B+ tree in Depth-first order

		return;
	}
	
	// Extra functions if needed

}


