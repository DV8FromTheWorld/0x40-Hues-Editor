package net.dv8tion;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.dv8tion.gui.MenuBar;
import net.dv8tion.gui.ResPackTreePane;
import net.dv8tion.gui.TreePane;

public class Core
{

    private static ResPackConfiguration respackConfig;
    private static JFrame frame;

    /**
     * Main entry point of the program. Starts pack loading procedures and creates GUI.
     *
     * @param args
     *             Command Line Arguments - This program doesn't support their use.
     * @throws Exception
     *             General use exceptions.  If an exception makes it this far, there is a programming error, not runtime error.
     */
    public static void main(String[] args) throws Exception
    {
        respackConfig = new ResPackConfiguration();
        //Open window - Preforming Setup
        Downloader.checkForRespack();;  //Downloading SWF
        loadResPacks();  //Downloading respack (n)  - Loading respack (n)

        frame = new JFrame();
        setTreePane(new ResPackTreePane(respackConfig));    //Defaults to the ResPack Layout (As opposed to the ImageSong Layout)

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
     *             The TreePane that contains all the ResPacks, properly formatted into a NodeTree.
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
        frame.revalidate();    //Redraws the GUI, not only for looks, but also so that the user can interact. (Non-Responsive GUI without this)
    }

    /**
     * Used to expand all paths of the tree provided.
     * This is used by a TreeListener to re-expand the Paths that were expanded when a change was made.
     *         (They need to be re-expanded because a call to treeModel.reload() un-expands them)
     *
     * @param paths
     *             The previously expanded TreePaths
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

    public static void load()
    {
        String testJSON;
        try
        {
            testJSON = new String(Files.readAllBytes(Paths.get("Config.json")), "UTF-8");
            respackConfig = new ResPackConfiguration(testJSON);
            setTreePane(new ResPackTreePane(respackConfig));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current ResPackConfiguration that is being displayed by the GUI.
     *
     * @return
     *      The ResPackConfiguration being displayed by the GUI.
     */
    public static ResPackConfiguration getResPackConfiguration()
    {
        return respackConfig;
    }

    public static void toJSON()
    {
        respackConfig.toJSON();
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
        String resPacksWebpage = Downloader.webpage(Downloader.REMOTE_RES_PACKS_URL);
        if (resPacksWebpage != null)
        {
            respackConfig.getResPacks().addAll(Parser.getRemoteResPacks(resPacksWebpage));
        }
        else
        {
            JOptionPane.showMessageDialog(frame,
                    "Could not locate remote respacks. Are you connected to the internet?\nURL: "
                            + Downloader.REMOTE_RES_PACKS_URL);
        }
    }
}
