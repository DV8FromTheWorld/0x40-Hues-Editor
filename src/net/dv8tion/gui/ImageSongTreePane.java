package net.dv8tion.gui;

import java.util.ArrayList;

import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

@SuppressWarnings("serial")
public class ImageSongTreePane extends TreePane
{

	public ImageSongTreePane(ArrayList<ResPack> resPacks)
	{
		super(resPacks);
		
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
				image.setCheckboxNode(add(packNode, image.getName(), true));
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
				song.setCheckboxNode(add(packNode, song.getTitle(), true));
			}
		}
		
		setTree(root);
	}

}
