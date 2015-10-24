# 0x40-Hues-Editor
A program to modify the 0x40 Hues SWF.  End goal is to be able to modify what pictures and song-loops are included in the file without having to re-download them and enable them after the file starts playing.  

# Todo List / Planned Features
This program is very much not complete. I would say that it is roughly halfway complete. Took quite a while to figure out how 0x40 had structured the internals of the SWF. The follow is a list of things that needs to be completed.
- Need to display a download progress bar while downloading the SWF and Respacks on first run. Currently shows nothing. The GUI doesn't appear until after all downloading and unzip is completely. This a problem. Not user friendly!
- Checkbox tree needs to save customization selections to file.  Needs to be able to load from file.
- Need to figure out how to store files into the SWF.  Needed so that the selected respacks can be inbeded into the SWF.
- Need to write Actionscript 3 that will be injected into the SWF to handle the new respacks.
- Look into rewriting parts of 0x40 Hues's Actionscript to handle possible duplicates between the newly imbeded images / songs and the ones that can be loaded later as respacks.
- Need to create a way to import an already customized SWF. This means being able to just import the customized respack list or importing the customized SWF and determining what the customized respack list was from the contents of the SWF.
- Support custom images and song loops being added, not just the official respacks. 
  - This can be individual images / songs or entire custom respacks
  - For individual images / songs, provide a gui to create the needed internal JSON in a user friendly manner.
- Look into adding other customizability such as:
  - Default startup song.
  - Default startup image.
  - Default startup skin (Retro, v4.20, Modern, Christmas, etc)
  - The ability to set the defaults for all the options on the Options screen.

# Libraries Used
- json.org's Java library
  - Used to parse and manipulate the internal JSON used by 0x40 Hues
  - Link: http://www.json.org/java/

- zip4j
  - Used to unzip the 0x40 Hues Respacks.
  - Seriously great library. Supports SO much stuff. Props to Srikanth Reddy Lingala for creating it.
  - Link: http://www.lingala.net/zip4j/

- swing checkbox tree
  - Used to show the 0x40 Hues Respacks in a easy to understand, graphical representation for ease of customization
  - If you check the code, mine is -slightly- different, I had to modify the internals so that I could check the values of the checkboxes through the code.
  - Link: https://github.com/scijava/swing-checkbox-tree

- JPEXS Free Flash Decompiler
  - Used to decompile the scripts and resources in the SWF.
  - This is the single most important library. Without this, this project would never have even been started. FFDec and its library are amazing resources.
  - Link: https://www.free-decompiler.com/flash/  (We are using the Java library, not the GUI program)
