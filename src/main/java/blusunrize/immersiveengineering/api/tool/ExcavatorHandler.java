/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

/**
 * @author BluSunrize - 03.06.2015
 * <p>
 * The Handler for the Excavator. Chunk->Ore calculation is done here, as is registration
 */
public class ExcavatorHandler
{
	/**
	 * A HashMap of MineralMixes and their rarity (Integer out of 100)
	 */
	public static LinkedHashMap<MineralMix, Integer> mineralList = new LinkedHashMap<MineralMix, Integer>();
	public static HashMap<DimensionChunkCoords, MineralWorldInfo> mineralCache = new HashMap<DimensionChunkCoords, MineralWorldInfo>();
	private static HashMap<Integer, Set<MineralMix>> dimensionPermittedMinerals = new HashMap<Integer, Set<MineralMix>>();
	public static double mineralChance = 0;
	public static boolean allowPackets = false;

	public static MineralMix addMineral(String name, String genType, int minCapacity, int maxCapacity, int mineralWeight, float failChance, String[] ores, float[] chances, boolean blacklist, int[] dimensions)
	{
		assert ores.length==chances.length;
		MineralMix mix = new MineralMix(name, genType, minCapacity, maxCapacity, failChance, ores, chances, blacklist, dimensions);
		mineralList.put(mix, mineralWeight);
		return mix;
	}

	public static void recalculateChances(boolean mutePackets)
	{
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
			e.getKey().recalculateChances();
		dimensionPermittedMinerals.clear();
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER&&allowPackets&&!mutePackets)
		{
			HashMap<MineralMix, Integer> packetMap = new HashMap<MineralMix, Integer>();
			for(Map.Entry<MineralMix, Integer> e : ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null&&e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			ImmersiveEngineering.packetHandler.sendToAll(new MessageMineralListSync(packetMap));
		}
	}

	public static MineralMix getRandomMineral(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo info = getMineralWorldInfo(world, chunkX, chunkZ);
		if(info==null||(info.mineral==null&&info.mineralOverride==null))
			return null;

		if(info.veinCapacity >= 0&&info.depletion > info.veinCapacity)
			return null;

		return info.mineralOverride!=null?info.mineralOverride: info.mineral;
	}

	public static MineralWorldInfo getMineralWorldInfo(World world, int chunkX, int chunkZ)
	{
		return getMineralWorldInfo(world, new DimensionChunkCoords(world.provider.getDimension(), chunkX, chunkZ), false);
	}

