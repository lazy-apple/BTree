package bTree;

import java.util.Collections;
import java.util.List;

import javax.tools.DocumentationTool.Location;

/***
 * B树
 * 
 * @author LaZY（李志一）
 * 
 *         出现过的问题：
 * 
 *         1.插入：因为递归放在了循环里，所以执行完递归后，一定要在递归语句后面加上break。跳出循环；
 *
 *         此外！因为while循环中还有一层for循环，当跳出for循环后，仍然在循环中。为了跳出最外层，增加标记，在插入数据后，改变标记的值，在所有插入操作结束后，恢复标记。
 * 
 *         2.插入过程中父节点孩子的移动，当节点位于父节点的末尾，只需要在末尾扩容，不需要移动。
 * 
 * 
 *         待完善：
 * 
 *         1.插入部分:判断当前节点的下一个要访问的位置是否未满，满则分裂。这样要比判断当前节点是否为满结构要清晰，写起来方便，易理解。
 */
public class BTree {
	public final static int t = 3;// B树的最小度

	public static BTree_node root;// B树的根节点

	private static boolean sign = false;// 标记：

	/***
	 * B树的初始化
	 */
	public void init() {
		BTree_node node = new BTree_node();
		node.leaf = true;// 当前节点设为叶子节点
		node.countKey = 0;
		this.root = node;
	}

