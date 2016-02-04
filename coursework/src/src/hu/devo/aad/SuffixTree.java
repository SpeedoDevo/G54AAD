package hu.devo.aad;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is where the magic happens.
 * The word flush is used instead of canonization, which is the standard terminology in literature
 * about suffix trees. This makes more sense for me since the last unique character of the
 * SuffixTree flushes out incomplete leaves. Also the word canonization is misused, it should be
 * canonicalization, but ain't nobody got time for typing either of these words out. <br/>
 * Implementation based on http://pastie.org/5925812 <br/>
 * Created by Barnabas on 26/12/2015.
 */
public class SuffixTree {
    static final int INF = Integer.MAX_VALUE;

    Node rootNode;
    //    String text = "";
    char[] text;

    /**
     * A list of possible unique flush (or canonization) chars.
     */
    static final char flushChars[] = {'$', '#', '%', '^', '`', '¬', '¦', '|', '@', '~', '='};
    char flushChar;
    boolean flushCharInText;

    int position = -1;
    Node needSuffixLink;
    int remainder;


    /**
     * indicates which node we are looking at
     */
    Node activeNode;
    /**
     * indicates how deep we have to look in nodes
     */
    int activeLength;
    /**
     * indicates which edge we are looking at
     */
    int activeEdge;

    BuildTimer build = new BuildTimer();
    SearchTimer search = new SearchTimer();

    /**
     * Prepares the SuffixTree for building.
     */
    SuffixTree() {
        nodeNumber = 0;
        rootNode = new Node(-1, -1);
        activeNode = rootNode;
    }

    /**
     * Builds the suffix tree using the text provided.
     *
     * @param text the text
     * @return the complete flushed (canonized) suffix tree
     * @throws IllegalArgumentException if the text couldn't be flushed
     */
    SuffixTree build(String text) throws IllegalArgumentException {
        this.text = new char[text.length() + 1];

        if (Settings.DO_BUILD_EXPERIMENT) {
            build.startTiming();
        }
        //check the text if the last char is unique
        text = maybeFlush(text);

        //build the tree by incrementally adding the chars
        for (int i = 0; i < text.length(); i++) {
            addChar(text.substring(i, i + 1).charAt(0));
            Util.logProgress(i, text.length());
            if (Settings.DO_BUILD_EXPERIMENT) {
                build.characterFinished(i);
            }
        }
        if (Settings.DO_BUILD_EXPERIMENT) {
            build.stopTiming();
        }

        //returning void is pointless
        return this;
    }

    /**
     * Adds a flushing char to the String if needed.
     *
     * @param text the text that might need flushing
     * @return the possibly flushed string
     * @throws IllegalArgumentException if the text needs flushing but it cannot be done
     */
    String maybeFlush(String text) throws IllegalArgumentException {
        //check if flushed already
        char lastChar = text.charAt(text.length() - 1);
        int equalsWordLengthIfUniq = text.indexOf(lastChar);
        boolean needsFlush = equalsWordLengthIfUniq < text.length() - 1;


        if (needsFlush) {
            //not yet flushed, add a uniq char to it
            return flush(text);
        } else {
            //already flushed leave the text alone
            flushChar = lastChar;
            //mark flushChar as a searchable char
            flushCharInText = true;
            if (Settings.LOG_SUFFIX_TREE_STEPS)
                Logger.println(text + " already flushed with '" + lastChar + "'\n");
            return text;
        }
    }

    /**
     * Flushes the provided text.
     *
     * @param text the text
     * @return the flushed string
     * @throws IllegalArgumentException if the text couldn't be flushed
     */
    String flush(String text) throws IllegalArgumentException {
        //check possible flushing chars
        for (char c : flushChars) {
            if (isUniq(c, text)) {
                //append the first uniq flusher
                if (Settings.LOG_SUFFIX_TREE_STEPS)
                    Logger.println("flushing " + text + " with '" + c + "'\n");
                flushChar = c;
                return text + c;
            }
        }
        throw new IllegalArgumentException("provided string contains all of these characters $#%^`¬¦|@~= ," +
                " remove at least one or make the last character unique");
    }

    /**
     * Checks if the char is contained in the text
     *
     * @param c    the char
     * @param text the text
     * @return true if it is uniq (not contained in text)
     */
    boolean isUniq(char c, String text) {
        int lessThanZeroIfUniq = text.indexOf(c);
        return lessThanZeroIfUniq < 0;
    }

