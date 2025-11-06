package com.vladmarica.bopIntegration.hee;

import chylex.hee.world.structure.tower.StructureTower;
import net.minecraft.world.World;

import java.util.Random;

public class CustomStructureTower extends StructureTower {
    public CustomStructureTower() {
        super();
    }

    public CustomStructureTower(World world, Random rand, int x, int z) {
        // 调用父类构造方法
        super(world, rand, x, z);

        // 清空原有的组件，添加我们的自定义组件
        this.components.clear();
        this.components.add(new CustomComponentTower(world, rand, x * 16, z * 16));
        this.updateBoundingBox();
    }
}
