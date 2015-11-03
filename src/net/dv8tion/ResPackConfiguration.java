package net.dv8tion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import net.dv8tion.types.ResPack;

import org.json.JSONObject;
import org.json.JSONStringer;

public class ResPackConfiguration
{
    private ArrayList<ResPack> packs;

    public ResPackConfiguration()
    {
        packs = new ArrayList<ResPack>();
    }

    public ResPackConfiguration(String jsonString)
    {
        this();
        loadFromJson(jsonString);
    }

    public ArrayList<ResPack> getResPacks()
    {
        return packs;
    }

    private void loadFromJson(String jsonString)
    {
        JSONObject config = new JSONObject(jsonString);
        JSONObject option = config.getJSONObject("options");
        //TODO: Use options to actually do stuff.

        for (Object respackObj : config.getJSONArray("respacks"))
        {
            JSONObject respackJson = (JSONObject) respackObj;
            ResPack respack = new ResPack(respackJson);
            packs.add(respack);
        }
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
            pack.writeToJson(writer);
        }
        writer
                .endArray()
            .endObject();
        byte[] json = writer.toString().getBytes();
        try {
            Files.write(Paths.get("Config.json"), json);
            System.out.println("saved!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
