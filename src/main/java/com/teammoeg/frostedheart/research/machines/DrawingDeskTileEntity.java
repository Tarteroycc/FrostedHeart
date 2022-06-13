package com.teammoeg.frostedheart.research.machines;

import java.util.Optional;
import java.util.Random;

import com.teammoeg.frostedheart.FHContent;
import com.teammoeg.frostedheart.content.recipes.PaperRecipe;
import com.teammoeg.frostedheart.research.ResearchListeners;
import com.teammoeg.frostedheart.research.gui.drawdesk.game.CardPos;
import com.teammoeg.frostedheart.research.gui.drawdesk.game.GenerateInfo;
import com.teammoeg.frostedheart.research.gui.drawdesk.game.ResearchGame;

import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class DrawingDeskTileEntity extends IEBaseTileEntity implements IInteractionObjectIE,IIEInventory{
	protected NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
	ResearchGame game=new ResearchGame();
	public static final int INK_SLOT=2;
	public static final int PAPER_SLOT=1;
	public static final int EXAMINE_SLOT=0;
    public DrawingDeskTileEntity() {
        super(FHContent.FHTileTypes.DRAWING_DESK.get());
    }
	@Override
	public boolean canUseGui(PlayerEntity arg0) {
		return true;
	}
	@Override
	public IInteractionObjectIE getGuiMaster() {
		return this;
	}
	@Override
	public void doGraphicalUpdates() {
	}
	@Override
	public NonNullList<ItemStack> getInventory() {
		return inventory;
	}
	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack item) {
		if(slot==EXAMINE_SLOT)
			return true;
		else if(slot==INK_SLOT)
			return item.getItem() instanceof IPen&&((IPen) item.getItem()).canUse(null,item,1);
		else if(slot==PAPER_SLOT)
			return PaperRecipe.recipes.stream().anyMatch(r->r.paper.test(item));
		else 
			return false;
	}
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) {
		if(nbt.contains("gamedata"))
			game.load(nbt.getCompound("gamedata"));
		if(!descPacket) {
			
			ItemStackHelper.loadAllItems(nbt, inventory);
		}
			
		
	}
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) {
		nbt.put("gamedata",game.serialize());
		if(!descPacket) {
			
			ItemStackHelper.saveAllItems(nbt, inventory);
		}
	}
	public ResearchGame getGame() {
		return game;
	}
	public void initGame(ServerPlayerEntity player) {
		if(inventory.get(PAPER_SLOT).isEmpty())return;
		int lvl=ResearchListeners.fetchGameLevel(player);
		if(lvl<0)return;
		Optional<PaperRecipe> pr=PaperRecipe.recipes.stream().filter(r->r.maxlevel>=lvl&&r.paper.test(inventory.get(PAPER_SLOT))).findAny();
		if(!pr.isPresent())return;
		if(!damageInk(player,5))return;
		inventory.get(PAPER_SLOT).shrink(1);
		game.init(GenerateInfo.all[lvl],new Random());
		game.setLvl(lvl);
	}
	private boolean damageInk(ServerPlayerEntity spe,int val) {
		ItemStack is=inventory.get(INK_SLOT);
		if(is.isEmpty()||!(is.getItem() instanceof IPen))return false;
		IPen pen=(IPen) is.getItem();
		return pen.damage(spe, is, val);
	}
	public boolean tryCombine(ServerPlayerEntity player,CardPos cp1,CardPos cp2) {
		if(!damageInk(player,1))return false;
		return game.tryCombine(cp1, cp2);
	}
	public void updateGame(ServerPlayerEntity player) {
		if(game.isFinished()) {
			
			ResearchListeners.commitGameLevel(player,game.getLvl());
			game.reset();
		}
	}
	public void submitItem(ServerPlayerEntity sender) {
		ResearchListeners.submitItem(sender,inventory.get(EXAMINE_SLOT));
	}

}
