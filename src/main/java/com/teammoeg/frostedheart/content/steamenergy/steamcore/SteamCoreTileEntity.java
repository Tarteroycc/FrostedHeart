package com.teammoeg.frostedheart.content.steamenergy.steamcore;

import java.util.Objects;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.teammoeg.frostedheart.FHCapabilities;
import com.teammoeg.frostedheart.infrastructure.config.FHConfig;
import com.teammoeg.frostedheart.base.block.FHBlockInterfaces;
import com.teammoeg.frostedheart.base.block.FHTickableBlockEntity;
import com.teammoeg.frostedheart.content.steamenergy.capabilities.HeatConsumerEndpoint;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class SteamCoreTileEntity extends GeneratingKineticBlockEntity implements
        FHTickableBlockEntity, IHaveGoggleInformation,
        FHBlockInterfaces.IActiveState{
    public SteamCoreTileEntity(BlockEntityType<?> type,BlockPos pos,BlockState state) {
        super(type, pos, state);
        this.setLazyTickRate(20);
    }

    HeatConsumerEndpoint network = new HeatConsumerEndpoint(10, FHConfig.COMMON.steamCoreMaxPower.get().floatValue(),FHConfig.COMMON.steamCorePowerIntake.get().floatValue());
    
    public float getGeneratedSpeed(){
        float speed = FHConfig.COMMON.steamCoreGeneratedSpeed.get().floatValue();
        if(getIsActive()) return speed;
        return 0f;
    }

    public float calculateAddedStressCapacity() {
        if(getIsActive()) return FHConfig.COMMON.steamCoreCapacity.get().floatValue();
        return 0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            if(network.tryDrainHeat(FHConfig.COMMON.steamCorePowerIntake.get().floatValue())){
                this.setActive(true);
                if(this.getSpeed() == 0f){
                    this.updateGeneratedRotation();
                }
                setChanged();
            }else {
                this.setActive(false);
                this.updateGeneratedRotation();
            }
        }
    }

    public void lazyTick() {
        super.lazyTick();

    }


    LazyOptional<HeatConsumerEndpoint> heatcap=LazyOptional.of(()->network);
    @Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if(cap==FHCapabilities.HEAT_EP.capability()&&side==this.getBlockState().getValue(BlockStateProperties.FACING).getOpposite()) {
			return heatcap.cast();
		}
		return super.getCapability(cap, side);
	}

	public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.FACING);
    }

 

    @Override
    protected void write(CompoundTag tag, boolean client) {
        super.write(tag, client);
        network.save(tag, client);
    }

    @Override
    public BlockState getState() {
        return this.getBlockState();
    }

    @Override
    public void setState(BlockState blockState) {
        if (this.getWorldNonnull().getBlockState(this.worldPosition) == this.getState()) {
            this.getWorldNonnull().setBlockAndUpdate(this.worldPosition, blockState);
        }
    }

    public Level getWorldNonnull() {
        return Objects.requireNonNull(super.getLevel());
    }

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		network.load(compound, clientPacket);
	}
}
