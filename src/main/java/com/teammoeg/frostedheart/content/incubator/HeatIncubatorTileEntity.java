/*
 * Copyright (c) 2022-2024 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.teammoeg.frostedheart.content.incubator;

import javax.annotation.Nonnull;

import com.teammoeg.frostedheart.FHBlockEntityTypes;
import com.teammoeg.frostedheart.FHCapabilities;
import com.teammoeg.frostedheart.content.steamenergy.capabilities.HeatConsumerEndpoint;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class HeatIncubatorTileEntity extends IncubatorTileEntity{
    HeatConsumerEndpoint network = new HeatConsumerEndpoint(10, 80, 5);

    public HeatIncubatorTileEntity(BlockPos bp,BlockState bs) {
        super(FHBlockEntityTypes.INCUBATOR2.get(),bp,bs);
    }


    LazyOptional<HeatConsumerEndpoint> heatcap=LazyOptional.of(()->network);
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
		if(capability==FHCapabilities.HEAT_EP.capability()&&facing == this.getBlockState().getValue(IncubatorBlock.HORIZONTAL_FACING)) {
			return heatcap.cast();
		}
		return super.getCapability(capability, facing);
    }
    @Override
    protected boolean fetchFuel() {


        if (network.tryDrainHeat(5)) {
            fuel = fuelMax = 400;
            return true;
        }

        return false;
    }
    @Override
    protected float getMaxEfficiency() {
        return 2f;
    }

    @Override
    public boolean isStackValid(int i, ItemStack itemStack) {
        if (i == 0) return false;
        return super.isStackValid(i, itemStack);
    }


    @Override
    public void readCustomNBT(CompoundTag compound, boolean client) {
        super.readCustomNBT(compound, client);
        network.load(compound,client);
    }


    @Override
    public void tick() {
        super.tick();

    }


    @Override
    public void writeCustomNBT(CompoundTag compound, boolean client) {
        super.writeCustomNBT(compound, client);
        network.save(compound,client);
    }


}
