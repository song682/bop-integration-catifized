package com.vladmarica.bopIntegration.hee;

import chylex.hee.world.structure.tower.ComponentTower;
import chylex.hee.world.structure.ComponentLargeStructureWorld;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Random;

public class CustomComponentTower extends ComponentTower {
    private static final Block replacementBlock = (Block)Block.blockRegistry.getObject("biomesoplenty:crystal");

    public CustomComponentTower() {
        super();
    }

    public CustomComponentTower(World world, Random rand, int x, int z) {
        // 由于原构造方法是protected，我们需要通过反射或其他方式调用
        // 这里我们直接调用父类的protected构造方法（在同一个包中）
        super(world, rand, x, z);
    }

    // 重写生成结构的方法，在生成过程中替换方块
    @Override
    protected int setupStructure(long seed) {
        int result = super.setupStructure(seed);
        replaceGlowstoneInStructure();
        return result;
    }

    private void replaceGlowstoneInStructure() {
        if (replacementBlock == null) return;

        // 遍历结构中的所有方块
        for (int x = 0; x < this.sizeX; x++) {
            for (int y = 0; y < this.sizeY; y++) {
                for (int z = 0; z < this.sizeZ; z++) {
                    // 获取结构中的方块
                    Block block = this.structure.getBlock(x, y, z);

                    // 如果是萤石，则替换
                    if (block == Blocks.glowstone) {
                        this.structure.setBlock(x, y, z, replacementBlock, 0);
                    }
                }
            }
        }
    }
}
