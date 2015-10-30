package net.dv8tion.types;

import net.dv8tion.gui.CheckBoxNode;

public class Song implements Comparable<Song>
{
    private String title;
    private String soundName;

    private CheckBoxNode node;

    public Song(String title)
    {
        this(title, null);
    }

    public Song(String title, String soundName)
    {
        this.title = title;
        this.soundName = soundName;
        this.node = null;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSoundName()
    {
        return soundName;
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

    @Override
    public int compareTo(Song compare)
    {
        return title.toLowerCase().compareTo(compare.title.toLowerCase());
    }
}
