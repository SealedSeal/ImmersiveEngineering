![Logo](https://raw.githubusercontent.com/BluSunrize/ImmersiveEngineering/master/src/main/resources/assets/immersiveengineering/logo.png)
==============

A retro-futuristic tech mod!
Wires, transformers, capacitors!
==============

4ClevDev edit of original mod with some changes, especially reworking excavators.
==============

Changes:

  *Excavator:<br/>
    *Mineral Vein:<br/>
      + Reworked code structure of minerals<br/>
      + Minerals are now not hardcoded and moved to json file inside confing folder IEMinerals<br/>
      + Added handler for json confings of minerals<br/>
      + Added support for non ore dictionary names (minecraft:iron_ore)<br/>
      + Added support for names with metadata (minecraft:stone/1)<br/>
      - Removed vein size from general config<br/>
      - Removed blacklisted dimensions from general config<br/>
    
  *Sample Drill:<br/>
    *Core Sample:<br/>
      + Reworked code that manages rendering of a core sample texture<br/>
        + When there is an item in a vein instead of a block, it will paint sample as a cobblestone instead of stone to distinguish empty sample from a sample that contains only items<br/>
        + Fixed issue with no texture when trying to render more than one item<br/>
    
  *Arc Furnace:<br/>
    + Added config parameter arcfurnace_electrodeAutoInserting that allows electrode input to arc furnace from a top central block<br/>
    + Added config parameter arcfurnace_legitSideInput that disables side input of ores to arc furnace, leaving only top side with a hole for input<br/>
    + Added config parameter arcfurnace_legitSideAdditive that disables side input of additives to arc furnace, leaving only top side with a hole for input<br/>
    + Added config parameter arcfurnace_legitSideElectrode that disables side input of electrodes to arc furnace, leaving only top side with holes for input<br/>
    + Added config parameter arcfurnace_legitSideOutput that disables bottom and top sides output of products from arc furnace, leaving only side with a hole for output<br/>
    + Added config parameter arcfurnace_legitSideSlag that disables bottom side output of slag from arc furnace, leaving only side with a hole for output<br/>
    
  *Crusher:<br/>
    + Added config parameter crusher_legitSideInput that disables side input to crusher, leaving only top side of central top blocks for input
==============

  Mineral Config Files
