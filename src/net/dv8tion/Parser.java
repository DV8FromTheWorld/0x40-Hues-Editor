package net.dv8tion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class Parser
{
    private static final String INFO_FILE_XML = "Info.xml";
    private static final String IMAGES_FILE_XML = "Images.xml";
    private static final String SONGS_FILE_XML = "Songs.xml";

    public static ResPack getLocalResPack(String folderPath) throws IOException
    {
        ResPack resPack;
        File folder = new File(folderPath);
        ArrayList<String> folderFiles = null;
        String infoFile = null;
        String imageFile = null;
        String songFile = null;

        while (infoFile == null)
        {
            folderFiles = new ArrayList<String>(Arrays.asList(folder.list()));
            if (folderFiles.contains(INFO_FILE_XML) || folderFiles.contains(INFO_FILE_XML.toLowerCase()))
            {
                if (folderFiles.contains(INFO_FILE_XML))
                {
                    infoFile = folder.getPath() + "/" + INFO_FILE_XML;
                }
                else
                {
                    infoFile = folder.getPath() + "/" + INFO_FILE_XML.toLowerCase();
                }
            }
            else
            {
                folder = folder.listFiles()[0];
            }
        }

        JSONObject info = XML.toJSONObject(FileHandler.loadFile(infoFile)).getJSONObject("info");
        resPack = new ResPack(info.getString("name"));
        resPack.path = folder.getPath();

        if (folderFiles.contains(IMAGES_FILE_XML) || folderFiles.contains(IMAGES_FILE_XML.toLowerCase()))
        {
            if (folderFiles.contains(IMAGES_FILE_XML))
            {
                imageFile = folder.getPath() + "/" + IMAGES_FILE_XML;
            }
            else
            {
                imageFile = folder.getPath() + "/" + IMAGES_FILE_XML.toLowerCase();
            }

            JSONObject imagesJson = XML.toJSONObject(FileHandler.loadFile(imageFile)).getJSONObject("images");
            if (imagesJson.optJSONArray("image") != null)
            {
                JSONArray images = imagesJson.getJSONArray("image");
                for (Object image : images)
                {
                    if (image instanceof JSONObject)
                    {
                        JSONObject imageJson = (JSONObject) image;
                        resPack.images.add(new Image(
                                imageJson.getString("name"),
                                imageJson.has("frameDuration")));
                    }
                }
            }
            else if (imagesJson.optJSONObject("image") != null)
            {
                JSONObject image = imagesJson.getJSONObject("image");
                resPack.images.add(new Image(
                        image.getString("name"),
                        image.has("frameDuration")));
            }
        }

        if (folderFiles.contains(SONGS_FILE_XML) || folderFiles.contains(SONGS_FILE_XML.toLowerCase()))
        {
            if (folderFiles.contains(SONGS_FILE_XML))
            {
                songFile = folder.getPath() + "/" + SONGS_FILE_XML;
            }
            else
            {
                songFile = folder.getPath() + "/" + SONGS_FILE_XML.toLowerCase();
            }

            JSONObject songsJson = XML.toJSONObject(FileHandler.loadFile(songFile)).getJSONObject("songs");
            if (songsJson.optJSONArray("song") != null)
            {
                JSONArray songs = songsJson.getJSONArray("song");
                for (Object song : songs)
                {
                    if (song instanceof JSONObject)
                    {
                        JSONObject name = (JSONObject) song;
                        resPack.songs.add(new Song(name.getString("title")));
                    }
                }
            }
            else if (songsJson.optJSONObject("song") != null)
            {
                JSONObject song = songsJson.getJSONObject("song");
                resPack.songs.add(new Song(song.getString("title")));
            }
        }

        return resPack;
    }

    public static ArrayList<ResPack> getRemoteResPacks(String resPacksWebpage) throws Exception
    {
        ArrayList<ResPack> packs = new ArrayList<ResPack>();
        JSONArray packsJson = new JSONArray(resPacksWebpage);
        for (Object pack : packsJson)
        {
            if (pack instanceof JSONObject)
            {
                ResPack resPack = getRemoteResPack((JSONObject) pack);
                if (resPack != null)
                    packs.add(resPack);
            }
        }
        return packs;
    }

    private static ResPack getRemoteResPack(JSONObject resPackInfo) throws Exception
    {
        String url = resPackInfo.getString("url");
        String folderName;

        Pattern p = Pattern.compile("(?<=respacks/).*(?=\\.zip)");
        Matcher m = p.matcher(url);
        find(m, "Cannot determine folder name of respack:\n" + url);
        folderName = m.group();

        if (!FileHandler.doesResPackExist(folderName))
        {
            File resPackFile = Downloader.resPack(url, folderName + ".zip");
            FileHandler.unzipResPack(resPackFile, folderName);
        }

        ResPack pack = getLocalResPack(FileHandler.RES_PACKS_LOCATION + folderName);
        if (pack != null)
            pack.url = url;
        return pack;
    }

    private static void find(Matcher matcher, String exceptionMessage) throws Exception
    {
        if (!matcher.find())
        {
            throw new Exception(exceptionMessage);
        }
    }
} 
