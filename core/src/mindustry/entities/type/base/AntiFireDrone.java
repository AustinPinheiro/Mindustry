package mindustry.entities.type.base;

import arc.math.geom.*;
import arc.struct.*;
import arc.math.*;
import mindustry.entities.effect.*;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitState;
import mindustry.world.Pos;
import mindustry.world.Tile;

import java.io.*;

import static mindustry.Vars.*;

public class AntiFireDrone extends BaseDrone{
    public final UnitState fireState = new UnitState(){

        public void entered(){
            target = null;
        }

        public void update(){

            boolean targetFound = false;
            if(retarget()){
                //convert from drone space to tile space (not 100% accurate)
                int xTile = (int)x / 8;
                int yTile = (int)y / 8;

                //calculate and store variables
                int range = (int) type.range;
                int xMin = xTile-range;
                int xMax = xTile+range;
                int yMin = yTile-range;
                int yMax = yTile+range;

                //Loop though all possible tiles in range and store those that have fire
                Array<Tile> tiles = new Array<>();

                for(int rx = xMin; rx <= xMax; rx++){
                    for(int ry = yMin; ry <= yMax; ry++){
                        if(Fire.has(rx, ry)){
                            Tile t = world.tile(rx, ry);
                            tiles.add(t);
                            targetFound = true;
                        }
                    }
                }

                //the target is closed fire tile.
                target = Geometry.findClosest(x, y, tiles);

                if(!targetFound){
                    target = null;
                }
            }

            if(target != null){
                if(target.dst(AntiFireDrone.this) > type.range){
                    circle(type.range * 0.9f);
                }else{
                    getWeapon().update(AntiFireDrone.this, target.getX(), target.getY());
                }
            }else{
                //circle spawner if there's nothing to repair
                if(getSpawner() != null){
                    target = getSpawner();
                    circle(type.range * 1.5f, type.speed / 2f);
                    target = null;
                }
            }
        }
    };

    @Override
    public boolean shouldRotate(){
        return target != null;
    }

    @Override
    public UnitState getStartState(){
        return fireState;
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.write(data);
        data.writeInt(state.is(fireState) && target instanceof TileEntity ? ((TileEntity)target).tile.pos() : Pos.invalid);
    }

    @Override
    public void read(DataInput data) throws IOException{
        super.read(data);
        Tile removeFire = world.tile(data.readInt());

        if(removeFire != null){
            target = removeFire.entity;
        }
    }
}
