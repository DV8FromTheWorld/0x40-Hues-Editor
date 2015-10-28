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

    public static void getDefaultImages(String defaultResPackFile, ResPack defaultResPack) throws Exception
    {
        Pattern p = Pattern.compile("images = \\[\\{ .*?\\}\\];"); //Regex: images = [{.*}]   Selects all the images
        Matcher m = p.matcher(defaultResPackFile);

        find(m, "Could not find images in defaultResPack defined by: " + Core.BUILT_RESOURCE_PACK_PATH);

        String imagesText = m.group(0).replaceAll(" {2,}", " "); //Replaces all instances of 2 or more consecutive spaces with a single space

        p = Pattern.compile("\\{.*?\\}"); //Regex: {.*?}  Selects an individual image
        m = p.matcher(imagesText);

        Pattern namePattern = Pattern.compile("(?<=\"name\":\").*?(?=\")");  //Selects the name attribute of each image
        Pattern bitmapPattern = Pattern.compile("(?<=((\"bitmap\":)|(\"bitmaps\":\\[))this\\.).*?(?=,)");  //Selects the bitmap attribute of each image
        Matcher nameMatch;
        Matcher bitmapMatch;
        while (m.find())    //Loops through each individually selected image
        {
            nameMatch = namePattern.matcher(m.group());
            find(nameMatch, "Could not locate the name of the image for: " + m.group());

            bitmapMatch = bitmapPattern.matcher(m.group());
            find(bitmapMatch, "Could not located the bitmapName of the image for : " + m.group());

            defaultResPack.images.add(new Image(nameMatch.group(), bitmapMatch.group()));
        }
    }

    public static void getDefaultSongs(String defaultResPackFile, ResPack defaultResPack) throws Exception
    {
        Pattern p = Pattern.compile("songs = \\[\\{.*?\\}\\]"); //Regex: songs = [{.*}]   Selects all the songs
        Matcher m = p.matcher(defaultResPackFile);

        find(m, "Could not find songs in defaultResPack defined by: " + Core.BUILT_RESOURCE_PACK_PATH);

        String songsText = m.group(0).replaceAll(" {2,}" , " "); //Replaces all instances of 2 or more consecutive spaces with a single space

        p = Pattern.compile("\\{.*?\\}"); //Regex {.*?} Selects an individual song
        m = p.matcher(songsText);

        Pattern titlePattern = Pattern.compile("(?<=\"title\":\").*?(?=\")");  //Selects the title attribute of each song
        Pattern soundPattern = Pattern.compile("(?<=\"sound\":this\\.).*?(?=,)"); //Selects the sound attribute of each song
        Matcher titleMatch;
        Matcher soundMatch;
        while (m.find())        //Loops through each individually selected song
        {
            titleMatch = titlePattern.matcher(m.group());
            find(titleMatch, "Could not locate the name of the song for: " + m.group());

            soundMatch = soundPattern.matcher(m.group());
            find(soundMatch, "Could not locate the soundName of the song for: " + m.group());

            defaultResPack.songs.add(new Song(titleMatch.group(), soundMatch.group()));
        }
    }

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
                        JSONObject name = (JSONObject) image;
                        resPack.images.add(new Image(name.getString("name")));
                    }
                }
            }
            else if (imagesJson.optJSONObject("image") != null)
            {
                JSONObject image = imagesJson.getJSONObject("image");
                resPack.images.add(new Image(image.getString("name")));
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
