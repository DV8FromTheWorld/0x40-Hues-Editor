package net.dv8tion.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import net.dv8tion.Core;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar implements ActionListener
{
    private JMenuItem fileNew;
    private JMenuItem fileOpen;
    private JMenuItem fileSave;
    private JMenuItem fileSaveAs;

    private JMenuItem viewResPack;
    private JMenuItem viewImageSong;

    @Override
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        if (fileNew.equals(source))
            System.out.println("Create new file");
        else if (fileOpen.equals(source))
            System.out.println("Open new file");
        else if (fileSave.equals(source))
            Core.toJSON();
        else if (fileSaveAs.equals(source))
            System.out.println("Save file as..");
        else if (viewResPack.equals(source))
        {
            Core.setTreePane(new ResPackTreePane(Core.getResPacks()));
            viewResPack.setEnabled(false);
            viewImageSong.setEnabled(true);
        }
        else if (viewImageSong.equals(source))
        {
            Core.setTreePane(new ImageSongTreePane(Core.getResPacks()));
            viewResPack.setEnabled(true);
            viewImageSong.setEnabled(false);
        }
    }

    public MenuBar()
    {
        JMenu fileMenu = new JMenu("File");

        fileNew = new JMenuItem("New");
        fileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        fileNew.addActionListener(this);
        fileMenu.add(fileNew);

        fileOpen = new JMenuItem("Open");
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        fileOpen.addActionListener(this);
        fileMenu.add(fileOpen);

        fileSave = new JMenuItem("Save");
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        fileSave.addActionListener(this);
        fileMenu.add(fileSave);

        fileSaveAs = new JMenuItem("Save As...");
        fileSaveAs.addActionListener(this);
        fileMenu.add(fileSaveAs);

        JMenu viewMenu = new JMenu("View");

        viewResPack = new JMenuItem("ResPack Layout");
        viewResPack.addActionListener(this);
        viewResPack.setEnabled(false);
        viewMenu.add(viewResPack);

        viewImageSong = new JMenuItem("Image/Song Layout");
        viewImageSong.addActionListener(this);
        viewMenu.add(viewImageSong);

        this.add(fileMenu);
        this.add(viewMenu);
    }
}
