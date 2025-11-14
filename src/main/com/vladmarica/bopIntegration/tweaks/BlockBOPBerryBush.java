package com.vladmarica.bopIntegration.tweaks;

import com.vladmarica.bopIntegration.BOPIntegrationMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBOPBerryBush extends BlockBush implements IGrowable {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons = new IIcon[4];

    public BlockBOPBerryBush() {
        super(Material.plants);
        this.setHardness(0.0F);
        this.setStepSound(soundTypeGrass);
        this.setTickRandomly(true);
        setBlockName("berry_bush");
        setBlockTextureName("bopintegration_catty:berry_bush_stage0");
    }

    /* ============================================================
        MUST HAVE: Bush survival logic
        Fixes "bone meal deletes plant"
    ============================================================ */

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        Block soil = world.getBlock(x, y - 1, z);

        return soil == Blocks.grass ||
                soil == Blocks.dirt ||
                soil == Blocks.farmland;
    }

    @Override
    protected boolean canPlaceBlockOn(Block block) {
        return block == Blocks.grass ||
                block == Blocks.dirt ||
                block == Blocks.farmland;
    }

    /* ============================================================
       Allow replacing tallgrass/fern
    ============================================================ */
    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);

        if (world.isAirBlock(x, y, z)) return true;

        if (block == Blocks.tallgrass || block == Blocks.deadbush)
            return true;

        return super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        Block b = world.getBlock(x, y, z);

        if (b == Blocks.tallgrass || b == Blocks.deadbush)
            world.setBlockToAir(x, y, z);

        super.onBlockAdded(world, x, y, z);
    }

    /* ============================================================
        Random Growth
    ============================================================ */

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);

        if (!canBlockStay(world, x, y, z)) {
            world.setBlockToAir(x, y, z);
            return;
        }

        int meta = world.getBlockMetadata(x, y, z);

        if (meta < 3 && rand.nextInt(10) == 0)
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
    }

    /* ============================================================
        Drops
    ============================================================ */

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        Item berry = (Item) Item.itemRegistry.getObject("BiomesOPlenty:food");
        return berry != null ? berry : Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random rand) {
        if (meta >= 3)
            return 2 + rand.nextInt(3); // 2–4 berries
        return 1;
    }

    /* ============================================================
        Icons
    ============================================================ */

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        icons[0] = reg.registerIcon("bopintegration:berry_bush_stage0");
        icons[1] = reg.registerIcon("bopintegration:berry_bush_stage1");
        icons[2] = reg.registerIcon("bopintegration:berry_bush_stage2");
        icons[3] = reg.registerIcon("bopintegration:berry_bush_stage3");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (meta < 0) meta = 0;
        if (meta > 3) meta = 3;
        return icons[meta];
    }

    /* ============================================================
        IGrowable (Bone meal)
    ============================================================ */

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

        if (meta < 3)
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
    }

}