    /**
     * Adds a suffix link to the node that last received one.
     *
     * @param to the node that the suffix link will point to
     */
    void addSuffixLink(Node to) {
        if (needSuffixLink != null) {
            if (Settings.LOG_SUFFIX_TREE_STEPS)
                Logger.printf("%s gets a suffixLink to %s\n", needSuffixLink, to);
            needSuffixLink.suffixLink = to;
        }
        needSuffixLink = to;
    }

    /**
     * Returns the character represented by activeEdge::int. Makes it easier to walk the tree.
     *
     * @return the character
     */
    char activeEdge() {
        return text[activeEdge];
    }

    /**
     * Sets the active... vars to look at the given node.
     *
     * @param node the node to process next
     */
    void walk(Node node) {
        activeEdge += node.length();
        activeLength -= node.length();
        activeNode = node;
    }

    /**
     * Adds a char to the tree.
     *
     * @param newChar the new char to add
     */
    void addChar(char newChar) {
        text[++position] = newChar;
        if (Settings.LOG_SUFFIX_TREE_STEPS)
            Logger.printf("process %c in %s\n", newChar, text);
        //        position++;
        needSuffixLink = null;
        remainder++;

        while (remainder > 0) {
            if (activeLength == 0) {
                activeEdge = position;
            }

            if (!activeNode.children.containsKey(activeEdge())) {
                //if the active node doesn't have an outgoing edge labeled with the current character then create one
                Node newNode = new Node(position, INF); //from the current pos until the end of the word
                activeNode.children.put(activeEdge(), newNode);
                addSuffixLink(activeNode);
                if (Settings.LOG_SUFFIX_TREE_STEPS)
                    Logger.println("new node '" + newNode + "'");
            } else {
                //get the edge labeled with the current character
                Node nextChild = activeNode.children.get(activeEdge());
                if (activeLength >= nextChild.length()) {
                    //we are not deep enough, go deeper
                    walk(nextChild);
                    if (Settings.LOG_SUFFIX_TREE_STEPS)
                        Logger.println("walk to '" + activeNode + "'");
                    continue;
                } else {

                    //we are looking at the right node, check if split is needed or if we can get away with implicit adding
                    if (text[nextChild.start + activeLength] == newChar) {
                        //same suffix found, implicitly add it
                        activeLength++;
                        addSuffixLink(activeNode);
                        if (Settings.LOG_SUFFIX_TREE_STEPS)
                            Logger.println("same suffix (" + newChar + ") found in '" + activeNode + "'");
                        break;
                    } else {
                        if (Settings.LOG_SUFFIX_TREE_STEPS)
                            Logger.print("split '" + nextChild + "' into ");
                        //different suffix found, have to split tree
                        Node split = new Node(nextChild.start, nextChild.start + activeLength);
                        activeNode.children.put(activeEdge(), split);

                        Node leaf = new Node(position, INF);

                        split.children.put(newChar, leaf);

                        nextChild.start += activeLength;
                        split.children.put(text[nextChild.start], nextChild);
                        if (Settings.LOG_SUFFIX_TREE_STEPS)
                            Logger.printf("'%s' -> ( '%s' , '%s' )\n", split, leaf, nextChild);
                        addSuffixLink(split);
                    }

                }
            }
            remainder--;

            if (activeNode == rootNode && activeLength > 0) {
                if (Settings.LOG_SUFFIX_TREE_STEPS)
                    Logger.println("1/" + activeLength + " implicit addition was processed");
                activeLength--;
                activeEdge = position - remainder + 1;
            } else {
                if (activeNode.suffixLink != null) {
                    activeNode = activeNode.suffixLink;
                    if (Settings.LOG_SUFFIX_TREE_STEPS)
                        Logger.println("suffix link (" + activeNode + ") needs processing");
                } else {
                    if (Settings.LOG_SUFFIX_TREE_STEPS)
                        Logger.println("no suffix link, character was fully processed\n");
                    activeNode = rootNode;
                }
            }
        }

    }

    /**
     * Searches for a search term in the tree.
     *
     * @param term the term
     * @return a set of int positions where the term starts in the tree's contained string, empty if it's not found
     * @throws IllegalArgumentException if the term contains the flushing char
     */
    TreeSet<Integer> search(String term) throws IllegalArgumentException {
        if (!flushCharInText && term.contains(flushChar + "")) {
            throw new IllegalArgumentException(
                    "Not allowed to search for the flushing character");
        }
        TreeSet<Integer> res = new TreeSet<>();
        search.startTiming();
        search(term, 0, rootNode, res);
        search.lastFound(res.size(), term.length());
        return res;
    }

