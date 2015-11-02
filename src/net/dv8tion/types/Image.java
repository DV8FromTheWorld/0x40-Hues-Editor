package net.dv8tion.types;

import net.dv8tion.gui.CheckBoxNode;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.scijava.swing.checkboxtree.CheckBoxNodeData;

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

    public Image(JSONObject imageJson)
    {
        this.name = imageJson.getString("name");
        this.bitmapName = String.valueOf(imageJson.get("bitmap"));//We use .get instead of getString because this could be null.

        //Doesn't actually display this checkbox, it is used only to set the proper "enabled", "checked" values.
        CheckBoxNodeData data = new CheckBoxNodeData(
                name,
                imageJson.getBoolean("checked"),
                imageJson.getBoolean("enabled"));
        this.setCheckboxNode(new CheckBoxNode(data));
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

    public void writeToJson(JSONWriter writer)
    {
        writer
            .object()
                .key("name").value(this.getName())
                .key("checked").value(this.isChecked())
                .key("enabled").value(this.isEnabled())
                .key("bitmap").value(this.getBitmapName())
            .endObject();
    }

    @Override
    public int compareTo(Image compare)
    {
        return name.toLowerCase().compareTo(compare.name.toLowerCase());
    }
}
