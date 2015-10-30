package net.dv8tion.types;

import java.util.TreeSet;

import net.dv8tion.gui.CheckBoxNode;

public class ResPack implements Comparable<ResPack>
{
    public TreeSet<Image> images;
    public TreeSet<Song> songs;

    private CheckBoxNode respackNode;
    private CheckBoxNode imagesNode;
    private CheckBoxNode songsNode;

    public String name;
    public String url;

    public ResPack(String name)
    {
        images = new TreeSet<Image>();
        songs = new TreeSet<Song>();
        this.name = name;
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

    @Override
    public int compareTo(ResPack compare) {
        return compare.name.compareTo(name);
    }
}
