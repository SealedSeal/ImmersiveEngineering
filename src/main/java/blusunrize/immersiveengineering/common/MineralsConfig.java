package blusunrize.immersiveengineering.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.util.*;

import blusunrize.immersiveengineering.common.util.IEMineral;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;

public class MineralsConfig
{
    //Array that contains all loaded from config minerals
    public static List<IEMineral> MineralList = new ArrayList<IEMineral>();

    //
    //@Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        //Gets config directory
        //Config config = new Configuration(new File(event.getModConfigurationDirectory().getAbsolutePath() + "additional/path/to/your/config.cfg"));
        String path = event.getModConfigurationDirectory().getAbsolutePath();

        createFolder(path);
        scanFolder(path);

        /*
        Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();

        //Writes list to json file
        try
        {
            FileWriter writer = new FileWriter(path + "/IEMinerals/default_copy.json");
            gsonWriter.toJson(MineralList, writer);
            writer.flush(); //flush data to file
            writer.close(); //close write 
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    //Scans folder with json lists of minerals
    private static void scanFolder(String path)
    {
        //path +=  "/IEMinerals";
        //Gets all files inside mineral config folder
        File[] files = new File(path + "/IEMinerals").listFiles();
        //Loop that iterates through all jsons
        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                scanJSON(path + "/IEMinerals/" + file.getName());
            }
        }
    }

    //Reads json file with minerals and adds them to MineralList
    private static void scanJSON(String path)
    {
        Gson gson = new Gson();

        try
        {
            JsonReader reader = new JsonReader(new FileReader(path/* + "/IEMinerals/default.json"*/));
            MineralList.addAll(Arrays.asList(gson.fromJson(reader, IEMineral[].class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Creates folder with a default json list of minerals inside a config folder
    private static void createFolder(String path)
    {
        //Creates directory for jsons
        File dir = new File(path + "/IEMinerals");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        //Creates default json with minerals that are initially hardcoded inside IE
        File defaultJSON = new File(path + "/IEMinerals/default.json");
        if (defaultJSON.exists()) {
            return;
        }

        //Default list of minerals
        ArrayList<IEMineral> defaultMinerals = new ArrayList<IEMineral>();
        defaultMinerals.add(new IEMineral("Iron", "fixed", 0, 38400, 25, .1f, new String[]{"oreIron", "oreNickel", "oreTin", "denseoreIron"}, new float[]{.5f, .25f, .20f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Magnetite", "fixed", 0, 38400, 25, .1f, new String[]{"oreIron", "oreGold"}, new float[]{.85f, .15f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Pyrite", "fixed", 0, 38400, 20, .1f, new String[]{"oreIron", "oreSulfur"}, new float[]{.5f, .5f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Bauxite", "fixed", 0, 38400, 20, .2f, new String[]{"oreAluminum", "oreTitanium", "denseoreAluminum"}, new float[]{.90f, .05f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Copper", "fixed", 0, 38400, 30, .2f, new String[]{"oreCopper", "oreGold", "oreNickel", "denseoreCopper"}, new float[]{.65f, .25f, .05f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Cassiterite", "fixed", 0, 38400, 15, .2f, new String[]{"oreTin", "denseoreTin"}, new float[]{.95f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Gold", "fixed", 0, 38400, 20, .3f, new String[]{"oreGold", "oreCopper", "oreNickel", "denseoreGold"}, new float[]{.65f, .25f, .05f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Nickel", "fixed", 0, 38400, 20, .3f, new String[]{"oreNickel", "orePlatinum", "oreIron", "denseoreNickel"}, new float[]{.85f, .05f, .05f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Platinum", "fixed", 0, 38400, 5, .35f, new String[]{"orePlatinum", "oreNickel", "", "oreIridium", "denseorePlatinum"}, new float[]{.40f, .30f, .15f, .1f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Uranium", "fixed", 0, 38400, 10, .35f, new String[]{"oreUranium", "oreLead", "orePlutonium", "denseoreUranium"}, new float[]{.55f, .3f, .1f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Quartzite", "fixed", 0, 38400, 5, .3f, new String[]{"oreQuartz", "oreCertusQuartz"}, new float[]{.6f, .4f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Galena", "fixed", 0, 38400, 15, .2f, new String[]{"oreLead", "oreSilver", "oreSulfur", "denseoreLead", "denseoreSilver"}, new float[]{.40f, .40f, .1f, .05f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Lead", "fixed", 0, 38400, 10, .15f, new String[]{"oreLead", "oreSilver", "denseoreLead"}, new float[]{.55f, .4f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Silver", "fixed", 0, 38400, 10, .2f, new String[]{"oreSilver", "oreLead", "denseoreSilver"}, new float[]{.55f, .4f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Lapis", "fixed", 0, 38400, 10, .2f, new String[]{"oreLapis", "oreIron", "oreSulfur", "denseoreLapis"}, new float[]{.65f, .275f, .025f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Cinnabar", "fixed", 0, 38400, 15, .1f, new String[]{"oreRedstone", "denseoreRedstone", "oreRuby", "oreCinnabar", "oreSulfur"}, new float[]{.75f, .05f, .05f, .1f, .05f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Coal", "fixed", 0, 38400, 25, .1f, new String[]{"oreCoal", "denseoreCoal", "oreDiamond", "oreEmerald"}, new float[]{.92f, .1f, .015f, .015f}, new int[]{1}));
        defaultMinerals.add(new IEMineral("Silt", "fixed", 0, 38400, 25, .05f, new String[]{"blockClay", "sand", "gravel"}, new float[]{.5f, .3f, .2f}, new int[]{1}));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Writes list to json file
        try
        {
            FileWriter writer = new FileWriter(path + "/IEMinerals/default.json");
            gson.toJson(defaultMinerals, writer);
            writer.flush(); //flush data to file
            writer.close(); //close write 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}