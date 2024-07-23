package com.teammoeg.frostedheart.content.robotics.logistics.tasks;

import com.teammoeg.frostedheart.content.robotics.logistics.LogisticNetwork;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LogisticRequestTask implements LogisticTask {
	ItemStack filter;
	int size;
	boolean fetchNBT;
	BlockEntity storage;

	public LogisticRequestTask(ItemStack filter, int size, boolean fetchNBT, BlockEntity storage) {
		super();
		this.filter = filter;
		this.size = size;
		this.fetchNBT = fetchNBT;
		this.storage = storage;
	}

	public ItemStack fetch(LogisticNetwork network,int msize) {
		ItemStack rets= network.fetchItem(filter, fetchNBT, Math.min(msize, size));
		size-=rets.getCount();
		return rets;
	}

	@Override
	public void work(LogisticNetwork network,int msize) {
		int rets= network.fetchItemInto(filter, network.getStorage(storage.getBlockPos()).getInventory(), fetchNBT, Math.min(msize, size));
		size-=rets;
	}

}
