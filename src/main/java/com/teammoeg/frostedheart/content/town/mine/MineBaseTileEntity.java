package com.teammoeg.frostedheart.content.town.mine;

import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.FHTileTypes;
import com.teammoeg.frostedheart.content.town.AbstractTownWorkerTileEntity;
import com.teammoeg.frostedheart.content.town.TownWorkerState;
import com.teammoeg.frostedheart.content.town.TownWorkerType;
import com.teammoeg.frostedheart.content.town.house.HouseTileEntity;
import com.teammoeg.frostedheart.util.blockscanner.BlockScanner;
import com.teammoeg.frostedheart.util.blockscanner.FloorBlockScanner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.exp;

public class MineBaseTileEntity extends AbstractTownWorkerTileEntity {
    public Set<BlockPos> linkedMines = new HashSet<>();
    private int volume;
    private int area;
    private int rack;
    private int chest;
    private double temperature;
    private double rating;
    private boolean addedToSchedulerQueue = false;

    public MineBaseTileEntity(){
        super(FHTileTypes.MINE_BASE.get());
    }

    public boolean isStructureValid(){
        BlockPos mineBasePos = this.getBlockPos();
        BlockPos doorPos = BlockScanner.getDoorAdjacent(level, mineBasePos);
        if (doorPos == null) return false;
        BlockPos floorBelowDoor = BlockScanner.getBlockBelow((pos)->!(Objects.requireNonNull(level).getBlockState(pos).is(BlockTags.DOORS)), doorPos);//找到门下面垫的的那个方块
        for (Direction direction : BlockScanner.PLANE_DIRECTIONS) {
            assert floorBelowDoor != null;
            BlockPos startPos = floorBelowDoor.relative(direction);//找到门下方块旁边的方块
            if (!FloorBlockScanner.isValidFloorOrLadder(Objects.requireNonNull(level), startPos)) {//如果门下方块旁边的方块不是合法的地板，找一下它下面的方块
                if (!FloorBlockScanner.isValidFloorOrLadder(Objects.requireNonNull(level), startPos.below()) || FloorBlockScanner.isHouseBlock(level, startPos.above(2))) {//如果它下面的方块也不是合法地板（或者梯子），或者门的上半部分堵了方块，就不找了。我们默认村民不能从两格以上的高度跳下来，也不能从一格高的空间爬过去
                    continue;
                }
                startPos = startPos.below();
            }
            MineBaseBlockScanner scanner = new MineBaseBlockScanner(level, startPos);
            FHMain.LOGGER.info("New scanner created; Start pos: " + startPos);
            if(scanner.scan()){
                FHMain.LOGGER.info("scan successful");
                this.area = scanner.getArea();
                this.volume = scanner.getVolume();
                this.rack = scanner.getRack();
                this.chest = scanner.getChest();
                this.linkedMines = scanner.getLinkedMines();
                this.occupiedArea = scanner.getOccupiedArea();
                this.temperature = scanner.getTemperature();
                return true;
            }
        }
        return false;
    }

    public double computeRating() {
        double rackRating = 1 - exp(-this.rack);
        double chestRating = 1 - exp(-this.chest * 0.4);
        double spaceRating = HouseTileEntity.calculateSpaceRating(this.volume, this.area);
        double temperatureRating = HouseTileEntity.calculateTemperatureRating(this.temperature);
        return this.rating = spaceRating*0.15 + temperatureRating*0.15 + chestRating*0.35 + rackRating*0.35;
    }

    @Override
    public void readCustomNBT(CompoundTag compoundNBT, boolean b) {

    }

    @Override
    public void writeCustomNBT(CompoundTag compoundNBT, boolean b) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public TownWorkerType getWorkerType() {
        return null;
    }


    @Override
    public CompoundTag getWorkData() {
        CompoundTag nbt = getBasicWorkData();
        if(this.isValid()) {
            nbt.putInt("volume", this.volume);
            nbt.putInt("area", this.area);
            nbt.putInt("rack", this.rack);
            nbt.putInt("chest", this.chest);
            ListTag list = new ListTag();
            for (BlockPos pos : this.linkedMines) {
                list.add(LongTag.valueOf(pos.asLong()));
            }
            nbt.put("linkedMines", list);
            nbt.putDouble("temperature", this.temperature);
            nbt.putDouble("rating", this.rating);
        }
        return nbt;
    }

    @Override
    public void setWorkData(CompoundTag data) {
        setBasicWorkData(data);
        if(this.isValid()) {
            this.volume = data.getInt("volume");
            this.area = data.getInt("area");
            this.rack = data.getInt("rack");
            this.chest = data.getInt("chest");
            this.linkedMines.clear();
            ListTag list = data.getList("linkedMines", Constants.NBT.TAG_LONG);
            list.forEach(nbt-> this.linkedMines.add( BlockPos.of( ((LongTag)nbt).getAsLong() )));
        }
    }

    public double getRating(){
        if(isWorkValid()) {
            if (this.rating == 0) return this.computeRating();
            return this.rating;
        }
        return 0;
    }
    public int getVolume(){
        return this.isWorkValid()?this.volume:0;
    }
    public Set<BlockPos> getLinkedMines() {
        return this.linkedMines;
    }
    public int getArea() {
        return this.isWorkValid() ? this.area : 0;
    }
    public int getRack() {
        return this.isWorkValid() ? this.rack : 0;
    }
    public int getChest() {
        return this.isWorkValid() ? this.chest : 0;
    }
    public double getTemperature() {
        return this.isWorkValid() ? this.temperature : 0;
    }

    public void setLinkedBaseToAllLinkedMines(){
        if(this.isWorkValid()) {
            for(BlockPos minePos:this.linkedMines){
                assert level != null;
                MineTileEntity mineTileEntity = (MineTileEntity) level.getBlockEntity(minePos);
                if(mineTileEntity!=null)
                    mineTileEntity.setLinkedBase(this.getBlockPos(),this.getRating());
            }
        }
    }


    public void refresh() {
        if(this.isOccupiedAreaOverlapped()){
            this.isStructureValid();
            return;
        }
        this.workerState = isStructureValid() ? TownWorkerState.VALID : TownWorkerState.NOT_VALID;
    }

    // ScheduledTaskTileEntity
    @Override
    public void executeTask() {
        this.refresh();
        this.setLinkedBaseToAllLinkedMines();
    }
    @Override
    public boolean isStillValid() {
        return this.isWorkValid();
    }
}
