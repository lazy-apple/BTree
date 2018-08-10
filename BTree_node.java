package bTree;

import java.util.ArrayList;
import java.util.List;

public class BTree_node {

	public boolean leaf;// 判断是否为树叶
	public List<Integer> key = new ArrayList<>();// 关键字
	public List<BTree_node> children = new ArrayList<>(); // 孩子
	public BTree_node parent;// 父节点
	public int countKey;// 记录key（关键字）的数量

	public BTree_node() {
		this.key = new ArrayList<>();
		this.children = new ArrayList<>();
	}

	public BTree_node getParent() {
		return parent;
	}

	public int getCountKey() {
		return countKey;
	}

	/***
	 * 判断指定节点（的关键字）是否已满。关键字个数为2t-1即满。
	 * 
	 * @param node
	 * @return t:已满； f:未满
	 */
	public boolean isFull() {
		if (this.key.size() == (2 * BTree.t - 1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断当前节点是否为叶子节点
	 * 
	 * @param node
	 * @return t：是叶子节点； f:不是叶子节点
	 */
	public boolean isLefe() {
		if (this.leaf) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * 判断当前节点是否为根节点
	 * 
	 * @return t:是； f:不是
	 */
	public boolean isRoot() {
		if (this == BTree.root) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getMiddleLocation() {
		int size = this.key.size();
		int middle = size/2+1;
		return middle;
	}
}
