package net.dv8tion.gui;

import java.util.ArrayList;

import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

@SuppressWarnings("serial")
public class ResPackTreePane extends TreePane
{
    public ResPackTreePane(ArrayList<ResPack> resPacks)
    {
        super(resPacks);

        for (ResPack pack : resPacks)
        {
            if (pack.imageCount() == 0 && pack.songCount() == 0)
                continue;

            CheckBoxNode packNode = add(root, pack.name, true);

            if (pack.imageCount() != 0)
            {
                CheckBoxNode imageNode = add(packNode, "Images", true);
                for (Image image : pack.images)
                {
                    image.setCheckboxNode(add(imageNode, image.getName(), true));
                }
            }

            if (pack.songCount() != 0)
            {
                CheckBoxNode songNode = add(packNode, "Songs", true);
                for (Song song : pack.songs)
                {
                    song.setCheckboxNode(add(songNode, song.getTitle(), true));
                }
            }
            root.add(packNode);
        }
        setTree(root);
    }
}
