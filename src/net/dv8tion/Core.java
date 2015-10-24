package net.dv8tion;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.dv8tion.gui.MenuBar;
import net.dv8tion.gui.ResPackTreePane;
import net.dv8tion.gui.TreePane;
import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

import org.json.JSONWriter;

public class Core
{	
	public static final String BUILD_RESOURCE_PACK = "BuiltResourcePack.as";
	public static final String EXTRACTED_SCRIPTS_LOCATION = "./scripts/";
	public static final String BUILT_RESOURCE_PACK_PATH = EXTRACTED_SCRIPTS_LOCATION + BUILD_RESOURCE_PACK;
	public static final String REMOTE_RES_PACKS_URL = "http://cdn.0x40hu.es/getRespacks.php";
	
	private static ArrayList<ResPack> packs;
	private static JFrame frame;
	
	/**
	 * Main entry point of the program. Starts pack loading procedures and creates GUI.
	 * 
	 * @param args
	 * 			Command Line Arguments - This program doesn't support their use.
	 * @throws Exception
	 * 			General use exceptions.  If an exception makes it this far, there is a programming error, not runtime error.
	 */
	public static void main(String[] args) throws Exception
	{
		//Open window - Preforming Setup
		FileHandler.extractRequiredFilesFromSWF();  //Downloading SWF
		loadResPacks();  //Downloading respack (n)  - Loading respack (n)
		
		frame = new JFrame();
		setTreePane(new ResPackTreePane(packs));	//Defaults to the ResPack Layout (As opposed to the ImageSong Layout)
		
		frame.setJMenuBar(new MenuBar());
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("0x40 Hues Editor");
		frame.setSize(500, 500);  //Setup complete  -- Close setup window
		frame.setVisible(true);  //Show normal window.
	}
	
	/**
	 * Sets the main content pane for the program.
	 * 
	 * @param pane
	 * 			The TreePane that contains all the ResPacks, properly formatted into a NodeTree.
	 */
	public static void setTreePane(TreePane pane)
	{
		for (Component c : frame.getContentPane().getComponents())
		{
			if (c instanceof TreePane)
			{
				frame.getContentPane().remove(c);
			}
		}
		frame.getContentPane().add(pane, BorderLayout.CENTER);
		frame.revalidate();	//Redraws the GUI, not only for looks, but also so that the user can interact. (Non-Responsive GUI without this)
	}
	
	/**
	 * Used to expand all paths of the tree provided.
	 * This is used by a TreeListener to re-expand the Paths that were expanded when a change was made.
	 * 		(They need to be re-expanded because a call to treeModel.reload() un-expands them)
	 * 
	 * @param paths
	 * 			The previously expanded TreePaths
	 */
	public static void expandTreePaths(ArrayList<TreePath> paths)
	{
		for (Component c : frame.getContentPane().getComponents())
		{
			if (c instanceof TreePane)
			{
				TreePane pane = (TreePane) c;
				JTree tree = pane.getTree();
				for (TreePath path : paths)
				{
					tree.expandPath(path);
				}
			}
		}
	}
	
	/**
	 * Gets the List of ResPacks.
	 * 
	 * @return
	 * 			ArrayList of ResPacks.
	 */
	public static ArrayList<ResPack> getResPacks()
	{
		return packs;
	}
	
	public static void toJSON()
	{
		StringWriter stringWriter = new StringWriter();
		JSONWriter writer = new JSONWriter(stringWriter);
		writer
			.object()
				.key("options")
					.object()
						.key("StoreInSWF").value(true)
					.endObject()
				.key("respacks").array();
		for (ResPack pack : packs)
		{
			writer
				.object()
					.key("name").value(pack.name)
					.key("enabled").value(true)
					.key("images").array();
			for (Image image : pack.images)
			{
				writer
					.object()
						.key("name").value(image.getName())
						.key("enabled").value(image.isChecked())
						.key("bitmap").value(image.getBitmapName())
					.endObject();
			}
			writer.endArray();
			writer.key("songs").array();
			for (Song song : pack.songs)
			{
				writer
					.object()
						.key("title").value(song.getTitle())
						.key("enabled").value(song.isChecked())
						.key("sound").value(song.getSoundName())
					.endObject();
			}
			writer.endArray();
			writer.endObject();
		}
		writer
				.endArray()
			.endObject();

		System.out.println(stringWriter.getBuffer().toString());
		
	}
	
	/**
	 * Loads all ResPacks, starting with the Built-in Default ResPack defined by the code inside of
	 * the BUILT_RESOURCE_PACK_PATH file.
	 * 
	 * It also loads all remote ResPacks as defined by the REMOTE_RES_PACKS_URL webpage.
	 * This includes downloading and extracting the Remote ResPacks to the Resources/packs/ folder.
	 */
	private static void loadResPacks() throws Exception
	{
		packs = new ArrayList<ResPack>();
		
		ResPack defaultResPack = new ResPack("Default Built-In ResPack");		
		String defaultResPackFile = FileHandler.loadFile(BUILT_RESOURCE_PACK_PATH);
		Parser.getDefaultImages(defaultResPackFile, defaultResPack);
		Parser.getDefaultSongs(defaultResPackFile, defaultResPack);
		packs.add(defaultResPack);
		
		String resPacksWebpage = Downloader.webpage(REMOTE_RES_PACKS_URL);
		if (resPacksWebpage == null)
		{
			JOptionPane.showMessageDialog(frame, 
					"Could not locate remote respacks. Are you connected to the internet?\nURL: "
							+ REMOTE_RES_PACKS_URL);
		}
		else
		{
			packs.addAll(Parser.getRemoteResPacks(resPacksWebpage));
		}
		
	}
}
