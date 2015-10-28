package net.dv8tion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.BinaryDataExporter;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.importers.BinaryDataImporter;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;

//Tool Chain explanation came from the Betamaster blog.
//Future proof link: https://web.archive.org/web/20140519182903/http://www.betamaster.us/blog/?p=1442
public class ToolChain
{
    private static final String TOOL_DIR = "./RABCDAsm/";
    
    private static final String INNER_NAME = "inner";
    
    private static final String DECOMPRESS_CMD = TOOL_DIR + "swfdecompress.exe " + INNER_NAME + ".swf";
    private static final String EXPORT_ABC_CMD = TOOL_DIR +"abcexport.exe " + INNER_NAME + ".swf";
    private static final String ABC_TO_ASM_CMD = TOOL_DIR +"rabcdasm.exe " + INNER_NAME + "-0.abc";
    private static final String ASM_TO_ABC_CMD = TOOL_DIR + "rabcasm.exe " +  INNER_NAME + "-0/" + INNER_NAME + "-0.main.asasm";
    private static final String REPLACE_ABC_CMD = TOOL_DIR + "abcreplace.exe " + INNER_NAME + ".swf 0 " + INNER_NAME + "-0/" + INNER_NAME + "-0.main.abc";
    private static final String COMPRESS_CMD = TOOL_DIR + "swf7zcompress.exe " + INNER_NAME + ".swf";
    
    /**
     * Toolchain that takes the custom respack zip and injects it into the 0x40 Hues SWF.
     * First, we extract the inner SWF (the player) out of the main SWF (which contains the preloader) using FFDec.
     * Then use RABCDAsm to decompress the inner SWF, extract the AS3 code, and turn it into AS3 ASM or "p-code"
     * It then replaces certain AS3 ASM files with the modified versions that allow for the custom respack loading.
     * After replacing the files, the Tool chain reverses the order, turning AS3 ASM back into the compiled code
     *  and puts it back inside the inner SWF. 
     * Loads the inner SWF and replaces the BinaryData of the Finale_loop song with our custom zip.
     * Lastly, we put the inner SWF back into the main SWF and write it to file.
     * 
     * @param newSwfSavePath
     *             The path and file name used to save the new SWF file.
     * @throws IOException
     *             Can be caused by RABCDAsm being missing, hues.swf missing, and a few other situations
     *             TODO: Write a better catch system for handling these errors.
     * @throws InterruptedException
     *             Thrown if something is still using hues.swf or the RABCDAsm tool chain.
     */
    public static void injectCustomZip(String newSwfSavePath) throws IOException, InterruptedException
    {
        cleanup();    //Delete the temp files in-case we didn't delete them last time.
        
        FileInputStream swfFileStream = new FileInputStream(FileHandler.SWF_FILE);
        SWF swf = new SWF(swfFileStream, new Boolean(false));
        
        //Extracts the internal swf (0x40 Player) using FFDec
        DefineBinaryDataTag binaryDataTag = (DefineBinaryDataTag) swf.getCharacter(3); //3 is the BinaryData that contains the inner SWF. Determined through testing.
        BinaryDataExportSettings settings = new BinaryDataExportSettings(BinaryDataExportMode.RAW);//Extract as Binary Data.
        BinaryDataExporter export = new BinaryDataExporter();
        export.exportBinaryData(null, ".", Arrays.asList(binaryDataTag), settings, null);
        File inner = new File("3.bin");
        inner.renameTo(new File(INNER_NAME + ".swf"));//Renames from "3.bin" to "inner.swf" for easy toolchain use
        
        //Uses the RABCDAsm toolchain.  Currently only uses the .exe's, need to update to use the .d files on UNIX machines.
        //TODO: Provide support for using the .d files instead of only .exe for UNIX machines.
        Process p;
        p = Runtime.getRuntime().exec(DECOMPRESS_CMD);
        p.waitFor();
        p = Runtime.getRuntime().exec(EXPORT_ABC_CMD);
        p.waitFor();
        p = Runtime.getRuntime().exec(ABC_TO_ASM_CMD);
        p.waitFor();

        //Replaces the extracted files with the modified ones
        String builtRespackFile = "BuiltResourcePack.class.asasm";        
        delete(new File(INNER_NAME + "-0/" + builtRespackFile));
        Files.copy(Paths.get(builtRespackFile), Paths.get(INNER_NAME + "-0/" + builtRespackFile));
        
        String huesReloadedFile = "HuesReloadedRe.class.asasm";       
        delete(new File(INNER_NAME + "-0/" + huesReloadedFile));
        Files.copy(Paths.get(huesReloadedFile), Paths.get(INNER_NAME + "-0/" + huesReloadedFile));
        
        //Recompiles the files, including the modified files. Puts it all back into the inner.swf file.
        p = Runtime.getRuntime().exec(ASM_TO_ABC_CMD);
        p.waitFor();
        p = Runtime.getRuntime().exec(REPLACE_ABC_CMD);
        p.waitFor();
        p = Runtime.getRuntime().exec(COMPRESS_CMD);
        p.waitFor();
        
        //Gets all the bytes of the inner.swf file
        byte[] innerBytes = Files.readAllBytes(Paths.get(INNER_NAME + ".swf"));
        ByteArrayRange bytes = new ByteArrayRange(innerBytes);
        
        //Turns the bytes into a FFDec SWF instance
        InputStream is = new ByteArrayInputStream(bytes.getArray(), bytes.getPos(), bytes.getLength());
        SWF bswf = new SWF(is, null, "(SWF Data)", Configuration.parallelSpeedUp.get());
        
        //Finds the DefineBinaryData tag for the Finale loop.
        String finaleString = "DefineBinaryData (344: BuiltResourcePack_clsBytes_loop_Finale)";        
        DefineBinaryDataTag finaleTag = null;
        for (Tag t : bswf.tags)
        {
            if (t instanceof DefineBinaryDataTag && t.getName().equals(finaleString))
            {
                finaleTag = (DefineBinaryDataTag)t;
                break;
            }
        }
        
        //Replaces the Bytes representing the Finale song in the inner.swf with the bytes representing the CustomZip
        byte[] stuff = Files.readAllBytes(Paths.get("PackShit.zip"));//Temp, will change.
        BinaryDataImporter im = new BinaryDataImporter();
        im.importData(finaleTag, stuff);

        //Saves the modified SWF to a byte array.  Think of the "save as..." button, but instead of saving to file, we store in a byte array.
        ByteArrayOutputStream fosO = new ByteArrayOutputStream();
        bswf.saveTo(fosO);

        //Imports the modified inner.swf (Player) back into the main 0x40 Hues SWF.
        BinaryDataImporter importer = new BinaryDataImporter();
        importer.importData(binaryDataTag, fosO.toByteArray());
        
        //Saves the modified 0x40 Hues SWF to the path provided.
        FileOutputStream fos = new FileOutputStream(new File(newSwfSavePath));
        swf.saveTo(fos);
        
        //Cleanup temp files.
        cleanup();
        swfFileStream.close();
    }
    
    /**
     * Used for simple cleanup of temp files
     */
    private static void cleanup()
    {
        delete(new File("3.bin"));
        delete(new File(INNER_NAME + ".swf"));
        delete(new File(INNER_NAME + "-0.abc"));
        delete(new File(INNER_NAME + "-0"));
    }
    
    /**
     * Used for deleting files and directories.
     * Can recursively delete all files in a directory and then delete the directory.
     */
    private static void delete(File f)
    {
        if (f.isDirectory())
        {
            for (File c : f.listFiles())
            {
                delete(c);                
            }
        }
        f.delete();
    }
}
