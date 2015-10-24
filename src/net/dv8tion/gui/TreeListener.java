package net.dv8tion.gui;

import java.util.ArrayList;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.dv8tion.Core;

public class TreeListener implements TreeModelListener, TreeExpansionListener
{
	private ArrayList<TreePath> expandedPaths;
	private boolean listenForExpandOrColapse;
	
	public TreeListener()
	{
		expandedPaths = new ArrayList<TreePath>();
		listenForExpandOrColapse = true;
	}
	
	@Override
	public void treeNodesChanged(TreeModelEvent event)
	{
		if (!(event.getChildren()[0] instanceof CheckBoxNode) 
				|| !(event.getSource() instanceof DefaultTreeModel))
		{
			return;
		}
		
		DefaultTreeModel model = (DefaultTreeModel) event.getSource();
		CheckBoxNode causeNode = (CheckBoxNode) event.getChildren()[0];
		boolean enable = causeNode.isChecked() && causeNode.isEnabled();

		for (CheckBoxNode node : causeNode.getChildren())
		{
			node.setEnabled(enable);
			if (node.getChildCount() != 0)
				model.nodeChanged(node);
			
		}
		model.reload();
		listenForExpandOrColapse = false;
		Core.expandTreePaths(expandedPaths);
		listenForExpandOrColapse = true;
		
	}

	@Override
	public void treeNodesInserted(TreeModelEvent event)
	{
		
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent event)
	{
		
	}

	@Override
	public void treeStructureChanged(TreeModelEvent event)
	{
		
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event)
	{
		if (listenForExpandOrColapse)
			expandedPaths.remove(event.getPath());
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event)
	{
		if (listenForExpandOrColapse)
			expandedPaths.add(event.getPath());
	}

}
