package com.teammoeg.frostedheart.base.capability.codec;

import org.objectweb.asm.Type;

import com.mojang.serialization.Codec;
import com.teammoeg.frostedheart.base.capability.IFHCapability;
import com.teammoeg.frostedheart.mixin.forge.CapabilityManagerAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
/**
 * Basic codec serialized capability type
 * */
public class FHCodecCapability<C> implements IFHCapability{
	private Class<C> capClass;
	private Capability<C> capability;
	private NonNullSupplier<C> factory;
	private Codec<C> codec;

	public FHCodecCapability(Class<C> capClass, NonNullSupplier<C> factory, Codec<C> codec) {
		super();
		this.capClass = capClass;
		this.factory = factory;
		this.codec = codec;
	}
	@SuppressWarnings("unchecked")
	public void register() {
        capability=(Capability<C>) ((CapabilityManagerAccess)(Object)CapabilityManager.INSTANCE).getProviders().get(Type.getInternalName(capClass).intern());
	}
	public ICapabilityProvider provider() {
		return new FHCodecCapabilityProvider<>(this);
	}
	LazyOptional<C> createCapability(){
		return LazyOptional.of(factory);
	}
	public LazyOptional<C> getCapability(Object cap) {
		if(cap instanceof ICapabilityProvider)
			return ((ICapabilityProvider)cap).getCapability(capability);
		return LazyOptional.empty();
	}
    public Capability<C> capability() {
		return capability;
	}
    public Codec<C> codec(){
    	return codec;
    }
	public Class<C> getCapClass() {
		return capClass;
	}
}
