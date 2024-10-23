/*
 * Copyright (c) 2021-2024 TeamMoeg
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

package com.teammoeg.frostedheart.content.climate.heatdevice.generator.t2;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import com.teammoeg.frostedheart.FHCapabilities;
import com.teammoeg.frostedheart.FHMultiblocks;
import com.teammoeg.frostedheart.FHTileTypes;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.GeneratorData;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.GeneratorSteamRecipe;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.GeneratorLogic;
import com.teammoeg.frostedheart.content.steamenergy.HeatEnergyNetwork;
import com.teammoeg.frostedheart.content.steamenergy.capabilities.HeatProviderEndPoint;
import com.teammoeg.frostedheart.util.client.ClientUtils;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class T2GeneratorLogic extends GeneratorLogic<T2GeneratorLogic,T2GeneratorState> {
    private static final BlockPos fluidIn = new BlockPos(1, 0, 2);

    private static final BlockPos networkTile = new BlockPos(1, 0, 0);
    private static final BlockPos redstone = new BlockPos(1, 1, 2);




    public T2GeneratorLogic() {
        super(FHMultiblocks.GENERATOR_T2, FHTileTypes.GENERATOR_T2.get(), false);
    }

    @Override
    protected void callBlockConsumerWithTypeCheck(Consumer<T2GeneratorLogic> consumer, BlockEntity te) {
        if (te instanceof T2GeneratorLogic)
            consumer.accept((T2GeneratorLogic) te);
    }


    @Override
    protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource) {
        return side == this.getFacing() && this.posInMultiblock.equals(fluidIn);
    }


    @Override
    public void disassemble() {
    	if(manager!=null)
        manager.invalidate();
        super.disassemble();
    }


    @Override
    protected IFluidTank[] getAccessibleFluidTanks(Direction side) {
        T2GeneratorLogic master = master();
        if (master != null && side == this.getFacing() && this.posInMultiblock.equals(fluidIn))
            return new FluidTank[]{master.tank};
        return new FluidTank[0];
    }





    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX() - 2, worldPosition.getY() - 2, worldPosition.getZ() - 2, worldPosition.getX() + 2, worldPosition.getY() + 6,
                worldPosition.getZ() + 2);
    }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        liquidtick = nbt.getInt("liquid_tick");
        tank.readFromNBT(nbt.getCompound("fluid"));

    }

    @Override
    protected void tickControls() {
        super.tickControls();
        int power = this.level.getDirectSignalTo(getBlockPosForPos(redstone));
        if (power > 0) {
            if (power > 10) {
                if (!this.isOverdrive()) this.setOverdrive(true);
                if (!this.isWorking()) this.setWorking(true);
            } else if (power > 5) {
                if (this.isOverdrive()) this.setOverdrive(false);
                if (!this.isWorking()) this.setWorking(true);
            } else {
                if (this.isWorking()) this.setWorking(false);
            }
        }
    }

    @Override
    protected void tickEffects(boolean isActive) {
        if (isActive) {
            BlockPos blockpos = this.getBlockPos().relative(Direction.UP, 5);
            Random random = level.random;
            if (random.nextFloat() < (isOverdrive() ? 0.8F : 0.5F)) {
                // for (int i = 0; i < random.nextInt(2)+1; ++i) {
//                if (this.liquidtick != 0 && random.nextFloat() < 0.06F) {
//                    ClientUtils.spawnSteamParticles(world, blockpos);
//                }
                ClientUtils.spawnT2FireParticles(level, blockpos);
                Vec3 wind = new Vec3(0, 0, 0);
                ClientUtils.spawnInvertedConeSteam(level, blockpos, wind);
            }
            /*
            if (this.isWorking() && this.getHeated() == getMaxHeated() && this.tickUntilStopBoom > 0) {
                ClientUtils.spawnSteamParticles(world, blockpos);
                this.tickUntilStopBoom--;
                this.notFullPowerTick = 0;
            }
            if (this.getHeated() < getMaxHeated()) {
                this.notFullPowerTick++;
                if (this.notFullPowerTick > this.nextBoom) {
                    this.notFullPowerTick = this.nextBoom;
                    this.tickUntilStopBoom = 20;
                }
            }*/
        }
    }
    LazyOptional<HeatProviderEndPoint> ep;
    @Override
	public <X> LazyOptional<X> getCapability(Capability<X> capability, Direction facing) {
    	if(capability==FHCapabilities.HEAT_EP.capability()&&facing == this.getFacing().getOpposite() && this.posInMultiblock.equals(networkTile)) {
    		LazyOptional<HeatProviderEndPoint> cep=getData().map(t->t.epcap).orElseGet(LazyOptional::empty);
    		if(ep!=cep) {
    			ep=cep;
    		}
    		return ep==null?LazyOptional.empty():ep.cast();
    	}
		return super.getCapability(capability, facing);
	}

	@Override
    protected boolean tickFuel() {
    	if(manager==null) {
    		 manager = new HeatEnergyNetwork(this, c -> {
    		        Direction dir = this.getFacing();

    		        c.accept(getBlockPosForPos(networkTile).relative(dir.getOpposite()), dir.getOpposite());

    		    });
    	}
        if((!master().getData().map(t->t.ep.hasValidNetwork()).orElse(true)||manager.data.size()<=1)&&!manager.isUpdateRequested()) {
        	manager.requestSlowUpdate();
        }
    	manager.tick();
        boolean active=super.tickFuel();
        if(active)
        	this.tickLiquid();
        return active;
    }

    protected void tickLiquid() {
        Optional<GeneratorData> data = getData();
        this.liquidtick = data.map(t -> t.steamProcess).orElse(0);
        if (!this.getIsActive())
            return;
        float rt = this.getTemperatureLevel();
        /*if (rt == 0) {
            this.spowerMod = 0;
            this.slevelMod = 0;
        }*/
        if (noliquidtick > 0) {
            noliquidtick--;
            return;
        }

        int liquidtick = data.map(t -> t.steamProcess).orElse(0);
        if (liquidtick >= rt) {
            data.ifPresent(t -> t.steamProcess -= (int) rt);
            return;
        }
        GeneratorSteamRecipe sgr = GeneratorSteamRecipe.findRecipe(this.tank.getFluid());
        if (sgr != null) {
            int rdrain = (int) (20 * super.getTemperatureLevel());
            int actualDrain = rdrain * sgr.input.getAmount();
            FluidStack fs = this.tank.drain(actualDrain, FluidAction.SIMULATE);
            if (fs.getAmount() >= actualDrain) {
                data.ifPresent(t->{t.steamProcess= rdrain;t.steamLevel = sgr.level;t.power=sgr.power;}); 

                final FluidStack fs2 = this.tank.drain(actualDrain, FluidAction.EXECUTE);
                data.ifPresent(t -> t.fluid = fs2.getFluid());
                return;
            }
        } else {
        	data.ifPresent(t->{t.steamLevel=0;t.steamProcess=0;t.power=0;});
        }
        noliquidtick = 40;
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if(!this.isDummy()||descPacket) {
	        CompoundTag tankx = new CompoundTag();
	        tank.writeToNBT(tankx);
	        nbt.putFloat("liquid_tick", liquidtick);
	        nbt.put("fluid", tankx);
        }
    }


	@Override
	public IETemplateMultiblock getNextLevelMultiblock() {
		return null;
	}


}
