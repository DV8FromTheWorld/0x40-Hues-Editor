package net.dv8tion.types;

import java.util.TreeSet;

public class ResPack implements Comparable<ResPack>
{
    public TreeSet<Image> images;
    public TreeSet<Song> songs;

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

    @Override
    public int compareTo(ResPack compare) {
        return compare.name.compareTo(name);
    }
}