	/**
	 * 判断B树是否为空树。B树的关键字个数为0，即为空树。
	 * 
	 * @return t:是空树； f:不是空树
	 */
	public boolean isEmpty() {
		if (this.root.countKey == 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 获得一片叶子节点（新能创建的）
	 * 
	 * @return 叶子
	 */
	public BTree_node getLeftNode() {
		BTree_node node = new BTree_node();
		node.leaf = true;
		return node;
	}

	/***
	 * 获得一个内部(非树叶)节点（新创建的）
	 * 
	 * @return 内部结点
	 */
	public BTree_node getRNode() {
		BTree_node node = new BTree_node();
		node.leaf = false;
		return node;
	}

	/***
	 * 向B树中插入关键字
	 * 
	 * @param k
	 *            要插入的关键字
	 */
	public void insert(int k) {
		insert(k, this.root);
		sign = false;// 完成操作后，标记恢复
	}

	/***
	 * 在指定节点中插入关键字
	 * 
	 * @param k
	 *            要插入的关键字
	 * @param node
	 */
	public void insert(int k, BTree_node node) {
		while (!(node.isLefe())) {// 插入只能在叶子节点进行，当不是叶子节点就一直查找。
			// 自上而下分裂满节点
			if (node.isFull()) {// 插入过程发现满节点，就将其分裂
				if (node.isRoot()) {// 满节点为根节点
					BTree_node newRoot = splitRoot(node);// 分裂根节点
					this.root = newRoot;
					insert(k, this.root);// 从新节点重新查找
					break;
				} else {// 满节点为内部节点
					BTree_node parent = splitInternalNode(node);// 满节点分裂后，在原本的父节点中重新查找
					insert(k, parent);
					break;
				}
				// 向下查找
			} else {
				int keySize = node.key.size();
				for (int i = 0; i < keySize; i++) {
					Integer key = node.key.get(i);
					if (k < key) {// 插入的数据比当前关键字小，就在关键字的前一个子孩子中寻找
						insert(k, node.children.get(i));
						break;
					} else if (k == key) {
						System.out.println("已存在");
						break;
					}
				}
				if (sign) {// 插入结束后退出循环（最外层）
					break;
				} else {

					if (k > node.key.get(keySize - 1)) {// 插入的数据比所有关键字大，在最后一个孩子中寻找
						insert(k, node.children.get(keySize));
						break;
					}
					if (sign) {
						break;
					}
				}
			}
		}
		// 在叶子节点中插入关键字
		if (node.isLefe()) {// 不能将此句省略，因为没有此句递归结束后会执行
			if (node.isRoot()) {// 向根节点插入
				insertInRoot(k);
			} else {
				insertInLefe(k, node);
			}
			sign = true;// 插入结束后退出循环（最外层）
		}

	}

	/***
	 * 插入过程中处理根节点的方法
	 * 
	 * @param k
	 *            要插入的关键字
	 */
	public void insertInRoot(int k) {
		BTree_node r = this.root;
		if (this.isEmpty()) {// 空树（根节点无关键字）
			r.key.add(k);// 直接插入
			r.countKey = 1;// 增加关键字数量
		} else if (r.isFull()) {// 满根即分裂/注意：满根的情况必须先分裂再插入，否则儿子不够分
			BTree_node newRoot = splitRoot(r);// 分裂根节点，并产生新根
			this.root = newRoot;// B树的根节点指向新根
			insert(k, this.root);// 从根节点再次查找
		} else {
			r.key.add(k);// 直接插入
			Collections.sort(r.key);// 插入后排序（递增）
			r.countKey++;// 增加关键字数量//
		}
	}

	/***
	 * 在叶子节点中插入关键字
	 * 
	 * @param k
	 *            要插入的关键字
	 * @param node
	 *            叶子节点
	 */
	public void insertInLefe(int k, BTree_node node) {
		if (node.isFull()) {// 满根即分裂/注意：满根的情况必须先分裂再插入，否则儿子不够分
			BTree_node parent = splitlefeNode(node);// 将叶子节点分裂，并在其父节点重新查找
			insert(k, parent);
			// BTree_node newRoot = splitRoot(node);// 分裂根节点，并产生新根
			// this.root = newRoot;// B树的根节点指向新根
			// insert(k);// 从根节点再次查找
		} else {
			node.key.add(k);// 直接插入
			Collections.sort(node.key);// 插入后排序（递增）
			node.countKey++;// 增加关键字数量
		}
	}

	/***
	 * 分裂根节点，并产生新根
	 * 
	 * @param root
	 *            根节点
	 * @return 新的根节点
	 */
	public BTree_node splitRoot(BTree_node root) {
		int location = root.getMiddleLocation();// 关键字的所在位置
		int middleKey = root.key.get(location - 1);// 要提升的关键字
		BTree_node newNode = makeNewNodeKey(location, root);// 生成分裂节点，赋值，修改关键字数量
		if (!(root.isLefe())) {
			newNode = makeNewNodeChildren(location, root, newNode);// 把分出的儿子赋给新生成的节点
			root = removeChildren(location, root);// 删掉分裂出的儿子
		}
		root = removeKeys(location, root);// 删掉分裂出的关键字并修改关键字数量
		BTree_node newRoot = getRNode();// 新生成的父节点
		newRoot.children.add(root);// 新的父节点（根节点）的儿子指针指向两个儿子
		newRoot.children.add(newNode);
		root.parent = newRoot;// 儿子指针的父亲指针指向新父亲
		newNode.parent = newRoot;
		newRoot.key.add(middleKey);// 为新的根节点赋关键字
		newRoot.countKey = 1;// 修改新的关键字的数量
		return newRoot;
	}

	/***
	 * 分裂普通叶子节点
	 * 
	 * @param root
	 *            被分裂的节点
	 * @return 被分裂节点的父亲
	 */
	public BTree_node splitlefeNode(BTree_node root) {
		BTree_node parent = root.getParent();// 当前节点（要被分裂的节点）的父节点
		int nodeIndex = getChildLocation(root);// 当前节点是其父节点的第几个孩子（索引）
		int location = root.getMiddleLocation();// 关键字的所在位置
		int middleKey = root.key.get(location - 1);// 要提升的关键字
		BTree_node newNode = makeNewNodeKey(location, root);// 生成分裂节点，赋值，修改关键字数量
		root = removeKeys(location, root);// 删掉分裂出的关键字并修改关键字数量
		parent = moveChildren(nodeIndex, parent);// 父节点的孩子向后移动
		parent.children.set(nodeIndex + 1, newNode);// 父节点的孩子指针指向新分裂出的节点
		newNode.parent = parent;// 分裂出的节点的双亲指针指向新的父节点
		parent.key.add(middleKey);// 父节点中插入提升的关键字
		Collections.sort(parent.key);// 为父节点插入后的关键字排序
		parent.countKey = 1;// 修改父节点的关键字的数量
		return parent;
	}

	/***
	 * 分裂内部结点
	 * 
	 * @param root
	 *            要被分裂的节点
	 * @return 被分裂节点的父亲
	 */
	public BTree_node splitInternalNode(BTree_node root) {
		BTree_node parent = root.getParent();// 当前节点（要被分裂的节点）的父节点
		int nodeIndex = getChildLocation(root);// 当前节点是其父节点的第几个孩子（索引）
		int location = root.getMiddleLocation();// 关键字的所在位置
		int middleKey = root.key.get(location - 1);// 要提升的关键字
		BTree_node newNode = makeNewNodeKey(location, root);// 生成分裂节点，赋值，修改关键字数量

		newNode = makeNewNodeChildren(location, root, newNode);// 把分出的儿子赋给新生成的节点
		root = removeChildren(location, root);// 删掉分裂出的儿子

		root = removeKeys(location, root);// 删掉分裂出的关键字并修改关键字数量
		parent = moveChildren(nodeIndex, parent);// 父节点的孩子向后移动
		parent.children.set(nodeIndex + 1, newNode);// 父节点的孩子指针指向新分裂出的节点
		newNode.parent = parent;// 分裂出的节点的双亲指针指向新的父节点

		parent.key.add(middleKey);// 父节点中插入提升的关键字
		Collections.sort(parent.key);// 为父节点插入后的关键字排序
		parent.countKey = 1;// 修改父节点的关键字的数量
		return parent;
	}

	/**
	 * 当前节点是其父节点的第几个孩子（索引）
	 * 
	 * @param node
	 *            当前节点
	 * @return 在父节点中的索引
	 */
	public int getChildLocation(BTree_node node) {
		int index = 0;
		BTree_node parent = node.getParent();
		for (int i = 0; i < parent.children.size(); i++) {
			if (parent.children.get(i) == node) {
				index = i;
			} else {
			}
		}
		return index;
	}

	/***
	 * 移动被分裂节点后面的孩子（向后移动）
	 * 
	 * @param index
	 *            被分裂节点在域中的索引
	 * @param parent
	 *            被分裂节点的父节点
	 * @return 父节点
	 */
	public BTree_node moveChildren(int index, BTree_node parent) {
		int oldsize = parent.children.size();
		if (index != oldsize - 1) {// 被分裂的节点不在末尾
			BTree_node lastchild = parent.children.get(oldsize - 1);
			parent.children.add(lastchild);// x
			for (int i = oldsize - 1; i >= index; i--) {
				BTree_node c = parent.children.get(i);
				parent.children.set(i + 1, c);
			}
		} else {
			parent.children.add(parent);// 在末尾扩容（1个单位）
		}
		return parent;
	}

	/***
	 * 创建新分裂出的节点，并将分裂出的关键字赋给新节点，修改关键字数量。
	 * 
	 * @param middle
	 *            要提升的关键字
	 * @param node
	 *            被分裂的节点
	 * @return 分裂出的节点
	 */
	public BTree_node makeNewNodeKey(int middle, BTree_node node) {
		int size = node.key.size();
		int newKeyCount = size - middle;// 要提升的关键字后面的关键字数量
		BTree_node leftNode = getLeftNode();// 创建新的被分裂出的节点（一定是叶子节点）
		for (int i = middle - 1 + 1; i < size; i++) {// (middle-1+1）：集合中的元素从0开始，middle-1：要提升的关键字，middle-1+1：要提升关键字后一个关键字
			int key = node.key.get(i);// 把要提升的关键字后面的关键字赋给分裂出的节点（刚创建的）
			leftNode.key.add(key);
		}
		leftNode.countKey = newKeyCount;
		return leftNode;
	}

	/***
	 * 为新分裂出的节点赋予孩子
	 * 
	 * @param location
	 *            分裂前的节点中要提升关键字的位置
	 * @param node
	 *            被分裂的节点
	 * @param newNode
	 *            分裂出的新节点
	 * @return 分裂出的新节点
	 */
	public BTree_node makeNewNodeChildren(int location, BTree_node node, BTree_node newNode) {
		int size = node.children.size();
		// int newChildrenCount = size - location;// 被分出的儿子的数量
		for (int i = location - 1 + 1; i < size; i++) {
			BTree_node child = node.children.get(i);
			newNode.children.add(child);
			child.parent = newNode;
		}
		return newNode;
	}

	/***
	 * 删除别分裂的节点的关键字并修改关键字数量
	 * 
	 * @param middle
	 *            要提升的关键字
	 * @param node
	 *            被分裂的节点
	 * @return 被分裂的节点
	 */
	public BTree_node removeKeys(int middle, BTree_node node) {
		int size = node.key.size();
		int removeCount = size - middle + 1;// 要提升的关键字及其后面的关键字数量
		int deleteLocation = middle - 1;// 要删除关键字的索引
		for (int i = middle - 1; i < size; i++) {// middle-1:要提升的关键字
			node.key.remove(deleteLocation);
		}
		node.countKey = size - removeCount;// 调整后的关键字数量为：原本数量-删掉的数量
		return node;
	}

	public BTree_node removeChildren(int location, BTree_node node) {
		int size = node.children.size();
		int deleteLocation = location;// 要删除孩子的索引
		for (int i = location; i < size; i++) {
			node.children.remove(deleteLocation);
		}
		return node;
	}

	/***
	 * 查找指定关键字，在关键字域中第几个位置（不是索引）
	 * 
	 * @param node
	 *            关键字所在的节点
	 * @param key
	 *            指定的关键字
	 * @return 关键字的位置
	 */
	public int getKeyLocation(BTree_node node, int key) {
		int location;
		int index = 0;
		while (node.key.get(index) != key) {
			index++;
		}
		return location = index + 1;
	}
}
