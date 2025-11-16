package com.vladmarica.bopIntegration.tweaks;

import biomesoplenty.BiomesOPlenty;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class BlockBOPBerryBush extends BlockBush implements IGrowable {

    // 调试开关：上线后改为 false
    private static final boolean DEBUG_LOG = false;

    @SideOnly(Side.CLIENT)
    private IIcon[] icons = new IIcon[4];

    public BlockBOPBerryBush() {
        super(getMaterialPlants());
        this.setHardness(0.0F);
        this.setStepSound(soundTypeGrass);
        this.setTickRandomly(true);
        setBlockName("bopBerryBush");
        this.setCreativeTab(BiomesOPlenty.tabBiomesOPlenty);
        setBlockTextureName("bopintegration:berry_bush_stage0");
    }

    private static Material getMaterialPlants() {
        Class<?> cls = Material.class;
        String[] names = new String[] {
                "field_151585_k", // runtime obfuscated name
                "plants",         // MCP SRG name
                "PLANTS",         // possible alternative
                "PLANT"           // possible alternative
        };

        for (String name : names) {
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                Object value = f.get(null);
                if (value instanceof Material) {
                    return (Material) value;
                }
            } catch (Throwable ignored) {}
        }

        // fallback
        return Material.vine; // vanilla plant/vine behavior
    }

    /* ---------------------------
       基本存活判定（必须实现）
       --------------------------- */
    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        Block soil = world.getBlock(x, y - 1, z);
        boolean ok = (soil == Blocks.grass || soil == Blocks.dirt || soil == Blocks.farmland);
        if (DEBUG_LOG && !ok) {
            BOPIntegrationMod.logger.info ("[BerryBush] canBlockStay=false at " + x + "," + y + "," + z
                    + " soil=" + (soil == null ? "null" : soil.getUnlocalizedName()));
        }
        return ok;
    }

    @Override
    protected boolean canPlaceBlockOn(Block block) {
        return block == Blocks.grass || block == Blocks.dirt || block == Blocks.farmland;
    }

    /* Allow placement on tallgrass/deadbush (so placing replaces them) */
    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        Block b = world.getBlock(x, y, z);
        if (world.isAirBlock(x, y, z)) return true;
        if (b == Blocks.tallgrass || b == Blocks.deadbush) return true;
        return super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

        int meta = world.getBlockMetadata(x, y, z);

        // Only act on fully grown bush
        if (meta == 3) {
            // SERVER: drop berries + reset stage
            if (!world.isRemote) {
                // Optional: drop berries (这里是掉落 2~4 个浆果)
                Item berryItem = (Item) Item.itemRegistry.getObject("BiomesOPlenty:food");
                if (berryItem != null) {
                    int count = 2 + world.rand.nextInt(3); // 2~4
                    this.dropBlockAsItem(world, x, y, z,
                            new ItemStack(berryItem, count, 0));
                }

                // Reset to regrow stage (meta = 1)
                world.setBlockMetadataWithNotify(x, y, z, 1, 3);
            }
            return true;
        }
        return false; // No special action for other stages
    }

    /* When block added, clear tallgrass there (same behavior as before) */
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        // If there is tall grass or dead bush at the same pos, clear it (defensive)
        Block present = world.getBlock(x, y, z);
        if (present == Blocks.tallgrass || present == Blocks.deadbush) {
            world.setBlockToAir(x, y, z);
            if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] onBlockAdded cleared tallgrass at " + x + "," + y + "," + z);
        }

        super.onBlockAdded(world, x, y, z);
    }

    /* Robust neighbor change handling:
       when something below changes, we must check canBlockStay and drop properly
    */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);

        if (!this.canBlockStay(world, x, y, z)) {
            // Instead of just setBlockToAir, explicitly drop items then clear block
            if (!world.isRemote) {
                this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
                if (DEBUG_LOG) BOPIntegrationMod.logger.info ("[BerryBush] neighbor change -> cannot stay -> dropping at " + x + "," + y + "," + z);
            }

            world.setBlockToAir(x, y, z);
        }
    }

    /* Random ticks: growth */
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        super.updateTick(world, x, y, z, rand);

        // If it cannot stay, drop and clear (defensive)
        if (!this.canBlockStay(world, x, y, z)) {
            if (!world.isRemote) {
                this.dropBlockAsItem (world, x, y, z, world.getBlockMetadata(x, y, z), 0);
                if (DEBUG_LOG) BOPIntegrationMod.logger.info ("[BerryBush] updateTick - cannot stay -> dropping at " + x + "," + y + "," + z);
            }
            world.setBlockToAir (x, y, z);
            return;
        }

        int meta = world.getBlockMetadata(x, y, z);
        if (meta < 3 && rand.nextInt(10) == 0) {
            int newMeta = meta + 1;
            world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
            if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] updateTick grew from " + meta + " to " + newMeta + " at " + x + "," + y + "," + z);
        }
    }

    /* Bone meal support */
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        // if disabled by config, return false (you can add ConfigHandler check here)
        return world.getBlockMetadata(x, y, z) < 3 && this.canBlockStay(world, x, y, z);
    }

    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return true;
    }

    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        // double-check canBlockStay before applying bone meal
        if (!this.canBlockStay(world, x, y, z)) {
            if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] bone meal attempted but canBlockStay=false at " + x + "," + y + "," + z);
            return;
        }
        int meta = world.getBlockMetadata(x, y, z);
        if (meta < 3) {
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
            if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] bone meal progressed meta " + meta + " -> " + (meta + 1) + " at " + x + "," + y + "," + z);
        }
    }

    /* Drops */
    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        Item berry = (Item) Item.itemRegistry.getObject("BiomesOPlenty:food");
        return (berry != null) ? berry : Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random rand) {
        if (meta >= 3) {
            return 2 + rand.nextInt(3); // 2~4
        }
        return 1;
    }

    /* When player harvests - ensure correct drops */
    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

        // perform normal drop behavior
        if (!world.isRemote) {
            this.dropBlockAsItem(world, x, y, z, meta, 0);
            if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] onBlockHarvested at " + x + "," + y + "," + z + " meta=" + meta + " by " + player.getCommandSenderName());
        }
        super.onBlockHarvested(world, x, y, z, meta, player);
    }

    /* Harvest logic for creative / silk touch etc. */
    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] harvestBlock at " + x + "," + y + "," + z + " meta=" + meta + " by " + player.getCommandSenderName());
        super.harvestBlock(world, player, x, y, z, meta);
    }

    /* Register icons */
    @Override
    @SideOnly(Side.CLIENT)
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

    /* Ensure item drops when block is broken by other means */
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();

        Item item = getItemDropped(metadata, world.rand, fortune);
        int qty = quantityDropped(metadata, fortune, world.rand);

        if (item != null && qty > 0) {
            drops.add(new ItemStack(item, qty));
        }

        if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] getDrops at " + x + "," + y + "," + z + " meta=" + metadata + " -> item=" + (item == null ? "null" : item.getUnlocalizedName()) + " qty=" + qty);

        return drops;
    }

    /* Ensure block placed by item properly initializes */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        // Ensure metadata correct if stack contains information — default to 0
        world.setBlockMetadataWithNotify(x, y, z, 0, 3);
        if (DEBUG_LOG) BOPIntegrationMod.logger.info("[BerryBush] onBlockPlacedBy at " + x + "," + y + "," + z + " by " + (placer == null ? "null" : (placer.getCommandSenderName())));
        // super.onBlockPlacedBy(world, x, y, z, placer, stack);
    }

}