package com.vladmarica.bopIntegration.hee;

import chylex.hee.world.structure.tower.MapGenTower;
import net.minecraft.world.gen.structure.StructureStart;

public class CustomMapGenTower extends MapGenTower {

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new CustomStructureTower(this.worldObj, this.rand, chunkX, chunkZ);
    }

}
