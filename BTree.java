package bTree;

import java.util.Collections;
import java.util.List;

import javax.tools.DocumentationTool.Location;

/***
 * B树
 * 
 * 出现过的问题：
 * 
 * 1.插入：因为递归放在了循环里，所以执行完递归后，一定要在递归语句后面加上break。跳出循环；
 *
 * 此外！因为while循环中还有一层for循环，当跳出for循环后，仍然在循环中。为了跳出最外层，增加标记，在插入数据后，改变标记的值，在所有插入操作结束后，恢复标记。
 * 
 * 2.插入过程中父节点孩子的移动，当节点位于父节点的末尾，只需要在末尾扩容，不需要移动。
 * 
 * 3.分裂：如果分裂的节点有孩子，那么新创建的节点应该为非叶子节点。
 * 
 * 4.删除：删除内部结点的前两种情况（即儿子节点至少有一个关键字是足够的）：因为当前节点的关键字替换为下面节点的关键字，递归删除的是下面节点的关键字，所以，递归的节点不应该是当前节点，应该是，当前节点的儿子节点
 * 
 * 5.删除：删除内部结点的第三种情况（合并）：当关键字的儿子节点是叶子节点，不需要再处理儿子的儿子们了，会报空指针。
 * 
 * 6.理解错误：删除时，当孩子节点的关键字数足够，找出孩子节点为根的子树的前驱或后继
 * 
 * 待完善：
 * 
 * 1.插入部分:判断当前节点的下一个要访问的位置是否未满，满则分裂。这样要比判断当前节点是否为满结构要清晰，写起来方便，易理解。
 * 
 * 2.删除部分：可读性较低
 * 
 * @author LaZY（李志一）
 * 
 * 
 */
public class BTree {
	public static BTree_node root;// B树的根节点

	public final static int t = 2;// B树的最小度

	private static boolean sign = false;// 标记：

	private static final Disk DISK = new Disk();// 磁盘

	/***
	 * B树的初始化
	 */
	public void init() {
		BTree_node node = new BTree_node();
		node.leaf = true;// 当前节点设为叶子节点
		node.countKey = 0;
		this.root = node;
		// HED 磁盘写
		this.DISK.WRITE(this.root);
	}

