package com.teammoeg.frostedheart.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RelicDimChunkGenerator extends ChunkGenerator {
    public static final Codec<RelicDimChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BiomeProvider.CODEC.fieldOf("biome_source").forGetter((generator) -> {
            return generator.getBiomeProvider();
        })).apply(instance, instance.stable(RelicDimChunkGenerator::new));
    });


    public RelicDimChunkGenerator(BiomeProvider provider) {
        super(provider, new DimensionStructuresSettings(false));
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    public ChunkGenerator func_230349_a_(long p_230349_1_) {
        return new RelicDimChunkGenerator(this.biomeProvider.getBiomeProvider(p_230349_1_));
    }

    @Override
    public void generateSurface(WorldGenRegion p_225551_1_, IChunk p_225551_2_) {

    }

    @Override
    public void func_230352_b_(IWorld p_230352_1_, StructureManager p_230352_2_, IChunk p_230352_3_) {

    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        return 64;
    }

    @Override
    public IBlockReader func_230348_a_(int p_230348_1_, int p_230348_2_) {
        return null;
    }
}
