package com.teammoeg.frostedheart.util.io.marshaller;

import net.minecraft.nbt.Tag;

public interface Marshaller {
	Tag toNBT(Object o);
	Object fromNBT(Tag nbt);
}
