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
		// add methods to fill both primary and secondary tree

		int order = PlaylistNode.order;
		int pos_song_number = 2*order;

		PlaylistNode primary_node = primaryRoot;
		int finished = 0;

		// find the leaf to insert
		while (primary_node.getType() != PlaylistNodeType.Leaf)
		{
			finished = 0;
			int child_count = ((PlaylistNodePrimaryIndex) primary_node).audioIdCount();
			for (int i = 0; i < child_count ; i++) {
				if (((PlaylistNodePrimaryIndex) primary_node).audioIdAtIndex(i) > song.audioId())
				{
					primary_node = ((PlaylistNodePrimaryIndex) primary_node).getChildrenAt(i);
					finished = 1;
					break;
				}
			}
			if (finished == 0) {
				primary_node = ((PlaylistNodePrimaryIndex) primary_node).getChildrenAt(child_count);
			}
		}

		PlaylistNodePrimaryLeaf insert_leaf = ((PlaylistNodePrimaryLeaf) primary_node);

		// find the place to insert in the leaf
		int insert_index = 0;
		for (int index = 0; index <= insert_leaf.songCount() ; index++) {
			if(insert_leaf.audioIdAtIndex(index) == -1){
				insert_index = index;
				break;
			}
			if (insert_leaf.audioIdAtIndex(index) > song.audioId())
			{
				insert_index = index;
				break;
			}
		}

		insert_leaf.addSong(insert_index, song);

		// leaf is a real leaf
		if(insert_leaf.getParent() != null){
			int leaf_song_count = insert_leaf.songCount();

			// if number of songs in leaf is bigger than the possible number of songs, namely overflow
			if(leaf_song_count > pos_song_number){

				PlaylistNode parent = insert_leaf.getParent();

				PlaylistNodePrimaryLeaf new_leaf = new PlaylistNodePrimaryLeaf(parent);

				// dividing the leaf
				int new_index=0;
				for(int old_index = order; old_index < leaf_song_count; old_index++){
					CengSong divided_song = insert_leaf.songAtIndex(old_index);
					new_leaf.addSong(new_index, divided_song);
					new_index++;
				}

				// removing the copied elements
				for(int old_index = order; old_index < leaf_song_count; old_index++){
					insert_leaf.getSongs().remove(order);
				}

				int id_to_copy_up = new_leaf.audioIdAtIndex(0); // take the first id of the new leaf

				PlaylistNodePrimaryIndex node_parent = (PlaylistNodePrimaryIndex) parent; // cast the type of parent

				// add id and the child to the parent
				for (int index = 0; index <= (node_parent).audioIdCount(); index++) {
					if( (node_parent).audioIdAtIndex(index) == -1){
						node_parent.getAllAudioIds().add(id_to_copy_up);
						node_parent.getAllChildren().add(new_leaf);
						break;
					}
					if( (node_parent).audioIdAtIndex(index) > id_to_copy_up)
					{
						node_parent.getAllAudioIds().add(index, id_to_copy_up);
						node_parent.getAllChildren().add(index + 1, new_leaf);
						break;
					}
				}

				// if number of songs in parent node is bigger than the possible number of songs, namely overflow
				while (node_parent.audioIdCount() > pos_song_number){

					PlaylistNode up_parent = parent.getParent();

					// if the internal node is a real internal node
					if(up_parent != null){

						PlaylistNodePrimaryIndex new_internal_node = new PlaylistNodePrimaryIndex(up_parent);

						// copy the last number of order elements to new internal node
						new_index=0;
						for (int index = order + 1 ; index < node_parent.audioIdCount() ; index++)
						{
							new_internal_node.getAllAudioIds().add(new_index,node_parent.audioIdAtIndex(index));
							new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(index));
							node_parent.getChildrenAt(index).setParent(new_internal_node);
							new_index++;
						}
						new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(node_parent.audioIdCount()));
						node_parent.getChildrenAt(node_parent.audioIdCount()).setParent(new_internal_node);

						int id_to_push_up = node_parent.audioIdAtIndex(order); // take the middle element id of the old internal node

						// removing the copied audio ids and children
						int node_count = node_parent.audioIdCount();
						for(int old_index = order; old_index < node_count; old_index++){
							node_parent.getAllAudioIds().remove(order);
						}
						for(int old_index = order+1; old_index <= node_count; old_index++){
							node_parent.getAllChildren().remove(order+1);
						}

						PlaylistNodePrimaryIndex node_up_parent = (PlaylistNodePrimaryIndex) up_parent;

						// add the id and point the next to the new internal node
						for (int index = 0; index <= node_up_parent.audioIdCount(); index++) {
							if( node_up_parent.audioIdAtIndex(index) == -1){
								node_up_parent.getAllAudioIds().add(id_to_push_up);
								node_up_parent.getAllChildren().add(new_internal_node);
								break;
							}
							if( node_up_parent.audioIdAtIndex(index) > id_to_push_up)
							{
								node_up_parent.getAllAudioIds().add(index, id_to_push_up);
								node_up_parent.getAllChildren().add(index + 1, new_internal_node);
								node_up_parent.getAllChildren().set(index, parent);
								break;
							}
						}
						node_parent = (PlaylistNodePrimaryIndex) up_parent;
						parent = up_parent;
					}
					// if the internal node is root (sus internal node)
					if(up_parent == null){
						PlaylistNodePrimaryIndex new_internal_node = new PlaylistNodePrimaryIndex(null);
						PlaylistNodePrimaryIndex new_root = new PlaylistNodePrimaryIndex(null);

						// copy the last number of order elements to new internal node
						new_index=0;
						for (int index = order + 1 ; index < node_parent.audioIdCount() ; index++)
						{
							new_internal_node.getAllAudioIds().add(new_index,node_parent.audioIdAtIndex(index));
							new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(index));
							node_parent.getChildrenAt(index).setParent(new_internal_node);
							new_index++;
						}
						new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(node_parent.audioIdCount()));
						node_parent.getChildrenAt(node_parent.audioIdCount()).setParent(new_internal_node);

						int id_to_push_up = node_parent.audioIdAtIndex(order); // take the middle element id of the old internal node

						// removing the copied audio ids and children
						int node_count = node_parent.audioIdCount();
						for(int old_index = order; old_index < node_count; old_index++){
							node_parent.getAllAudioIds().remove(order);
						}
						for(int old_index = order+1; old_index <= node_count; old_index++){
							node_parent.getAllChildren().remove(order+1);
						}

						new_root.getAllAudioIds().add(id_to_push_up);
						new_root.getAllChildren().add(parent);
						new_root.getAllChildren().add(new_internal_node);
						parent.setParent(new_root);
						new_internal_node.setParent(new_root);
						primaryRoot = new_root;
						break;
					}
				}
			}
		}

		// leaf is root (sus leaf)
		if(insert_leaf.getParent() == null){
			int leaf_song_count = insert_leaf.songCount();

			// if number of songs in leaf is bigger than the possible number of songs, namely overflow
			if (leaf_song_count > pos_song_number){
				PlaylistNodePrimaryLeaf new_leaf = new PlaylistNodePrimaryLeaf(null);
				PlaylistNodePrimaryIndex new_root = new PlaylistNodePrimaryIndex(null);

				// dividing the leaf
				int new_index=0;
				for(int old_index = order; old_index < leaf_song_count; old_index++){
					CengSong divided_song = insert_leaf.songAtIndex(old_index);
					new_leaf.addSong(new_index, divided_song);
					new_index++;
				}

				// removing the copied elements
				for(int old_index = order; old_index < leaf_song_count; old_index++){
					insert_leaf.getSongs().remove(order);
				}

				int id_to_copy_up = new_leaf.audioIdAtIndex(0); // take the first id of the new leaf

				insert_leaf.setParent(new_root);
				new_leaf.setParent(new_root);
				new_root.getAllAudioIds().add(id_to_copy_up);
				new_root.getAllChildren().add(insert_leaf);
				new_root.getAllChildren().add(new_leaf);
				primaryRoot = new_root;
			}
		}

		int order_2 = PlaylistNode.order;
		int pos_genre_number = 2*order_2;

		PlaylistNode secondary_node = secondaryRoot;
		int finished_2 = 0;

		// find the leaf to insert
		while (secondary_node.getType() != PlaylistNodeType.Leaf)
		{
			finished_2 = 0;
			int child_count = ((PlaylistNodeSecondaryIndex) secondary_node).genreCount();
			for (int i = 0; i < child_count ; i++) {
				if (((PlaylistNodeSecondaryIndex) secondary_node).genreAtIndex(i).compareTo(song.genre()) > 0)
				{
					secondary_node = ((PlaylistNodeSecondaryIndex) secondary_node).getChildrenAt(i);
					finished_2 = 1;
					break;
				}
			}
			if (finished_2 == 0) {
				secondary_node = ((PlaylistNodeSecondaryIndex) secondary_node).getChildrenAt(child_count);
			}
		}

		PlaylistNodeSecondaryLeaf insert_leaf_2 = ((PlaylistNodeSecondaryLeaf) secondary_node);

		// find the place to insert in the leaf
		int insert_index_2 = 0;
		for (int index = 0; index <= insert_leaf_2.genreCount() ; index++) {
			if(insert_leaf_2.genreAtIndex(index) == null){
				insert_index_2 = index;
				break;
			}
			if (insert_leaf_2.genreAtIndex(index).compareTo(song.genre()) >= 0)
			{
				insert_index_2 = index;
				break;
			}
		}

		insert_leaf_2.addSong(insert_index_2, song);

		// leaf is a real leaf
		if(insert_leaf_2.getParent() != null){
			int leaf_genre_count = insert_leaf_2.genreCount();

			// if number of genres in leaf is bigger than the possible number of genres, namely overflow
			if(leaf_genre_count > pos_genre_number){

				PlaylistNode parent = insert_leaf_2.getParent();

				PlaylistNodeSecondaryLeaf new_leaf = new PlaylistNodeSecondaryLeaf(parent);

				// dividing the leaf
				int new_index=0;
				for(int old_index = order_2; old_index < leaf_genre_count; old_index++){
					ArrayList<CengSong> divided_bucket = insert_leaf_2.songsAtIndex(old_index);
					for(int i = 0; i < divided_bucket.size(); i++){
						CengSong divided_song = divided_bucket.get(i);
						new_leaf.addSong(new_index, divided_song);
					}
					new_index++;
				}

				// removing the copied elements
				for(int old_index = order_2; old_index < leaf_genre_count; old_index++){
					insert_leaf_2.getSongBucket().remove(order_2);
				}

				String genre_to_copy_up = new_leaf.genreAtIndex(0); // take the first id of the new leaf

				PlaylistNodeSecondaryIndex node_parent = (PlaylistNodeSecondaryIndex) parent; // cast the type of parent

				// add genre and the child to the parent
				for (int index = 0; index <= (node_parent).genreCount(); index++) {
					if( (node_parent).genreAtIndex(index) == "Not Valid Index!!!"){
						node_parent.getAllGenres().add(genre_to_copy_up);
						node_parent.getAllChildren().add(new_leaf);
						break;
					}
					if( (node_parent).genreAtIndex(index).compareTo(genre_to_copy_up) >= 0)
					{
						node_parent.getAllGenres().add(index, genre_to_copy_up);
						node_parent.getAllChildren().add(index + 1, new_leaf);
						break;
					}
				}

				// if number of genres in parent node is bigger than the possible number of genres, namely overflow
				while (node_parent.genreCount() > pos_genre_number){

					PlaylistNode up_parent = parent.getParent();

					// if the internal node is a real internal node
					if(up_parent != null){

						PlaylistNodeSecondaryIndex new_internal_node = new PlaylistNodeSecondaryIndex(up_parent);

						// copy the last number of order elements to new internal node
						new_index=0;
						for (int index = order_2 + 1 ; index < node_parent.genreCount() ; index++)
						{
							new_internal_node.getAllGenres().add(new_index,node_parent.genreAtIndex(index));
							new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(index));
							node_parent.getChildrenAt(index).setParent(new_internal_node);
							new_index++;
						}
						new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(node_parent.genreCount()));
						node_parent.getChildrenAt(node_parent.genreCount()).setParent(new_internal_node);

						String genre_to_push_up = node_parent.genreAtIndex(order_2); // take the middle element id of the old internal node

						// removing the copied genres and children
						int node_count = node_parent.genreCount();
						for(int old_index = order_2; old_index < node_count; old_index++){
							node_parent.getAllGenres().remove(order_2);
						}
						for(int old_index = order_2+1; old_index <= node_count; old_index++){
							node_parent.getAllChildren().remove(order_2+1);
						}

						PlaylistNodeSecondaryIndex node_up_parent = (PlaylistNodeSecondaryIndex) up_parent;

						// add the id and point the next to the new internal node
						for (int index = 0; index <= node_up_parent.genreCount(); index++) {
							if( node_up_parent.genreAtIndex(index) == "Not Valid Index!!!"){
								node_up_parent.getAllGenres().add(genre_to_push_up);
								node_up_parent.getAllChildren().add(new_internal_node);
								break;
							}
							if( node_up_parent.genreAtIndex(index).compareTo(genre_to_push_up) >= 0)
							{
								node_up_parent.getAllGenres().add(index, genre_to_push_up);
								node_up_parent.getAllChildren().add(index + 1, new_internal_node);
								node_up_parent.getAllChildren().set(index, parent);
								break;
							}
						}
						node_parent = (PlaylistNodeSecondaryIndex) up_parent;
						parent = up_parent;
					}
					// if the internal node is root (sus internal node)
					if(up_parent == null){
						PlaylistNodeSecondaryIndex new_internal_node = new PlaylistNodeSecondaryIndex(null);
						PlaylistNodeSecondaryIndex new_root = new PlaylistNodeSecondaryIndex(null);

						// copy the last number of order elements to new internal node
						new_index=0;
						for (int index = order_2 + 1 ; index < node_parent.genreCount() ; index++)
						{
							new_internal_node.getAllGenres().add(new_index,node_parent.genreAtIndex(index));
							new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(index));
							node_parent.getChildrenAt(index).setParent(new_internal_node);
							new_index++;
						}
						new_internal_node.getAllChildren().add(new_index,node_parent.getChildrenAt(node_parent.genreCount()));
						node_parent.getChildrenAt(node_parent.genreCount()).setParent(new_internal_node);

						String genre_to_push_up = node_parent.genreAtIndex(order_2); // take the middle element id of the old internal node

						// removing the copied genres and children
						int node_count = node_parent.genreCount();
						for(int old_index = order_2; old_index < node_count; old_index++){
							node_parent.getAllGenres().remove(order_2);
						}
						for(int old_index = order_2+1; old_index <= node_count; old_index++){
							node_parent.getAllChildren().remove(order_2+1);
						}

						new_root.getAllGenres().add(genre_to_push_up);
						new_root.getAllChildren().add(parent);
						new_root.getAllChildren().add(new_internal_node);
						parent.setParent(new_root);
						new_internal_node.setParent(new_root);
						secondaryRoot = new_root;
						break;
					}
				}
			}
		}

		// leaf is root (sus leaf)
		if(insert_leaf_2.getParent() == null){
			int leaf_genre_count = insert_leaf_2.genreCount();

			// if number of genres in leaf is bigger than the possible number of genres, namely overflow
			if (leaf_genre_count > pos_genre_number){
				PlaylistNodeSecondaryLeaf new_leaf = new PlaylistNodeSecondaryLeaf(null);
				PlaylistNodeSecondaryIndex new_root = new PlaylistNodeSecondaryIndex(null);

				// dividing the leaf
				int new_index=0;
				for(int old_index = order_2; old_index < leaf_genre_count; old_index++){
					ArrayList<CengSong> divided_bucket = insert_leaf_2.songsAtIndex(old_index);
					for(int i = 0; i < divided_bucket.size(); i++){
						CengSong divided_song = divided_bucket.get(i);
						new_leaf.addSong(new_index, divided_song);
					}
					new_index++;
				}

				// removing the copied elements
				for(int old_index = order_2; old_index < leaf_genre_count; old_index++){
					insert_leaf_2.getSongBucket().remove(order_2);
				}

				String genre_to_copy_up = new_leaf.genreAtIndex(0); // take the first id of the new leaf

				insert_leaf_2.setParent(new_root);
				new_leaf.setParent(new_root);
				new_root.getAllGenres().add(genre_to_copy_up);
				new_root.getAllChildren().add(insert_leaf_2);
				new_root.getAllChildren().add(new_leaf);
				secondaryRoot = new_root;
			}
		}


		return;
	}
	
	public CengSong searchSong(Integer audioId) {
		// TODO: Implement this method
		// find the song with the searched audioId in primary B+ tree
		// return value will not be tested, just print according to the specifications

		searchPrimary(audioId, primaryRoot, 0);

		return null;
	}
	
	
	public void printPrimaryPlaylist() {
		// TODO: Implement this method
		// print the primary B+ tree in Depth-first order

		printPrimary(primaryRoot, 0);

		return;
	}

	
	public void printSecondaryPlaylist() {
		// TODO: Implement this method
		// print the secondary B+ tree in Depth-first order

		printSecondary(secondaryRoot, 0);
		return;
	}
	
	// Extra functions if needed
	private void printPrimary(PlaylistNode node, int tab_count) {

		if (node.getType() == PlaylistNodeType.Leaf){
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("<data>");
			PlaylistNodePrimaryLeaf leaf_node = (PlaylistNodePrimaryLeaf) node;
			for (int index = 0; index < leaf_node.getSongs().size(); index++){
				CengSong song = leaf_node.songAtIndex(index);
				for (int count = 0; count < tab_count; count++) {
					System.out.print("\t");
				}
				System.out.println("<record>"+song.audioId()+"|"+song.genre()+"|"+song.songName()+"|"+song.artist()+"</record>");
			}
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("</data>");
		}
		else if (node.getType() != PlaylistNodeType.Leaf){
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("<index>");
			PlaylistNodePrimaryIndex internal_node = (PlaylistNodePrimaryIndex) node;
			for (int index = 0; index < internal_node.getAllAudioIds().size(); index++){
				for (int count = 0; count < tab_count; count++) {
					System.out.print("\t");
				}
				System.out.println(internal_node.audioIdAtIndex(index));
			}
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("</index>");
			tab_count++;
			for (int index = 0; index < internal_node.getAllChildren().size(); index++) {
				printPrimary((internal_node.getChildrenAt(index)), tab_count);
			}
		}
	}

	private void printSecondary(PlaylistNode node, int tab_count) {

		if (node.getType() == PlaylistNodeType.Leaf){
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("<data>");
			PlaylistNodeSecondaryLeaf leaf_node = (PlaylistNodeSecondaryLeaf) node;
			for (int index = 0; index < leaf_node.getSongBucket().size(); index++){
				ArrayList<CengSong> bucket = leaf_node.songsAtIndex(index);
				for (int count = 0; count < tab_count; count++) {
					System.out.print("\t");
				}
				System.out.println(leaf_node.genreAtIndex(index));
				for(int i = 0; i<bucket.size(); i++){
					CengSong song = bucket.get(i);
					for (int count = 0; count < tab_count+1; count++) {
						System.out.print("\t");
					}
					System.out.println("<record>"+song.audioId()+"|"+song.genre()+"|"+song.songName()+"|"+song.artist()+"</record>");
				}
			}
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("</data>");
		}
		else if (node.getType() != PlaylistNodeType.Leaf){
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("<index>");
			PlaylistNodeSecondaryIndex internal_node = (PlaylistNodeSecondaryIndex) node;
			for (int index = 0; index < internal_node.getAllGenres().size(); index++){
				for (int count = 0; count < tab_count; count++) {
					System.out.print("\t");
				}
				System.out.println(internal_node.genreAtIndex(index));
			}
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("</index>");
			tab_count++;
			for (int index = 0; index < internal_node.getAllChildren().size(); index++) {
				printSecondary((internal_node.getChildrenAt(index)), tab_count);
			}
		}
	}

	private void searchPrimary(Integer audio_id ,PlaylistNode node, int tab_count) {

		if (node.getType() == PlaylistNodeType.Leaf){
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("<data>");
			PlaylistNodePrimaryLeaf leaf_node = (PlaylistNodePrimaryLeaf) node;
			for (int index = 0; index < leaf_node.getSongs().size(); index++){
				CengSong song = leaf_node.songAtIndex(index);
				if(song.audioId() == audio_id){
					for (int count = 0; count < tab_count; count++) {
						System.out.print("\t");
					}
					System.out.println("<record>"+song.audioId()+"|"+song.genre()+"|"+song.songName()+"|"+song.artist()+"</record>");
				}
			}
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("</data>");
		}
		else if (node.getType() != PlaylistNodeType.Leaf){
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("<index>");
			PlaylistNodePrimaryIndex internal_node = (PlaylistNodePrimaryIndex) node;
			for (int index = 0; index < internal_node.getAllAudioIds().size(); index++){
				for (int count = 0; count < tab_count; count++) {
					System.out.print("\t");
				}
				System.out.println(internal_node.audioIdAtIndex(index));
			}
			for (int count = 0; count < tab_count; count++) {
				System.out.print("\t");
			}
			System.out.println("</index>");
			tab_count++;
			for (int index = 0; index <= internal_node.getAllChildren().size(); index++) {
				if(internal_node.audioIdAtIndex(index) == -1){
					searchPrimary(audio_id, internal_node.getChildrenAt(index), tab_count);
					break;
				}
				if(internal_node.audioIdAtIndex(index) > audio_id){
					searchPrimary(audio_id, internal_node.getChildrenAt(index), tab_count);
					break;
				}
			}
		}
	}

}


