package net.dv8tion.gui;

import net.dv8tion.ResPackConfiguration;
import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

@SuppressWarnings("serial")
public class ResPackTreePane extends TreePane
{
    public ResPackTreePane(ResPackConfiguration config)
    {
        super(config);

        for (ResPack pack : resPacks)
        {
            if (pack.imageCount() == 0 && pack.songCount() == 0)
                continue;

            CheckBoxNode packNode = add(root, pack.name, pack.isRespackChecked());
            pack.setRespackNode(packNode);

            if (pack.imageCount() != 0)
            {
                CheckBoxNode imageNode = add(packNode, "Images", pack.isImagesChecked());
                imageNode.setEnabled(pack.isRespackChecked());
                pack.setImagesNode(imageNode);

                boolean enableImages = imageNode.isChecked() && imageNode.isEnabled();
                for (Image image : pack.images)
                {
                    CheckBoxNode node = add(imageNode, image.getName(), image.isChecked());
                    node.setEnabled(enableImages);
                    image.setCheckboxNode(node);
                }
            }

            if (pack.songCount() != 0)
            {
                CheckBoxNode songNode = add(packNode, "Songs", pack.isSongsChecked());
                songNode.setEnabled(pack.isRespackChecked());
                pack.setSongsNode(songNode);

                boolean enableSongs = songNode.isChecked() && songNode.isEnabled();
                for (Song song : pack.songs)
                {
                    CheckBoxNode node = add(songNode, song.getTitle(), song.isChecked());
                    node.setEnabled(enableSongs);
                    song.setCheckboxNode(node);
                }
            }
            root.add(packNode);
        }
        setTree(root);
    }
}
