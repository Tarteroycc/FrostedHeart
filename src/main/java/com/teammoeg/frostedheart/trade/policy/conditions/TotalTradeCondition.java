/*
 * Copyright (c) 2024 TeamMoeg
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

package com.teammoeg.frostedheart.trade.policy.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teammoeg.frostedheart.trade.FHVillagerData;
import com.teammoeg.frostedheart.trade.policy.Conditions;
import com.teammoeg.frostedheart.trade.policy.PolicyCondition;
import net.minecraft.network.PacketBuffer;

public class TotalTradeCondition implements PolicyCondition {
    int level;

    public TotalTradeCondition(int level) {
        super();
        this.level = level;
    }

    public TotalTradeCondition(JsonObject jo) {
        this(jo.get("point").getAsInt());
    }

    public TotalTradeCondition(PacketBuffer buffer) {
        this(buffer.readVarInt());
    }

    @Override
    public JsonElement serialize() {
        JsonObject jo = new JsonObject();
        Conditions.writeType(this, jo);
        jo.addProperty("point", level);
        return jo;
    }

    @Override
    public void write(PacketBuffer buffer) {
        Conditions.writeId(this, buffer);
        buffer.writeVarInt(level);
    }

    @Override
    public boolean test(FHVillagerData ve) {
        return ve.getTotaltraded() >= level;
    }

}
