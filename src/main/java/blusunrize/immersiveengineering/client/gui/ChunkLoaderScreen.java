/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.gui.elements.GuiReactiveList;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.ChunkLoaderMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChunkLoaderScreen extends IEContainerScreen<ChunkLoaderMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("chunk_loader");

	public ChunkLoaderScreen(ChunkLoaderMenu container, Inventory inventoryPlayer, Component component)
	{
		super(container, inventoryPlayer, component, TEXTURE);
		this.imageHeight = 241;
	}

	@Override
	protected void init()
	{
		super.init();

		GuiReactiveList widget = new GuiReactiveList(
				leftPos+8, topPos+52, 96, 85,
				list -> {
				},
				() -> menu.blockEntityList
		).setPadding(0, 0, 4, 4).setTextStyling(0x918d85, 0x918d85, false);
		this.addRenderableWidget(widget);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new EnergyInfoArea(leftPos+157, topPos+96, menu.energy),
				new RefreshInfoArea(leftPos+141, topPos+94, menu.refreshTimer)
		);
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		Slot s = this.menu.slots.get(0);
		if(!s.hasItem()&&mouseX > leftPos+s.x&&mouseX < leftPos+s.x+16&&mouseY > topPos+s.y&&mouseY < topPos+s.y+16)
			addLine.accept(Items.PAPER.getDescription());
	}

	private static class RefreshInfoArea extends InfoArea
	{
		private final Supplier<Integer> refreshTimer;

		public RefreshInfoArea(int xMin, int yMin, Supplier<Integer> refreshTimer)
		{
			super(new Rect2i(xMin, yMin, 2, 16));
			this.refreshTimer = refreshTimer;
		}

		@Override
		protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
		{
			// Todo: show some text for hovering
		}

		@Override
		public void draw(GuiGraphics graphics)
		{
			final int height = area.getHeight();
			int stored = (int)(height*(refreshTimer.get()/(float)(60*20)));
			graphics.blit(TEXTURE,
					area.getX(), area.getY()+(height-stored),
					176, 75+(height-stored),
					area.getWidth(), stored
			);
		}
	}
}