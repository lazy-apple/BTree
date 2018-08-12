package bTree;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author Administrator
 *
 */
public class BTree_test {
	
	public void test(test t) {
		t.string = "123";
	}
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		BTree tree = new BTree();
		tree.init();
		//插入测试
//		tree.insert(51);
//		tree.insert(52);
//		tree.insert(49);
//		tree.insert(48);
//		tree.insert(47);
//		tree.insert(46);
//		tree.insert(45);
//		tree.insert(44);
//		tree.insert(43);
//		tree.insert(42);
//		tree.insert(41);
//		tree.insert(40);
		
		//删除测试
		
		/*//1.删根节点且是叶子节点的关键字
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		
		tree.delete(3);
		tree.delete(2);
		tree.delete(1);*/
		
		/*//2.删除叶子节点的关键字（非根节点）
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.insert(4);
		
		tree.delete(4);
		tree.delete(1);*/
		
		/*//3.删除内部节点的关键字，且关键字后继子树的关键字足够
		tree.insert(10);
		tree.insert(20);
		tree.insert(30);
		tree.insert(40);
		tree.insert(50);
		tree.insert(60);
		
		tree.delete(40);*/
		
		/*//4.删除内部节点的关键字，且关键字的前驱，后继子树的关键字都不够，合并
		tree.insert(10);
		tree.insert(20);
		tree.insert(30);
		tree.insert(40);
		tree.insert(50);
		tree.insert(60);
		
		tree.delete(20);*/
		
		/*//5.删除内部节点的关键字，(根节点，且根节点只有一个关键字)且关键字的前驱，后继子树的关键字都不够，合并
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.insert(4);
		
		tree.delete(4);
		tree.delete(2);*/
		
		/*//6.遍历的时候发现节点不满足关键字个数,但是兄弟节点关键字满足要求
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.insert(4);
		
		tree.delete(1);*/
		
		/*//7.遍历的时候发现节点不满足关键字个数,兄弟节点关键字也不足
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.insert(4);
		
		tree.delete(4);
		tree.delete(3);*/
		
		/*//8.删除时移动儿子的儿子时的情况
		tree.insert(10);
		tree.insert(20);
		tree.insert(30);
		tree.insert(40);
		tree.insert(50);
		tree.insert(60);
		tree.insert(8);
		tree.insert(9);
		tree.insert(6);
		
		tree.delete(20);*/
		
		/*//8.删除时移动儿子的儿子时的情况2
		tree.insert(10);
		tree.insert(20);
		tree.insert(30);
		tree.insert(40);
		tree.insert(50);
		tree.insert(60);
		tree.insert(8);
		tree.insert(9);
		tree.insert(6);
		
		tree.delete(40);*/
		
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.insert(4);
		tree.insert(5);
		tree.insert(6);
		tree.insert(7);
		tree.insert(8);
		tree.insert(9);
		tree.insert(10);
		
		tree.delete(4);
		tree.delete(5);
		tree.delete(6);
		tree.delete(7);
		tree.delete(8);
		tree.delete(9);
		System.out.println("succeful");
	}
	
	
}
