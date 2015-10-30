package net.dv8tion.gui;

import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.dv8tion.ResPackConfiguration;
import net.dv8tion.types.ResPack;

import org.scijava.swing.checkboxtree.CheckBoxNodeData;
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer;

@SuppressWarnings("serial")
public abstract class TreePane extends JScrollPane
{
    protected ArrayList<ResPack> resPacks;
    protected DefaultMutableTreeNode root;
    protected JTree tree;

    public TreePane(ResPackConfiguration config)
    {
        this.resPacks = config.getResPacks();
        root = new DefaultMutableTreeNode("Root");
    }

    public JTree getTree()
    {
        return tree;
    }

    protected CheckBoxNode add(
            DefaultMutableTreeNode parent, String text, boolean checked)
    {
        CheckBoxNodeData data = new CheckBoxNodeData(text, checked, true);
        CheckBoxNode node = new CheckBoxNode(data);
        parent.add(node);
        return node;
    }

    protected void setTree(DefaultMutableTreeNode rootNode)
    {
        JTree tree = new JTree(new DefaultTreeModel(root));
        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setCellEditor(new CheckBoxNodeEditor(tree));
        tree.setEditable(true);

        TreeListener listener = new TreeListener();
        tree.getModel().addTreeModelListener(listener);
        tree.addTreeExpansionListener(listener);

        this.tree = tree;
        this.setViewportView(tree);
    }
}
