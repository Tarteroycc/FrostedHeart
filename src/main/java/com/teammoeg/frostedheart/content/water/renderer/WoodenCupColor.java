package com.teammoeg.frostedheart.content.water.renderer;

import com.teammoeg.frostedheart.content.water.util.FluidHelper;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;


public class WoodenCupColor implements ItemColor {
    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (tintIndex == 1) {
            int color = FluidUtil.getFluidHandler(itemStack).map(h -> h.getFluidInTank(0).getFluid()).map(FluidHelper::getColor).get();
            if (color == 0) {
                return -1;
            }
            return color;
        }
        else return -1;
    }
}