	/**
	 * 判断B树是否为空树。B树的关键字个数为0，即为空树。
	 * 
	 * @return t:是空树； f:不是空树
	 */
	public boolean isEmpty() {
		if (this.root.key.size() == 0) {
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

			// HED 磁盘写
			this.DISK.WRITE(r);

		} else if (r.isFull()) {// 满根即分裂/注意：满根的情况必须先分裂再插入，否则儿子不够分
			BTree_node newRoot = splitRoot(r);// 分裂根节点，并产生新根
			this.root = newRoot;// B树的根节点指向新根
			insert(k, this.root);// 从根节点再次查找
		} else {
			r.key.add(k);// 直接插入
			Collections.sort(r.key);// 插入后排序（递增）
			r.countKey++;// 增加关键字数量//

			// HED 磁盘写
			this.DISK.WRITE(r);

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

			// HED 磁盘写
			this.DISK.WRITE(node);

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

		// HED 磁盘写
		this.DISK.WRITE(root);
		this.DISK.WRITE(newNode);
		this.DISK.WRITE(newRoot);

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

		// HED 磁盘写
		this.DISK.WRITE(root);
		this.DISK.WRITE(parent);
		this.DISK.WRITE(newNode);

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

		// HED 磁盘写
		this.DISK.WRITE(root);
		this.DISK.WRITE(parent);
		this.DISK.WRITE(newNode);

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
		BTree_node newNode;
		if (node.children.size() != 0) {
			newNode = getRNode();// 如果有孩子，则创建的是非叶子节点
		} else {
			newNode = getLeftNode();// 创建新的被分裂出的节点（一定是叶子节点）
		}
		for (int i = middle - 1 + 1; i < size; i++) {// (middle-1+1）：集合中的元素从0开始，middle-1：要提升的关键字，middle-1+1：要提升关键字后一个关键字
			int key = node.key.get(i);// 把要提升的关键字后面的关键字赋给分裂出的节点（刚创建的）
			newNode.key.add(key);
		}
		newNode.countKey = newKeyCount;
		return newNode;
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

	public void delete(int k) {
		if (isEmpty()) {
			System.out.println("你访问的是空树");// 空树不允许执行删除操作
		} else {
			delete(k, this.root);
		}
	}

	private void delete(int k, BTree_node node) {
		int keySize = node.key.size();
		for (int i = 0; i < keySize; i++) {// 从节点第一个关键字开始查找
			Integer key = node.key.get(i);
			if (k < key) {// 要删除的关键字比当前关键字小，就在关键字的前一个子节点（孩子）中寻找

				BTree_node beforeChild = node.children.get(i);
				int beforeSize = beforeChild.key.size();
				if (enoughKeys(beforeSize)) {// 对关键字足够的节点，不调整。
					delete(k, node.children.get(i));
					break;// 执行完操作后退出循环
				} else {

					// 对关键字较少的节点，进行调整。
					BTree_node af_befChild = node.children.get(i + 1);// 要删除的关键字所在子树根节点的后一个节点
					int size = af_befChild.key.size();
					if (enoughKeys(size)) {
						node = del_handleBy_aft(node, key, i, beforeChild, af_befChild);
						delete(k, node);
					} else if (!(enoughKeys(size))) {// 关键字不足
						node = del_mergeBy_aft(node, key, i, beforeChild, af_befChild);
						delete(k, node);
					}
					// else if(!(enoughKeys(size))&&node.isRoot()){//关键字不足，且是根节点
					//
					// }

				}

			} else if (k == key) {// 找到要删除的节点

				// 删除关键字

				if (node.isLefe()) {
					if (node.isRoot() && node.key.size() == 1) {
						System.out.println("根节点只有一个节点，不许删除");
					} else {
						node.key.remove(i);// 是叶子节点，直接删除索引下的关键字
						node.countKey--;
						// HED 修改标记
						this.sign = true;
					}
				} else {// 是内部结点
					BTree_node beforeChild = node.children.get(i);
					BTree_node afterChild = node.children.get(i + 1);
					int beforeKeyCount = beforeChild.key.size();
					int afterKeyCount = afterChild.key.size();
					/*
					 * 要删除关键字的前一个节点c的关键字数量不少于t，
					 * 
					 * 用c的末尾关键字替换要删除的关键字
					 * 
					 * 仍然从当前节点开始 递归删除c
					 *
					 */
					if (enoughKeys(beforeKeyCount)) {
						int size = beforeChild.key.size();

						Integer lastKey = lefkey(beforeChild);
						// Integer lastKey = beforeChild.key.get(size - 1);//
						// 关键字前面孩子的最后一个关键字
						node.key.set(i, lastKey);
						delete(lastKey, beforeChild);
					}
					/*
					 * 要删除关键字的后一个节点c的关键字数量不少于t，
					 * 
					 * 用c的首关键字替换要删除的关键字
					 * 
					 * 仍然从当前节点开始 递归删除c
					 *
					 */
					else if (!(enoughKeys(beforeKeyCount)) && enoughKeys(afterKeyCount)) {
						int size = afterChild.key.size();

						Integer firstKey = rigkey(afterChild);
						// Integer firstKey = afterChild.key.get(0);//
						// 关键字后面孩子的第一个关键字
						node.key.set(i, firstKey);
						delete(firstKey, afterChild);// 递归删除儿子节点的关键字
					}
					/*
					 * 根节点只有一关键字
					 * 
					 * 合并后，让跟节点指向合并后的节点
					 */

					else if (node.isRoot() && node.key.size() == 1 && !(enoughKeys(beforeKeyCount))
							&& !(enoughKeys(afterKeyCount))) {
						beforeChild.key.add(k);// 要删除的关键字，插入到前一个节点中
						for (int j = 0; j < afterChild.key.size(); j++) {// 后一个节点的所有关键字保存到前一个节点中
							Integer keyy = afterChild.key.get(j);
							// HED 操作的不是对象本身
							beforeChild.key.add(keyy);
						}
						if (beforeChild.isLefe()) {// 当儿子节点是叶子节点，不需要处理儿子的儿子们

						} else {
							for (int j = 0; j < afterChild.children.size(); j++) {// 后一个节点的所有儿子保存到前一个节点中
								BTree_node child = afterChild.children.get(j);
								beforeChild.children.add(child);
								child.parent = beforeChild;
							}
						}
						beforeChild.countKey += (1 + afterChild.getCountKey());// 修改前一个儿子的关键字的数量
						this.root = beforeChild;
						delete(k, this.root);
					}
					/*
					 * 要删除关键字的前后节点的关键字都小于t
					 * 
					 * 则将要删除的关键字、前面节点、后面节点合并
					 * 
					 */
					else if (!(enoughKeys(beforeKeyCount)) && !(enoughKeys(afterKeyCount))) {
						// HED 操作的不是对象本身
						beforeChild.key.add(k);// 要删除的关键字，插入到前一个节点中
						for (int j = 0; j < afterChild.key.size(); j++) {// 后一个节点的所有关键字保存到前一个节点中
							Integer keyy = afterChild.key.get(j);
							// HED 操作的不是对象本身
							beforeChild.key.add(keyy);
						}
						if (beforeChild.isLefe()) {// 当儿子节点是叶子节点，不需要处理儿子的儿子们

						} else {
							for (int j = 0; j < afterChild.children.size(); j++) {// 后一个节点的所有儿子保存到前一个节点中
								BTree_node child = afterChild.children.get(j);
								beforeChild.children.add(child);
								child.parent = beforeChild;
							}
						}
						beforeChild.countKey += (1 + afterChild.getCountKey());// 修改前一个儿子的关键字的数量
						node.key.remove(i);// 从节点个中删除要删掉的关键字
						node.countKey--;// 修改节点的关键字数量
						node.children.remove(i + 1);// 删除要删除关键字后面的儿子节点

						// 修改后的节点赋值给原节点。不使用递归的原因1.易错。2.插入以后会跳出循环，不在执行剩下的内容
						// BTree_node parent = node.parent;
						// int index = getChildLocation(node);//
						// 节点在其父节点的孩子中的位置（索引）
						// parent.children.set(index, node);
						delete(k, beforeChild);
					}

				}
				break;
			}
			break;
		}
		// HED 需要在退出循环后不进行下面的代码
		if (this.sign) {

		} else {
			Integer key = node.key.get(keySize - 1);
			if (k > keySize) {// 插入的数据比所有关键字大，在最后一个孩子中寻找

				BTree_node afterChild = node.children.get(keySize);
				int afterSize = afterChild.key.size();
				if (enoughKeys(afterSize)) {// 对关键字足够的节点，不调整。
					delete(k, node.children.get(keySize));
				} else {

					// 对关键字较少的节点，进行调整。
					BTree_node bef_aftChild = node.children.get(keySize - 1);// 要删除的关键字所在子树根节点的前一个节点
					int size = bef_aftChild.key.size();
					if (enoughKeys(size)) {
						node = del_handleBy_bef(node, key, keySize - 1, afterChild, bef_aftChild);
						delete(k, node);
					} else if (!(enoughKeys(size))) {// 关键字不足
						node = del_mergeBy_bef(node, key, keySize - 1, afterChild, bef_aftChild);
						delete(k, node);
					}

				}

			}
		}
	}

	/***
	 * 节点中的关键字个数>=t（至少有t）个关键字
	 * 
	 * @param quentity
	 *            节点中的关键字数量
	 * @return t:足够 f：不够
	 */
	private boolean enoughKeys(int quentity) {
		if (quentity == this.t || quentity > this.t) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * 向下查找过程中，要删除的关键字，在当前关键字的前一个节点的子树中，然而，此节点的关键字数不足够。根据前一个节点的后一个节点进行调整
	 * 
	 * @param par
	 *            当前节点
	 * @param k
	 *            当前关键字
	 * @param i
	 *            关键字的所在索引
	 * @param chil
	 *            删除关键字所在的节点
	 * @param aft_chil
	 *            删除关键字所在的节点的后一个节点
	 * @return 返回调整后的当前节点
	 */
	private BTree_node del_handleBy_aft(BTree_node par, int k, int i, BTree_node chil, BTree_node aft_chil) {// XXX
																												// 参数待优化
		chil.key.add(k);// 父节点的关键字添加到孩子节点中
		chil.countKey++;
		par.key.remove(i);// 从父节点删掉刚移除的关键字
		par.countKey--;
		Integer key = aft_chil.key.get(0);// 儿子节点的后一个节点的首关键字插入到父节点
		par.key.add(key);
		par.countKey++;
		Collections.sort(par.key);
		aft_chil.key.remove(0);// 把移除的后一个儿子节点的关键字删掉
		aft_chil.countKey--;
		if (chil.isLefe()) {

		} else {// 儿子节点不是叶子节点，处理儿子节点的儿子
			BTree_node child = aft_chil.children.get(0);// 后一个儿子节点的第一个儿子添加到儿子节点中
			chil.children.add(child);
			aft_chil.children.remove(0);
		}
		return par;
	}

	/***
	 * 向下查找过程中，要删除的关键字，在当前关键字的后一个节点的子树中，然而，此节点的关键字数不足够。根据后一个节点的前一个节点进行调整
	 * 
	 * @param par
	 *            当前节点
	 * @param k
	 *            当前关键字
	 * @param i
	 *            关键字的所在索引
	 * @param chil
	 *            删除关键字所在的节点
	 * @param bef_chil
	 *            删除关键字所在的节点的前一个节点
	 * @return 返回调整后的当前节点
	 */
	private BTree_node del_handleBy_bef(BTree_node par, int k, int i, BTree_node chil, BTree_node bef_chil) {// XXX
																												// 参数待优化
		chil.key.add(k);// 父节点的关键字添加到孩子节点中
		Collections.sort(chil.key);
		chil.countKey++;
		par.key.remove(i);// 从父节点删掉刚移除的关键字
		par.countKey--;
		int size = bef_chil.key.size();
		Integer key = bef_chil.key.get(size - 1);// 儿子节点的前一个节点的末尾关键字插入到父节点
		par.key.add(key);
		Collections.sort(par.key);
		par.countKey++;
		bef_chil.key.remove(size - 1);// 把移除的前一个儿子节点的关键字删掉
		bef_chil.countKey--;
		if (chil.isLefe()) {

		} else {// 儿子节点不是叶子节点，处理儿子节点的儿子
			BTree_node child = bef_chil.children.get(bef_chil.children.size() - 1);// 前一个儿子节点的第最后一个儿子添加到儿子节点中
			chil.children.add(child);// 假数据,扩容
			for (int j = chil.children.size() - 1; j >= 0; j--) {// 从第一个元素向后移动
				BTree_node c = chil.children.get(j - 1);
				chil.children.set(j, c);
			}
			chil.children.set(0, child);// 真数据
			bef_chil.children.remove(bef_chil.children.size() - 1);
		}
		return par;
	}

	/***
	 * 父节点关键字的子孩子的右边兄弟的关键字也不够，将父节点的此关键字，孩子，和孩子的右兄弟合并,合并到孩子中
	 * 
	 * @param par
	 *            父节点（当前节点）
	 * @param k
	 *            父节点的关键字
	 * @param i
	 *            关键字位置
	 * @param chil
	 *            关键字前的子孩子
	 * @param aft_chil
	 *            孩子的右兄弟
	 * @return 修改后的父节点
	 */
	private BTree_node del_mergeBy_aft(BTree_node par, int k, int i, BTree_node chil, BTree_node aft_chil) {
		chil.key.add(k);// 添加父节点的关键字
		Collections.sort(chil.key);
		chil.countKey++;
		par.key.remove(i);// 父节点的关键字移除
		par.countKey--;
		// 此时父节点中还多了一个孩子
		List<Integer> keys = aft_chil.key;
		List<BTree_node> children = aft_chil.children;
		chil.key.addAll(keys);// 添加右兄弟的所有关键字
		Collections.sort(chil.key);
		chil.countKey += keys.size();
		if (chil.isLefe()) {

		} else {
			for (int j = 0; j < children.size(); j++) {// 添加右兄弟的所有孩子
				BTree_node child = children.get(j);
				chil.children.add(child);
				child.parent = chil;
			}
		}
		par.children.remove(i + 1);// 从父节点中删掉右兄弟
		if (par.isRoot()) {
			this.root = chil;
			chil.parent = null;
			return this.root;
		} else {
			return par;
		}
	}

	/***
	 * 父节点关键字后面的子孩子的左边兄弟的关键字也不够，将父节点的此关键字，孩子，和孩子的左兄弟合并
	 * 
	 * @param par
	 *            父节点（当前节点）
	 * @param k
	 *            父节点的关键字
	 * @param i
	 *            关键字位置
	 * @param chil
	 *            关键字后的子孩子
	 * @param aft_chil
	 *            孩子的左兄弟
	 * @return 修改后的父节点
	 */
	private BTree_node del_mergeBy_bef(BTree_node par, int k, int i, BTree_node chil, BTree_node bef_chil) {
		chil.key.add(k);// 添加父节点的关键字
		Collections.sort(chil.key);
		chil.countKey++;
		par.key.remove(i);// 父节点的关键字移除
		par.countKey--;
		// 此时父节点中还多了一个孩子
		List<Integer> keys = bef_chil.key;
		List<BTree_node> children = bef_chil.children;
		chil.key.addAll(keys);// 添加左兄弟的所有关键字
		Collections.sort(chil.key);
		chil.countKey += keys.size();
		if (chil.isLefe()) {

		} else {// 添加右兄弟的所有孩子
			List<BTree_node> c_children = chil.getChildren();
			bef_chil.children.addAll(c_children);
			List<BTree_node> bef_children = bef_chil.getChildren();
			chil.children.clear();
			chil.children.addAll(bef_children);

			// }
		}

		par.children.remove(i);// 从父节点中删掉右兄弟
		if (par.isRoot()) {
			this.root = chil;
			chil.parent = null;
			return this.root;
		} else {
			return par;
		}
	}

	public int rigkey(BTree_node node) {
		int key;
		while (!(node.isLeaf())) {
			node = node.children.get(0);
		}
		key = node.key.get(0);
		return key;
	}

	public int lefkey(BTree_node node) {
		int key;
		while (!(node.isLeaf())) {
			int size = node.children.size();
			node = node.children.get(size - 1);
		}
		int size = node.key.size();
		key = node.key.get(size - 1);
		return key;
	}

}
