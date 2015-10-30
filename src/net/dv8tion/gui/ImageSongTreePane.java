package net.dv8tion.gui;

import net.dv8tion.ResPackConfiguration;
import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

@SuppressWarnings("serial")
public class ImageSongTreePane extends TreePane
{
    public ImageSongTreePane(ResPackConfiguration config)
    {
        super(config);

        CheckBoxNode imageNode = add(root, "Images", true);
        for (ResPack pack : resPacks)
        {
            if (pack.imageCount() == 0)
            {
                continue;
            }

            CheckBoxNode packNode = add(imageNode, pack.name, true);
            for (Image image : pack.images)
            {
                image.setCheckboxNode(add(packNode, image.getName(), image.isChecked()));
            }
        }

        CheckBoxNode songNode = add(root, "Songs", true);
        for (ResPack pack : resPacks)
        {
            if (pack.songCount() == 0)
            {
                continue;
            }

            CheckBoxNode packNode = add(songNode, pack.name, true);
            for (Song song : pack.songs)
            {
                song.setCheckboxNode(add(packNode, song.getTitle(), song.isChecked()));
            }
        }
        setTree(root);
    }
}
