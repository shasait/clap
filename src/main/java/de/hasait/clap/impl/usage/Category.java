package de.hasait.clap.impl.usage;

import java.util.ArrayDeque;
import java.util.Deque;

import de.hasait.clap.CLAP;
import de.hasait.clap.impl.AbstractCLAPRelated;

class Category extends AbstractCLAPRelated {

    private final String title;

    private int order;

    private final UsageNode rootNode;
    private final Deque<UsageNode> nodeStack = new ArrayDeque<>();

    public Category(CLAP clap, String title) {
        super(clap);

        this.title = title;

        this.rootNode = new UsageNode(clap, null, "", "", " ", false, null);
        nodeStack.addLast(rootNode);
    }

    public String getTitleForOutput(boolean prefix) {
        return (prefix ? "*" : "") + nls(title);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public boolean pushNode(String prefix, String suffix, String separator, boolean decision, String distinctionKey) {
        UsageNode parent = nodeStack.getLast();
        if (!parent.addDistinctionKey(distinctionKey)) {
            return false;
        }
        UsageNode node = new UsageNode(clap, parent, prefix, suffix, separator, decision, distinctionKey);
        nodeStack.addLast(node);
        return true;
    }

    public void popNode() {
        nodeStack.removeLast();
    }

    public void addEntry(String text) {
        UsageNode parent = nodeStack.getLast();
        UsageEntryLeaf entryLeaf = new UsageEntryLeaf(clap, parent, text);
    }

    public void addCategoryRef(Category category) {
        UsageNode parent = nodeStack.getLast();
        UsageForeignCategoryLeaf foreignCategoryLeaf = new UsageForeignCategoryLeaf(clap, parent, category);
    }

    public UsageNode getRootNode() {
        return rootNode;
    }

    public void beforePrint() {
        rootNode.beforePrint();
    }

}
