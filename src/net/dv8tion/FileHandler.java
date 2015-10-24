package net.dv8tion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;

public class FileHandler
{
	public static final String RES_PACKS_LOCATION = "./Resources/packs/";
	public static final String SWF_FILE = "./hues.swf";
			
	public static String loadFile(String filePath) throws IOException
	{
		return loadFileKeepLineBreaks(filePath).replaceAll("\r|\n", "");
	}
	
	public static String loadFileKeepLineBreaks(String filePath) throws IOException
	{
		return new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
	}
	
	public static boolean doesResPackExist(String folderName)
	{
		return fileOrFolderExists(RES_PACKS_LOCATION + folderName);
	}
	
	public static boolean fileOrFolderExists(String path)
	{
		File file = new File(path);
		return file.exists();
	}
	
	public static void unzipResPack(File resPackFile, String folderName) throws ZipException
	{
		ZipFile resZip = new ZipFile(resPackFile);
		resZip.extractAll(RES_PACKS_LOCATION + folderName);
	}
	
	public static void extractRequiredFilesFromSWF() throws IOException, InterruptedException
	{
		File swfFile = new File(SWF_FILE);
		if (!swfFile.exists())
		{
			Downloader.file("http://muhnig.ga/versions/0x40%20Hues%20v5.11.swf", SWF_FILE);
		}
		
		FileInputStream swfFileStream = new FileInputStream(swfFile);
        SWF swf = new SWF(swfFileStream, new Boolean(false));
        DefineBinaryDataTag binaryDataTag = (DefineBinaryDataTag) swf.getCharacter(3); //3 is the BinaryData that contains the important ActionScript classes
        if (binaryDataTag == null)
        {
            System.out.println("This is null yo!");
        }
        InputStream is = new ByteArrayInputStream(binaryDataTag.binaryData.getRangeData());
        SWF bswf = new SWF(is, false);
        for (ScriptPack pack : bswf.getAS3Packs())
        {
        	if (pack.getName().equals("BuiltResourcePack"))
        	{
        		ScriptExportSettings as = new  ScriptExportSettings(ScriptExportMode.AS, false);
                File file = pack.export(Core.EXTRACTED_SCRIPTS_LOCATION, as, true);
        	}
        }
        swfFileStream.close();
	}
}
