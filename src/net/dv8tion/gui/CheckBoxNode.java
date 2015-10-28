package net.dv8tion.gui;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.scijava.swing.checkboxtree.CheckBoxNodeData;

@SuppressWarnings("serial")
public class CheckBoxNode extends DefaultMutableTreeNode
{
    private CheckBoxNodeData data;

    public CheckBoxNode(CheckBoxNodeData data)
    {
        super(data);
        this.data = data;
    }

    public CheckBoxNodeData getData()
    {
        return data;
    }

    public void setChecked(final boolean checked)
    {
        data.setChecked(checked);
    }

    public boolean isChecked()
    {
        return data.isChecked();
    }

    public void setEnabled(final boolean enabled)
    {
        data.setEnabled(enabled);
    }

    public boolean isEnabled()
    {
        return data.isEnabled();
    }

    public void setText(final String text)
    {
        data.setText(text);
    }

    public String getText()
    {
        return data.getText();
    }

    public ArrayList<CheckBoxNode> getChildren()
    {
        @SuppressWarnings("rawtypes")
        Enumeration iterate = children();
        ArrayList<CheckBoxNode> children = new ArrayList<CheckBoxNode>();
        while(iterate.hasMoreElements())
        {
            children.add((CheckBoxNode) iterate.nextElement());
        }
        return children;
    }
}
