package blusunrize.immersiveengineering.common.util;

public class IEMineral
		{
			public String name;
			public String genType;
			public int minCapacity;
			public int maxCapacity;
			public int mineralWeight;
			public float failChance;
			public String[] ores;
			public float[] chances;
			public int[] blacklistedDimensions;

			public IEMineral (String name, String genType, int minCapacity, int maxCapacity, int mineralWeight, float failChance, String[] ores, float[] chances, int[] blacklistedDimensions) 
			{
				this.genType = genType;
				this.minCapacity = minCapacity;
				this.maxCapacity = maxCapacity;
				this.name = name;
				this.mineralWeight = mineralWeight;
				this.failChance = failChance;
				this.ores = ores;
				this.chances = chances;
				this.blacklistedDimensions = blacklistedDimensions;
			}
		}