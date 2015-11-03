package net.dv8tion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FileHandler
{
    public static final String RES_PACKS_LOCATION = "./Resources/packs/";
    public static final String RES_PACK_ZIP_LOCATION = "./Resources/packsZips/";
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
}
