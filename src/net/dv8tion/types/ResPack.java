package net.dv8tion.types;

import java.util.TreeSet;

import net.dv8tion.gui.CheckBoxNode;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.scijava.swing.checkboxtree.CheckBoxNodeData;

public class ResPack implements Comparable<ResPack>
{
    public TreeSet<Image> images;
    public TreeSet<Song> songs;

    private CheckBoxNode respackNode;
    private CheckBoxNode imagesNode;
    private CheckBoxNode songsNode;

    public String name;
    public String url;
    public String path;

    public ResPack(String name)
    {
        images = new TreeSet<Image>();
        songs = new TreeSet<Song>();
        this.name = name;
        this.path = null;
    }

    public ResPack(JSONObject respackJson)
    {
        this(respackJson.getString("name"));
        this.path = String.valueOf(respackJson.get("path"));

        //The checkbox data for the Pack-level node
        CheckBoxNodeData respackData = new CheckBoxNodeData(
                name,
                respackJson.getBoolean("checked"),
                true);
        this.setRespackNode(new CheckBoxNode(respackData));

        //The checkbox data for the Images node
        CheckBoxNodeData imagesData = new CheckBoxNodeData(
                name,
                respackJson.getBoolean("imagesChecked"),
                respackJson.getBoolean("imagesEnabled"));
        this.setImagesNode(new CheckBoxNode(imagesData));

        //The checkbox data for the Songs node
        CheckBoxNodeData songsData = new CheckBoxNodeData(
                name,
                respackJson.getBoolean("songsChecked"),
                respackJson.getBoolean("songsEnabled"));
        this.setSongsNode(new CheckBoxNode(songsData));

        //Load all the images and songs from the provided JSON.
        for (Object imageObj : respackJson.getJSONArray("images"))
        {
            JSONObject imageJson = (JSONObject) imageObj;
            this.images.add(new Image(imageJson));
        }
        for (Object songObj : respackJson.getJSONArray("songs"))
        {
            JSONObject songJson = (JSONObject) songObj;
            this.songs.add(new Song(songJson));
        }
    }

    public int imageCount()
    {
        return images.size();
    }

    public int songCount()
    {
        return songs.size();
    }

    public boolean isRespackChecked()
    {
        return respackNode != null ? respackNode.isChecked() : true;
    }

    public boolean isImagesChecked()
    {
        return imagesNode != null ? imagesNode.isChecked() : true;
    }

    public boolean isImagesEnabled()
    {
        return imagesNode != null ? imagesNode.isEnabled() : true;
    }

    public boolean isSongsChecked()
    {
        return songsNode != null ? songsNode.isChecked() : true;
    }

    public boolean isSongsEnabled()
    {
        return songsNode != null ? songsNode.isEnabled() : true;
    }

    public void setRespackNode(CheckBoxNode node)
    {
        respackNode = node;
    }

    public void setImagesNode(CheckBoxNode node)
    {
        imagesNode = node;
    }

    public void setSongsNode(CheckBoxNode node)
    {
        songsNode = node;
    }

    public void setRespackChecked(boolean checked)
    {
        respackNode.setChecked(checked);
    }

    public void setImagesChecked(boolean checked)
    {
        imagesNode.setChecked(checked);
    }

    public void setImagesEnabled(boolean enabled)
    {
        imagesNode.setEnabled(enabled);
    }

    public void setSongsChecked(boolean checked)
    {
        songsNode.setChecked(checked);
    }

    public void setSongsEnabled(boolean enabled)
    {
        songsNode.setEnabled(enabled);
    }

    public void writeToJson(JSONWriter writer)
    {
        //Write the ResPack's Information to the JSON stream.
        writer.object()
            .key("name").value(this.name)
            .key("path").value(this.path)
            .key("checked").value(this.isRespackChecked())
            .key("imagesChecked").value(this.isImagesChecked())
            .key("imagesEnabled").value(this.isImagesEnabled())
            .key("songsChecked").value(this.isSongsChecked())
            .key("songsEnabled").value(this.isSongsEnabled());

        //Loops through and writes each image's information.
        writer.key("images").array();
        for (Image image : this.images)
        {
            image.writeToJson(writer);
        }
        writer.endArray();

        //Loops through and writes each song's information.
        writer.key("songs").array();
        for (Song song : this.songs)
        {
            song.writeToJson(writer);
        }
        writer.endArray();

        writer.endObject();
    }
    @Override
    public int compareTo(ResPack compare) {
        return compare.name.compareTo(name);
    }
}
