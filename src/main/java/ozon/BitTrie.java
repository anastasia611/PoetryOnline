package ozon;

import java.util.ArrayList;
import java.util.List;

/**
 * Trie (prefix tree) with the bit representation of integer numbers.
 *
 * Implements:
 * adding new numbers;
 * initializing from array;
 * searching minimum excluded number value.
 */
public class BitTrie {
    /**
     * How much bits will be in numbers stored in trie (actually specifies the trie height).
     */
    private final int BITS = 31;

    /**
     * Trie data structure, the list of nodes.
     * The first element corresponds to the most significant bit.
     *
     * @see Node
     */
    private List<Node> trie;

    /**
     * The number of leaves in the trie (the number of elements)
     */
    private int trieSize = 1;

    /**
     * Current value of xor for all requests
     *
     * Can be seen that if we have all numbers up to 2^k - 1 in array
     * and perform xor operation with anything less than 2^k,
     * then all these numbers will remain in the array.
     * So that no need to actually apply xor to all elements,
     * we can just store xor of all requests made (x1 ^ x2 ^ x3 ... ^ xn)
     */
    private int xorValue = 0;

    /**
     * Creates trie for bit representation of numbers specified in the array.
     * Left son of each node corresponds "0" bit, right - "1" bit.
     * In each node the number of leaves in the subtree corresponding to this node is stored.
     **
     * Example: [ 0, 1, 5, 6 ]
     *                     (4)
     *                    /
     *                   0
     *                  /
     *                (4)
     *               /   \
     *              0     1
     *             /       \
     *           (2)       (2)
     *          /         /   \
     *         0         0     1
     *        /         /       \
     *      (2)       (1)       (1)
     *     /  \        \        /
     *    0    1        1      0
     *   /      \        \    /
     * (1)      (1)      (1) (1)
     * 0000    0001     0101 0110
     *    0       1        5    6
     *
     * @param array Array with numbers to add to the trie.
     */
    public BitTrie(int[] array) {
        this.trie = new ArrayList<>();

        for (int num : array) {
            addNode(0, num, this.BITS);
        }
    }

    /**
     * Creates empty trie for bit representation on numbers.
     */
    public BitTrie() {
        this.trie = new ArrayList<>();
    }

    /**
     * Applies xor operation.
     *
     * @param x Value for xor operation.
     */
    public void xor(int x) {
        this.xorValue ^= x;
    }

    /**
     * Returns minimum excluded number value for the elements.
     *
     * @return The current mex value.
     */
    public int getMex() {
        return getMexValueForBits(0, BITS, 0);
    }

    /**
     * Adds new element equals mex.
     *
     * @return value of a new element
     */
    public int add() {
        // get the current mex
        int newValue = getMex();

        // add new element with mex value (apply xor before to correspond other values in trie)
        addNode(0, newValue ^ xorValue, BITS);

        return newValue;
    }

    /**
     * Adds new node to the trie.
     *
     * @param nodeIndex Index of the current node in the trie elements list.
     * @param num Leaf value to add.
     * @param bitNum Current bit number.
     */
    private void addNode(int nodeIndex, int num, int bitNum) {
        // if node doesn't exist create it
        if (trie.size() < nodeIndex + 1) {
            trie.add(nodeIndex, new Node());
        }

        // recursion base if we reached leaf
        if (bitNum < 0) {
            trie.get(nodeIndex).leavesNum = 1;
            return;
        }

        // extract %bitNum% bit (0 or 1) to go to the next node corresponding to this bit
        int nxt = (num >> bitNum) & 1;

        // if there's no child add it
        if (trie.get(nodeIndex).children[nxt] == -1) {
            trie.get(nodeIndex).children[nxt] = trieSize++;
        }

        // call the function recursively for the next, smaller bit
        addNode(trie.get(nodeIndex).children[nxt], num, bitNum - 1);
        trie.get(nodeIndex).leavesNum = 0;

        // after all subtrees were processed add the number of leaves in the left child
        if (trie.get(nodeIndex).children[0] != -1) {
            trie.get(nodeIndex).leavesNum += trie.get(trie.get(nodeIndex).children[0]).leavesNum;
        }

        // add the number of leaves in the right child
        if (trie.get(nodeIndex).children[1] != -1) {
            trie.get(nodeIndex).leavesNum += trie.get(trie.get(nodeIndex).children[1]).leavesNum;
        }
    }

    /**
     * Returns %bitNum% least significant bits of the minimum excluded number value for the elements.
     *
     * To get mex go down by the trie.
     *
     * @param nodeIndex Index of the current node in trie elements list.
     * @param bitNum Current bit number.
     * @param ans Current mex value for %bitNum% most significant bits.
     */
    private int getMexValueForBits(int nodeIndex, int bitNum, int ans) {
        // if the trie is empty return 0
        if (trie.size() == 0) {
            return 0;
        }

        // if reached leaf return current value
        if (nodeIndex == -1) {
            return ans;
        }

        // extract %bitNum% bit (0 or 1) of the current xor value
        int bit = (xorValue >> bitNum) & 1;
        int nxt = bit;

        // if the subtree of the child is a complete binary tree with depth equal to the number of the left bits,
        // then there is no numbers left in it and we choose another child
        if (trie.get(nodeIndex).children[nxt] != -1 && trie.get(trie.get(nodeIndex).children[nxt]).leavesNum == (1 << bitNum)) {
            nxt = 1 - nxt;
        }

        // set value of the %bitNum% bit in the current mex
        ans |= (nxt ^ bit) << bitNum;

        // call the function recursively for the smaller bit
        return getMexValueForBits(trie.get(nodeIndex).children[nxt], bitNum - 1, ans);
    }
}

/**
 * Trie node structure
 */
class Node {
    /**
     * Represents node's children index in the nodes list.
     * If child doesn't exist the value  is -1.
     */
    int[] children = new int[]{-1, -1};

    /**
     * The number of leaves in the subtree corresponding to this node.
     */
    int leavesNum = 0;
}