    /**
     * The recursive part of search
     *
     * @param term what to search for
     * @param pos  current position in the term
     * @param node current node checked
     * @param res  results collected here
     */
    void search(String term, int pos, Node node, TreeSet<Integer> res) {
        //can we move from here
        if (node.children.containsKey(term.charAt(pos))) {
            //yes we can
            Node next = node.children.get(term.charAt(pos));
            String currentSuffix = next.label();
            if (pos + currentSuffix.length() < term.length()) {
                //if the term is longer
                if (term.substring(pos).startsWith(currentSuffix)) {
                    search(term, pos + next.length(), next, res);
                }
            } else {
                //the node's contained string is longer
                //we might have a match
                if (next.label().startsWith(term.substring(pos))) {
                    //we have a hit
                    searchLeaves(pos, next, res, term.length());
                }
            }

        } else {
            //the node doesn't have any more children
            //if its contained string matches, then we have a match
            if (node.label().startsWith(term.substring(pos))) {
                searchLeaves(pos, node, res, term.length());
            }
        }
    }

    void searchLeaves(int pos, Node node, TreeSet<Integer> res, int termLength) {
        if (node.children.isEmpty()) {
            res.add(node.start - pos);
            if (res.size() == 1) {
                search.firstFound(termLength);
            }
        } else {
            for (Node child : node.children.values()) {
                searchLeaves(node.length() + pos, child, res, termLength);
                if (res.size() == 1 && !Settings.DO_SEARCH_ALL) {
                    break;
                }
            }
        }
    }

    String textSubstring(int from, int to) {
        return new String(Arrays.copyOfRange(text, from, to));
    }


    public String toString() {
        return "ST[" + text.length + "](" + textSubstring(0, 30) +
                (text.length > 30 ? "..." : "") + ")";
    }

    /**
     * Builds a string that visualizes the structure of the tree
     *
     * @return the string
     */
    String visualize() {
        StringBuilder sb = new StringBuilder();
        int longestInt = String.valueOf(text.length).length();
        for (int i = 0; i < text.length; i++) {
            sb.append(String.format("%-" + (longestInt + 1) + "d", i));
        }
        sb.append('\n');
        String padding = new String(new char[longestInt]).replace("\0", " ");
        for (int i = 0; i < text.length; i++) {
            sb.append(text[i]).append(padding);
        }
        sb.append("\n\n");
        return rootNode.visualize(sb, "", true);
    }

    /**
     * Gets the node number when a new one is created.
     */
    static int nodeNumber;

    class Node {

        int id;
        int start;
        int end;
        Node suffixLink;

        TreeMap<Character, Node> children = new TreeMap<>();

        public Node(int start, int end) {
            this.start = start;
            this.end = end;
            id = nodeNumber++;
        }

        /**
         * @return the length of the contained string
         */
        int length() {
            return actualEnd() - start;
        }

        /**
         * Needed because infinity denotes until the string's end.
         *
         * @return where the contained string ends
         */
        int actualEnd() {
            return Math.min(end, position + 1);
        }

        /**
         * @return the string representation of the node
         */
        String label() {
            return start == -1 && end == -1 ? "" : textSubstring(start, actualEnd());
        }

        public String toString() {
            String label;
            if (start == -1 && end == -1) {
                label = "root";
            } else {
                int e = actualEnd();
                String maybeLink = suffixLink != null ? " {=> [" + suffixLink.id + "]}" : "";
                label = String.format("[%d] (%d,%d) %s%s",
                        id, start, e, textSubstring(start, e), maybeLink);
            }
            return label;
        }

        /**
         * Builds a visualization of the node and its children. Recursive method used by
         * {@link SuffixTree#visualize()}.
         *
         * @param sb     a StringBuilder to append the result to
         * @param pre    prefix to prepend each line with
         * @param isTail should be true if this is the last child of the parent node.
         * @return the finished string representation
         */
        String visualize(StringBuilder sb, String pre, boolean isTail) {
            sb.append(pre).append(isTail ? "└── " : "├── ").append(this).append("\n");
            int i = 0;
            int size = children.size();
            for (Iterator<Node> iterator = children.values().iterator(); iterator.hasNext(); i++) {
                Node n = iterator.next();
                n.visualize(sb, pre + (isTail ? "    " : "│   "), i == size - 1);
            }
            return sb.toString();
        }
    }

    /**
     * Used for logging what happens in the suffix tree, it should be turned off for large trees
     * with {@link Settings#LOG_SUFFIX_TREE_STEPS}.
     */
    public static class Logger {
        static PrintStream l = System.out;

        static void println(Object o) {
            l.println(o);
        }

        static void print(Object o) {
            l.print(o);
        }

        static void printf(String format, Object... args) {
            l.printf(format, args);
        }
    }
}