	public static MineralWorldInfo getMineralWorldInfo(World world, DimensionChunkCoords chunkCoords, boolean guaranteed)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo worldInfo = mineralCache.get(chunkCoords);
		if(worldInfo==null)
		{
			MineralMix mix = null;
			Random r = world.getChunk(chunkCoords.x, chunkCoords.z).getRandomWithSeed(940610);
			double dd = r.nextDouble();

			boolean empty = !guaranteed&&dd > mineralChance;
			if(!empty)
			{
				MineralSelection selection = new MineralSelection(world, chunkCoords, 2);
				if(selection.getTotalWeight() > 0)
				{
					int weight = selection.getRandomWeight(r);
					for(Map.Entry<MineralMix, Integer> e : selection.getMinerals())
					{
						weight -= e.getValue();
						if(weight < 0)
						{
							mix = e.getKey();
							break;
						}
					}
				}
			}
			
			//Stores mineral data into chunk here
			worldInfo = new MineralWorldInfo();
			worldInfo.mineral = mix;
			
			//Sets vein capacity based on mineral genType
			if (mix != null) {
				if (worldInfo.mineral.genType.toLowerCase().equals("infinite")) {
					worldInfo.veinCapacity = -1;
				} else if (worldInfo.mineral.genType.toLowerCase().equals("fixed")) {
					worldInfo.veinCapacity = worldInfo.mineral.maxCapacity;
				} else if (worldInfo.mineral.genType.toLowerCase().equals("range")) {
					worldInfo.veinCapacity = worldInfo.mineral.minCapacity + (int)((worldInfo.mineral.maxCapacity - worldInfo.mineral.minCapacity) * dd);
				}
			}
			
			mineralCache.put(chunkCoords, worldInfo);
		}
		return worldInfo;
	}

	public static void depleteMinerals(World world, int chunkX, int chunkZ)
	{
		MineralWorldInfo info = getMineralWorldInfo(world, chunkX, chunkZ);
		info.depletion++;
		IESaveData.setDirty(world.provider.getDimension());
	}

	public static class MineralMix
	{
		public String name;
		public String genType;
		public int minCapacity;
		public int maxCapacity;
		public float failChance;
		public String[] ores;
		public float[] chances;
		public NonNullList<ItemStack> oreOutput;
		public float[] recalculatedChances;
		boolean isValid = false;
		/**
		 * Should an ore given to this mix not be present in the dictionary, it will attempt to draw a replacement from this list
		 */
		public HashMap<String, String> replacementOres;
		public int[] dimensionWhitelist = new int[0];
		public int[] dimensionBlacklist = new int[0];

		public MineralMix(String name, String genType, int minCapacity, int maxCapacity, float failChance, String[] ores, float[] chances, boolean blacklist, int[] dimensions)
		{
			this.name = name;
			this.genType = genType;
			this.minCapacity = minCapacity;
			this.maxCapacity = maxCapacity;
			this.failChance = failChance;
			this.ores = ores;
			this.chances = chances;

			//Assigns list of dimensions
			if (blacklist) {
				this.dimensionBlacklist = dimensions.clone();
			} else if (!blacklist) {
				this.dimensionWhitelist = dimensions.clone();
			}
		}

		public MineralMix addReplacement(String original, String replacement)
		{
			if(replacementOres==null)
				replacementOres = new HashMap<>();
			replacementOres.put(original, replacement);
			return this;
		}

		//4CD_TODO
		//Ore block selection is done here
		public void recalculateChances()
		{
			double chanceSum = 0;
			NonNullList<ItemStack> existing = NonNullList.create();
			ArrayList<Double> reChances = new ArrayList<>();
			for(int i = 0; i < ores.length; i++)
			{
				String ore = ores[i];
				if(replacementOres!=null&&/*!ApiUtils.isExistingOreName(ore)&&*/replacementOres.containsKey(ore))
					ore = replacementOres.get(ore);
				if(ore!=null&&!ore.isEmpty()/*&&ApiUtils.isExistingOreName(ore)*/)
				{
					ItemStack preferredOre = IEApi.getPreferredOreStack(ore);
					if(!preferredOre.isEmpty())
					{
						existing.add(preferredOre);
						reChances.add((double)chances[i]);
						chanceSum += chances[i];
					}
				}
			}
			isValid = existing.size() > 0;
			oreOutput = existing;
			recalculatedChances = new float[reChances.size()];
			for(int i = 0; i < reChances.size(); i++)
				recalculatedChances[i] = (float)(reChances.get(i)/chanceSum);
		}

		public ItemStack getRandomOre(Random rand)
		{
			float r = rand.nextFloat();
			for(int i = 0; i < recalculatedChances.length; i++)
			{
				r -= recalculatedChances[i];
				if(r < 0)
					return this.oreOutput.get(i);
			}
			return ItemStack.EMPTY;
		}

		public boolean isValid()
		{
			return isValid;
		}

		public boolean validDimension(int dim)
		{
			if(dimensionWhitelist!=null&&dimensionWhitelist.length > 0)
			{
				for(int white : dimensionWhitelist)
					if(dim==white)
						return true;
				return false;
			}
			else if(dimensionBlacklist!=null&&dimensionBlacklist.length > 0)
			{
				for(int black : dimensionBlacklist)
					if(dim==black)
						return false;
				return true;
			}
			return true;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("name", this.name);
			tag.setString("genType", this.genType);
			tag.setInteger("minCapacity", this.minCapacity);
			tag.setInteger("maxCapacity", this.maxCapacity);
			tag.setFloat("failChance", this.failChance);
			NBTTagList tagList = new NBTTagList();
			for(String ore : this.ores)
				tagList.appendTag(new NBTTagString(ore));
			tag.setTag("ores", tagList);

			tagList = new NBTTagList();
			for(float chance : this.chances)
				tagList.appendTag(new NBTTagFloat(chance));
			tag.setTag("chances", tagList);

			tagList = new NBTTagList();
			if(this.oreOutput!=null)
				for(ItemStack output : this.oreOutput)
					tagList.appendTag(output.writeToNBT(new NBTTagCompound()));
			tag.setTag("oreOutput", tagList);

			tagList = new NBTTagList();
			for(float chance : this.recalculatedChances)
				tagList.appendTag(new NBTTagFloat(chance));
			tag.setTag("recalculatedChances", tagList);
			tag.setBoolean("isValid", isValid);
			tag.setIntArray("dimensionWhitelist", dimensionWhitelist);
			tag.setIntArray("dimensionBlacklist", dimensionBlacklist);
			return tag;
		}

		public static MineralMix readFromNBT(NBTTagCompound tag)
		{
			String name = tag.getString("name");
			String genType = tag.getString("genType");
			int minCapacity = tag.getInteger("minCapacity");
			int maxCapacity = tag.getInteger("maxCapacity");
			float failChance = tag.getFloat("failChance");

			NBTTagList tagList = tag.getTagList("ores", 8);
			String[] ores = new String[tagList.tagCount()];
			for(int i = 0; i < ores.length; i++)
				ores[i] = tagList.getStringTagAt(i);

			tagList = tag.getTagList("chances", 5);
			float[] chances = new float[tagList.tagCount()];
			for(int i = 0; i < chances.length; i++)
				chances[i] = tagList.getFloatAt(i);

			tagList = tag.getTagList("oreOutput", 10);
			NonNullList<ItemStack> oreOutput = NonNullList.withSize(tagList.tagCount(), ItemStack.EMPTY);
			for(int i = 0; i < oreOutput.size(); i++)
				oreOutput.set(i, new ItemStack(tagList.getCompoundTagAt(i)));

			tagList = tag.getTagList("recalculatedChances", 5);
			float[] recalculatedChances = new float[tagList.tagCount()];
			for(int i = 0; i < recalculatedChances.length; i++)
				recalculatedChances[i] = tagList.getFloatAt(i);

			boolean isValid = tag.getBoolean("isValid");
			MineralMix mix = new MineralMix(name, genType, minCapacity, maxCapacity, failChance, ores, chances, true, new int[0]);
			mix.oreOutput = oreOutput;
			mix.recalculatedChances = recalculatedChances;
			mix.isValid = isValid;
			mix.dimensionWhitelist = tag.getIntArray("dimensionWhitelist");
			mix.dimensionBlacklist = tag.getIntArray("dimensionBlacklist");
			return mix;
		}
	}

	public static class MineralWorldInfo
	{
		public MineralMix mineral;
		public MineralMix mineralOverride;
		public int depletion;
		public int veinCapacity;

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			if(mineral!=null)
				tag.setString("mineral", mineral.name);
			if(mineralOverride!=null)
				tag.setString("mineralOverride", mineralOverride.name);
			tag.setInteger("depletion", depletion);
			tag.setInteger("veinCapacity", veinCapacity);
			return tag;
		}

		public static MineralWorldInfo readFromNBT(NBTTagCompound tag)
		{
			MineralWorldInfo info = new MineralWorldInfo();
			if(tag.hasKey("mineral"))
			{
				String s = tag.getString("mineral");
				for(MineralMix mineral : mineralList.keySet())
					if(s.equalsIgnoreCase(mineral.name))
						info.mineral = mineral;
			}
			if(tag.hasKey("mineralOverride"))
			{
				String s = tag.getString("mineralOverride");
				for(MineralMix mineral : mineralList.keySet())
					if(s.equalsIgnoreCase(mineral.name))
						info.mineralOverride = mineral;
			}
			info.depletion = tag.getInteger("depletion");
			info.veinCapacity = tag.getInteger("veinCapacity");
			return info;
		}
	}

	public static class MineralSelection
	{
		private final int totalWeight;
		private final Set<Map.Entry<MineralMix, Integer>> validMinerals;

		public MineralSelection(World world, DimensionChunkCoords chunkCoords, int radius)
		{
			Set<MineralMix> surrounding = new HashSet<>();
			for(int xx = -radius; xx <= radius; xx++)
				for(int zz = -radius; zz <= radius; zz++)
					if(xx!=0||zz!=0)
					{
						DimensionChunkCoords offset = chunkCoords.withOffset(xx, zz);
						MineralWorldInfo worldInfo = mineralCache.get(offset);
						if(worldInfo!=null&&worldInfo.mineral!=null)
							surrounding.add(worldInfo.mineral);
					}

			int weight = 0;
			this.validMinerals = new HashSet<>();
			for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
				if(e.getKey().isValid()&&e.getKey().validDimension(chunkCoords.dimension)&&!surrounding.contains(e.getKey()))
				{
					validMinerals.add(e);
					weight += e.getValue();
				}
			this.totalWeight = weight;
		}

		public int getTotalWeight()
		{
			return this.totalWeight;
		}

		public int getRandomWeight(Random random)
		{
			return Math.abs(random.nextInt()%this.totalWeight);
		}

		public Set<Map.Entry<MineralMix, Integer>> getMinerals()
		{
			return this.validMinerals;
		}
	}

}