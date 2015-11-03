package net.dv8tion.types;

import net.dv8tion.gui.CheckBoxNode;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.scijava.swing.checkboxtree.CheckBoxNodeData;

public class Song implements Comparable<Song>
{
    private String title;

    private CheckBoxNode node;

    public Song(String title)
    {
        this.title = title;
        node = null;
    }

    public Song(JSONObject songJson)
    {
        this.title = songJson.getString("title");
        //Doesn't actually display this checkbox, it is used only to set the proper "enabled", "checked" values.
        CheckBoxNodeData data = new CheckBoxNodeData(
                title,
                songJson.getBoolean("checked"),
                songJson.getBoolean("enabled"));
        this.setCheckboxNode(new CheckBoxNode(data));
    }

    public String getTitle()
    {
        return title;
    }

    public CheckBoxNode getCheckboxNode()
    {
        return node;
    }

    public boolean isChecked()
    {
        return node != null ? node.isChecked() : true;
    }

    public boolean isEnabled()
    {
        return node != null ? node.isEnabled() : true;
    }

    public void setCheckboxNode(CheckBoxNode node)
    {
        this.node = node;
    }

    public void writeToJson(JSONWriter writer)
    {
        writer
            .object()
                .key("title").value(this.getTitle())
                .key("checked").value(this.isChecked())
                .key("enabled").value(this.isEnabled())
            .endObject();
    }
    @Override
    public int compareTo(Song compare)
    {
        return title.toLowerCase().compareTo(compare.title.toLowerCase());
    }
}
