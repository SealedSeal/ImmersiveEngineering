/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.Objects;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class RobotWolfItem extends IEBaseItem
{
	public static ResourceLocation REGISTRY_KEY = ieLoc("robot");

	public RobotWolfItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		if(!(level instanceof ServerLevel)||player==null)
			return InteractionResult.SUCCESS;
		else
		{
			ItemStack itemstack = ctx.getItemInHand();
			BlockPos targetPos = ctx.getClickedPos();
			Direction direction = ctx.getClickedFace();
			BlockState blockstate = level.getBlockState(targetPos);

			BlockPos spawnPos;
			if(blockstate.getCollisionShape(level, targetPos).isEmpty())
				spawnPos = targetPos;
			else
				spawnPos = targetPos.relative(direction);

			Wolf wolf = EntityType.WOLF.spawn((ServerLevel)level, itemstack, player, spawnPos, MobSpawnType.SPAWN_EGG, true, !Objects.equals(targetPos, spawnPos)&&direction==Direction.UP);
			if(wolf!=null)
			{
				// Set variant, tame and buff it
				level.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).getHolder(REGISTRY_KEY).ifPresent(wolf::setVariant);
				wolf.tame(player);
				wolf.setOrderedToSit(true);
				wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6);
				wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50);
				wolf.setHealth(50);
				// Consume item
				itemstack.shrink(1);
				level.gameEvent(player, GameEvent.ENTITY_PLACE, targetPos);
			}

			return InteractionResult.CONSUME;
		}
	}


	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{

	}
}
