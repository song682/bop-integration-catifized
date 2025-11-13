package com.vladmarica.bopIntegration.tweaks;
import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockBOPBerryBush extends BlockBush implements IGrowable {

    private final IIcon[] icons = new IIcon[4];

    public BlockBOPBerryBush() {
        super(Material.plants);
        this.setTickRandomly(true);
        this.setBlockName("bopBerryBush");
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(0.0F);
        this.setStepSound(soundTypeGrass);
    }

    @Override
    protected boolean canPlaceBlockOn(net.minecraft.block.Block block) {
        return block == Blocks.grass || block == Blocks.dirt || block == Blocks.farmland;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);
        int meta = world.getBlockMetadata(x, y, z);
        if (meta < 3 && rand.nextInt(10) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
        }
    }

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        Item berry = (Item) Item.itemRegistry.getObject("biomesoplenty:berry");
        return berry != null ? berry : Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random rand) {
        return 1 + rand.nextInt(2);
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        this.icons[0] = reg.registerIcon("bopintegration_catty:berry_bush_stage0");
        this.icons[1] = reg.registerIcon("bopintegration_catty:berry_bush_stage1");
        this.icons[2] = reg.registerIcon("bopintegration_catty:berry_bush_stage2");
        this.icons[3] = reg.registerIcon("bopintegration_catty:berry_bush_stage3");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta < 0 || meta > 3) meta = 3;
        return this.icons[meta];
    }

    // IGrowable实现（骨粉支持）
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        return world.getBlockMetadata(x, y, z) < 3;
    }

    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return true;
    }

    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta < 3) {
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
        }
    }
}