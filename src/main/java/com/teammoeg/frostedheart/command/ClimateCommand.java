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

package com.teammoeg.frostedheart.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.content.climate.ClimateEvent;
import com.teammoeg.frostedheart.content.climate.WorldClimate;
import com.teammoeg.frostedheart.util.TranslateUtils;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.IServerWorldInfo;

public class ClimateCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> get = Commands.literal("get")
                .executes((ct) -> {
                    try {
                        ct.getSource().sendSuccess(TranslateUtils.str(String.valueOf(WorldClimate.get(ct.getSource().getLevel()))), true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return Command.SINGLE_SUCCESS;
                });
        LiteralArgumentBuilder<CommandSource> rebuild = Commands.literal("rebuild").then(Commands.literal("cache").executes(ct -> {

                    WorldClimate.get(ct.getSource().getLevel()).rebuildCache(ct.getSource().getLevel());
                    ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .executes((ct) -> {

                    WorldClimate.get(ct.getSource().getLevel()).resetTempEvent(ct.getSource().getLevel());
                    ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
                    return Command.SINGLE_SUCCESS;
                });
        LiteralArgumentBuilder<CommandSource> init = Commands.literal("init")
                .executes((ct) -> {
                    WorldClimate.get(ct.getSource().getLevel()).addInitTempEvent(ct.getSource().getLevel());
                    ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
                    return Command.SINGLE_SUCCESS;
                });

        LiteralArgumentBuilder<CommandSource> app = Commands.literal("append").then(
                Commands.literal("warm").executes(ct -> {
                    WorldClimate.get(ct.getSource().getLevel()).appendTempEvent(ClimateEvent::getWarmClimateEvent);
                    ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
                    return Command.SINGLE_SUCCESS;
                })
        ).then(
                Commands.literal("cold").executes(ct -> {
                    WorldClimate.get(ct.getSource().getLevel()).appendTempEvent(ClimateEvent::getColdClimateEvent);
                    ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
                    return Command.SINGLE_SUCCESS;
                })
        ).then(
                Commands.literal("blizzard").executes(ct -> {
                    WorldClimate.get(ct.getSource().getLevel()).appendTempEvent(ClimateEvent::getBlizzardClimateEvent);
                    ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
                    return Command.SINGLE_SUCCESS;
                })
        ).executes(ct -> {
            WorldClimate.get(ct.getSource().getLevel()).appendTempEvent(ClimateEvent::getClimateEvent);
            ct.getSource().sendSuccess(TranslateUtils.str("Succeed!").withStyle(TextFormatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        });
        LiteralArgumentBuilder<CommandSource> reset = Commands.literal("resetVanilla")
                .executes((ct) -> {
                    IServerWorldInfo serverWorldInfo=ct.getSource().getLevel().serverLevelData;
                    serverWorldInfo.setThunderTime(0);
                    serverWorldInfo.setRainTime(0);
                    serverWorldInfo.setClearWeatherTime(0);
                    return Command.SINGLE_SUCCESS;
                });

        dispatcher.register(Commands.literal(FHMain.MODID).requires(s -> s.hasPermission(2)).then(Commands.literal("climate").then(get).then(init).then(rebuild).then(reset).then(app)));
    }
}
