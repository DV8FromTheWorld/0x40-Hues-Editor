package net.dv8tion.types;

import net.dv8tion.gui.CheckBoxNode;

public class Image implements Comparable<Image>
{
    private String name;
    private String bitmapName;

    private CheckBoxNode node;

    public Image(String name)
    {
        this(name, null);
    }

    public Image(String name, String bitmapName)
    {
        this.name = name;
        this.bitmapName = bitmapName;
        this.node = null;
    }

    public boolean isChecked()
    {
        return node != null ? node.isChecked() : true;
    }

    public boolean isEnabled()
    {
        return node != null ? node.isEnabled() : true;
    }

    public String getName()
    {
        return name;
    }

    public String getBitmapName()
    {
        return bitmapName;
    }

    public CheckBoxNode getCheckboxNode()
    {
        return node;
    }

    public void setCheckboxNode(CheckBoxNode node)
    {
        this.node = node;
    }

    @Override
    public int compareTo(Image compare)
    {
        return name.toLowerCase().compareTo(compare.name.toLowerCase());
    }
}
