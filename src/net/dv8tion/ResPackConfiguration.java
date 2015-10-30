package net.dv8tion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import net.dv8tion.types.Image;
import net.dv8tion.types.ResPack;
import net.dv8tion.types.Song;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

public class ResPackConfiguration
{
    private ArrayList<ResPack> packs;

    //Used for testing.
    public static void main(String[] args) throws IOException
    {
        String testJSON = new String(Files.readAllBytes(Paths.get("Config.xml")), "UTF-8");
        new ResPackConfiguration(testJSON);
    }

    public ResPackConfiguration()
    {
        packs = new ArrayList<ResPack>();
    }

    public ResPackConfiguration(String jsonFile)
    {
        this();
        loadFromJson(jsonFile);
    }

    public ArrayList<ResPack> getResPacks()
    {
        return packs;
    }

    public void loadFromJson(String jsonString)
    {
        JSONObject config = new JSONObject(jsonString);
        JSONObject option = config.getJSONObject("options");
        //TODO: Use options to actually do stuff.

        JSONArray respacks = config.getJSONArray("respacks");
        //Build Respacks.
    }

    public void toJSON()
    {
        JSONStringer writer = new JSONStringer();
        writer
            .object()
                .key("options")
                    .object()
                        .key("StoreInSWF").value(true)
                    .endObject()
                .key("respacks")
                .array();
        for (ResPack pack : packs)
        {
            writer
                .object()
                    .key("name").value(pack.name)
                    .key("checked").value(pack.isRespackChecked())
                    .key("imagesChecked").value(pack.isImagesChecked())
                    .key("imagesEnabled").value(pack.isImagesEnabled())
                    .key("songsChecked").value(pack.isSongsChecked())
                    .key("songsEnabled").value(pack.isSongsEnabled())
                    .key("images").array();
            for (Image image : pack.images)
            {
                image.writeToJson(writer);
            }
            writer.endArray();
            writer.key("songs").array();
            for (Song song : pack.songs)
            {
                song.writeToJson(writer);
            }
            writer.endArray();
            writer.endObject();
        }
        writer
                .endArray()
            .endObject();
        byte[] json = writer.toString().getBytes();
        try {
            Files.write(Paths.get("Config.xml"), json);
            System.out.println("saved!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
