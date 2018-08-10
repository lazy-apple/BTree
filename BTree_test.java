package bTree;

import java.util.ArrayList;
import java.util.List;

public class BTree_test {

	public static void main(String[] args) {
		BTree tree = new BTree();
		tree.init();
		tree.insert(50);
		tree.insert(51);
		tree.insert(52);
		tree.insert(49);
		tree.insert(48);
		tree.insert(47);
		tree.insert(46);
		tree.insert(45);
		tree.insert(44);
		tree.insert(43);
		tree.insert(42);
		tree.insert(41);
		tree.insert(40);
		
		
		System.out.println("succeful");
	}
	
	
}
