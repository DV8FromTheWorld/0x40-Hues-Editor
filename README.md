# 0x40-Hues-Editor
A program to modify the 0x40 Hues SWF.  End goal is to be able to modify what pictures and song-loops are included in the file without having to re-download them and enable them after the file starts playing.  

# Todo List / Planned Features
This program is very much not complete. I would say that it is roughly halfway complete. Took quite a while to figure out how 0x40 had structured the internals of the SWF. The follow is a list of things that needs to be completed.
- Need to display a download progress bar while downloading the SWF and Respacks on first run. Currently shows nothing. The GUI doesn't appear until after all downloading and unzip is completely. This a problem. Not user friendly!
- Checkbox tree needs to save customization selections to file.  Needs to be able to load from file.
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
  - Link: https://www.free-decompiler.com/flash/  (We are using the Java library, not the GUI program)

- RABCDAsm
  - Also used to decompile the SWF and extract the scripts. Incredibly powerful.
  - Currently we are using the Windows Binaries, but it is written in D so hopefully we can support linux too.
  - Link https://github.com/CyberShadow/RABCDAsm
    - This needs 7zip to be installed on your machine.
    - Windows: You will need to properly set your PATH variable in Environment Variables
    - Link: http://www.7-zip.org/download.html
