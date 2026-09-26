package com.vladmarica.bopIntegration.content.event;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCItems;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import com.vladmarica.bopIntegration.Config;
import com.vladmarica.bopIntegration.content.block.BlockHoneyCauldron;
import com.vladmarica.bopIntegration.proxy.CommonProxy;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

/**
 * 蜂蜜机制事件处理器
 *
 * 三个核心功能：
 * 1. 蜂蜜块→液态蜂蜜：蜂蜜块放在热块上方会融化为液态蜂蜜
 * 2. 炼药锅蜂蜜：炼药锅放在热块上可融化蜂蜜块，空罐可收集
 * 3. 液态蜂蜜→蜂蜜块：暴露在空气中或在热块上太久会晶化
 */
public class EventHoneyMechanics {

    // ==================== 缓存结构 ====================

    /** 已知的蜂蜜块位置：World -> Set<BlockPos> */
    private static final Map<World, Set<BlockPos>> honeyCache = new HashMap<>();

    /** 已知的液态蜂蜜位置：World -> Set<BlockPos> */
    private static final Map<World, Set<BlockPos>> liquidCache = new HashMap<>();

    /** 液态蜂蜜暴露在空气中的起始世界时间：World -> BlockPos -> startTime */
    private static final Map<World, Map<BlockPos, Long>> exposureStart = new HashMap<>();

    /** 液态蜂蜜在热块上的起始世界时间：World -> BlockPos -> startTime */
    private static final Map<World, Map<BlockPos, Long>> hotStart = new HashMap<>();

    /** 热块列表（registry name 字符串） */
    private Set<String> hotBlockNames = new HashSet<>();

    // ==================== 计时器 ====================

    private int meltTickCounter = 0;
    private int crystallizeTickCounter = 0;
    private int rescanTickCounter = 0;

    // ==================== 构造 & 初始化 ====================

    public EventHoneyMechanics(String[] hotBlocksConfig) {
        parseHotBlocks(hotBlocksConfig);
    }

    /** 解析热块配置数组 */
    private void parseHotBlocks(String[] config) {
        hotBlockNames.clear();
        if (config == null || config.length == 0) return;
        for (String name : config) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                hotBlockNames.add(trimmed);
            }
        }
    }

    /** 更新热块列表（配置重载时调用） */
    public void reloadHotBlocks(String[] hotBlocksConfig) {
        parseHotBlocks(hotBlocksConfig);
    }

    /** 判断某个方块是否在热块列表中 */
    private boolean isHotBlock(Block block) {
        if (block == null || hotBlockNames.isEmpty()) return false;
        String name = Block.blockRegistry.getNameForObject(block);
        return name != null && hotBlockNames.contains(name);
    }

    // ==================== WorldTickEvent ====================

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        World world = event.world;
        Config cfg = BOPIntegrationMod.config;

        // ========== 蜂蜜块融化处理（每 honeyMeltInterval 刻） ==========
        if (cfg.honeyEnabled && cfg.honeyMeltEnabled) {
            meltTickCounter++;
            if (meltTickCounter >= cfg.honeyMeltInterval) {
                meltTickCounter = 0;
                processHoneyBlockMelting(world);
            }
        }

        // ========== 液态蜂蜜晶化处理（每 20 刻 = 1 秒） ==========
        if (cfg.honeyEnabled && cfg.honeyCrystallizeEnabled) {
            crystallizeTickCounter++;
            if (crystallizeTickCounter >= 20) {
                crystallizeTickCounter = 0;
                processLiquidHoneyCrystallization(world, cfg);
            }
        }

        // ========== 定期重新扫描（每 200 刻 = 10 秒） ==========
        rescanTickCounter++;
        if (rescanTickCounter >= 200) {
            rescanTickCounter = 0;
            rescanAroundPlayers(world);
        }
    }

    // ==================== 蜂蜜块→液态蜂蜜 ====================

    /** 处理所有缓存的蜂蜜块：检查下方是否热块，如是则融化 */
    private void processHoneyBlockMelting(World world) {
        Set<BlockPos> positions = honeyCache.get(world);
        if (positions == null || positions.isEmpty()) return;

        Iterator<BlockPos> it = positions.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();

            // 如果方块已不存在或已变化，移除跟踪
            if (world.getBlock(pos.x, pos.y, pos.z) != BOPCBlocks.honeyBlock) {
                it.remove();
                continue;
            }

            // 检查下方是否是热块
            Block below = world.getBlock(pos.x, pos.y - 1, pos.z);
            if (isHotBlock(below)) {
                // 融化为液态蜂蜜（完整块 meta=7）
                world.setBlock(pos.x, pos.y, pos.z, BOPCBlocks.honey, 7, 3);
                // 添加到液态蜂蜜缓存
                addToLiquidCache(world, pos);
                it.remove();
            }
        }

        // 清理空世界条目
        if (positions.isEmpty()) {
            honeyCache.remove(world);
        }
    }

    // ==================== 液态蜂蜜→蜂蜜块（晶化） ====================

    /** 处理液态蜂蜜的晶化逻辑 */
    private void processLiquidHoneyCrystallization(World world, Config cfg) {
        long worldTime = world.getTotalWorldTime();

        // --- 处理暴露在空气中的晶化 ---
        Map<BlockPos, Long> exposureMap = exposureStart.get(world);
        if (exposureMap != null) {
            Iterator<Map.Entry<BlockPos, Long>> it = exposureMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Long> entry = it.next();
                BlockPos pos = entry.getKey();

                // 检查方块是否还是液态蜂蜜
                if (world.getBlock(pos.x, pos.y, pos.z) != BOPCBlocks.honey) {
                    it.remove();
                    continue;
                }

                // 检查是否仍然暴露在空气中
                Block above = world.getBlock(pos.x, pos.y + 1, pos.z);
                boolean exposed = above.isAir(world, pos.x, pos.y + 1, pos.z);

                if (!exposed) {
                    // 被盖住了，重置计时
                    it.remove();
                    continue;
                }

                // 检查是否超时
                if (worldTime - entry.getValue() >= cfg.crystallizeExposureTime) {
                    // 晶化为蜂蜜块
                    world.setBlock(pos.x, pos.y, pos.z, BOPCBlocks.honeyBlock, 0, 3);
                    addToHoneyCache(world, pos);
                    it.remove();
                }
            }

            if (exposureMap.isEmpty()) {
                exposureStart.remove(world);
            }
        }

        // --- 处理在热块上的晶化 ---
        Map<BlockPos, Long> hotMap = hotStart.get(world);
        if (hotMap != null) {
            Iterator<Map.Entry<BlockPos, Long>> it = hotMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Long> entry = it.next();
                BlockPos pos = entry.getKey();

                // 检查方块是否还是液态蜂蜜
                if (world.getBlock(pos.x, pos.y, pos.z) != BOPCBlocks.honey) {
                    it.remove();
                    continue;
                }

                // 检查下方是否仍然是热块
                Block below = world.getBlock(pos.x, pos.y - 1, pos.z);
                if (!isHotBlock(below)) {
                    it.remove();
                    continue;
                }

                // 检查是否超时
                if (worldTime - entry.getValue() >= cfg.crystallizeHotTime) {
                    // 过热晶化为蜂蜜块
                    world.setBlock(pos.x, pos.y, pos.z, BOPCBlocks.honeyBlock, 0, 3);
                    addToHoneyCache(world, pos);
                    it.remove();
                }
            }

            if (hotMap.isEmpty()) {
                hotStart.remove(world);
            }
        }
    }

    // ==================== 定期扫描 ====================

    /** 在玩家周围扫描新的蜂蜜块和液态蜂蜜 */
    private void rescanAroundPlayers(World world) {
        for (Object obj : world.playerEntities) {
            EntityPlayer player = (EntityPlayer) obj;
            int px = (int) player.posX;
            int py = (int) player.posY;
            int pz = (int) player.posZ;

            // 扫描 12x8x12 范围
            for (int dx = -12; dx <= 12; dx++) {
                for (int dy = -4; dy <= 4; dy++) {
                    for (int dz = -12; dz <= 12; dz++) {
                        int x = px + dx;
                        int y = py + dy;
                        int z = pz + dz;

                        Block block = world.getBlock(x, y, z);
                        if (block == BOPCBlocks.honeyBlock) {
                            addToHoneyCache(world, new BlockPos(x, y, z));
                        } else if (block == BOPCBlocks.honey) {
                            addToLiquidCache(world, new BlockPos(x, y, z));
                        }
                    }
                }
            }
        }
    }

    // ==================== 缓存管理 ====================

    private void addToHoneyCache(World world, BlockPos pos) {
        Set<BlockPos> positions = honeyCache.computeIfAbsent(world, k -> new HashSet<>());
        positions.add(pos);
    }

    private void addToLiquidCache(World world, BlockPos pos) {
        Set<BlockPos> positions = liquidCache.computeIfAbsent(world, k -> new HashSet<>());
        if (positions.add(pos)) {
            // 新发现的液态蜂蜜，检查是否需要开始计时
            Block above = world.getBlock(pos.x, pos.y + 1, pos.z);
            Block below = world.getBlock(pos.x, pos.y - 1, pos.z);

            boolean exposed = above.isAir(world, pos.x, pos.y + 1, pos.z);

            if (exposed) {
                Map<BlockPos, Long> expMap = exposureStart.computeIfAbsent(world, k -> new HashMap<>());
                if (!expMap.containsKey(pos)) {
                    expMap.put(pos, world.getTotalWorldTime());
                }
            }

            if (isHotBlock(below)) {
                Map<BlockPos, Long> hotMap = hotStart.computeIfAbsent(world, k -> new HashMap<>());
                if (!hotMap.containsKey(pos)) {
                    hotMap.put(pos, world.getTotalWorldTime());
                }
            }
        }
    }


    // ==================== 炼药锅交互 ====================

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.world.isRemote) return;
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        Config cfg = BOPIntegrationMod.config;
        if (!cfg.honeyEnabled || !cfg.honeyCauldronEnabled) return;

        World world = event.world;
        int x = event.x;
        int y = event.y;
        int z = event.z;
        Block clicked = world.getBlock(x, y, z);

        // 原版炼药锅 或 蜂蜜炼药锅都处理
        if (clicked != Blocks.cauldron && !(clicked instanceof BlockHoneyCauldron)) return;

        EntityPlayer player = event.entityPlayer;
        ItemStack held = player.getHeldItem();
        if (held == null) return;

        // 检查炼药锅下方是否是热块
        Block below = world.getBlock(x, y - 1, z);
        boolean isOnHot = isHotBlock(below);

        if (held.getItem() == BOPCItems.jarEmpty && isOnHot) {
            // 空罐 + 炼药锅在热块上 → 收集蜂蜜
            handleCollectHoneyFromCauldron(world, x, y, z, player, held);
            event.setCanceled(true);
        } else if (held.getItem() == Item.getItemFromBlock(BOPCBlocks.honeyBlock) && isOnHot) {
            // 蜂蜜块 + 炼药锅在热块上 → 融化蜂蜜到炼药锅
            handleMeltHoneyInCauldron(world, x, y, z, player, held);
            event.setCanceled(true);
        }
    }

    /** 从炼药锅中收集蜂蜜（空罐→满罐） */
    private void handleCollectHoneyFromCauldron(World world, int x, int y, int z, EntityPlayer player, ItemStack jarEmpty) {
        Block block = world.getBlock(x, y, z);
        // 必须是蜂蜜炼药锅才有蜂蜜
        if (!(block instanceof BlockHoneyCauldron)) {
            CommonProxy.logger.debug("Cauldron at ({},{},{}) has no honey", x, y, z);
            return;
        }

        int honeyLevel = world.getBlockMetadata(x, y, z);
        if (honeyLevel <= 0) return;

        // 消耗一个空罐
        if (!player.capabilities.isCreativeMode) {
            jarEmpty.stackSize--;
        }

        // 给予一个蜂蜜满罐
        ItemStack honeyJar = new ItemStack(BOPCItems.jarFilled, 1, 0);
        if (!player.inventory.addItemStackToInventory(honeyJar)) {
            player.dropPlayerItemWithRandomChoice(honeyJar, false);
        }

        // 降低蜂蜜量
        honeyLevel--;
        if (honeyLevel <= 0) {
            // 清空：替换回原版炼药锅
            world.setBlock(x, y, z, Blocks.cauldron, 0, 3);
        } else {
            world.setBlockMetadataWithNotify(x, y, z, honeyLevel, 3);
        }

        CommonProxy.logger.debug("Collected honey jar from cauldron, remaining level: {}", honeyLevel);
    }

    /** 将蜂蜜块放入炼药锅融化 */
    private void handleMeltHoneyInCauldron(World world, int x, int y, int z, EntityPlayer player, ItemStack honeyBlockStack) {
        Block block = world.getBlock(x, y, z);
        BlockHoneyCauldron honeyCauldron = CommonProxy.honeyCauldronBlock;

        // 消耗一个蜂蜜块
        if (!player.capabilities.isCreativeMode) {
            honeyBlockStack.stackSize--;
        }

        int currentLevel;
        if (block instanceof BlockHoneyCauldron) {
            // 已经是蜂蜜炼药锅，直接读取元数据
            currentLevel = world.getBlockMetadata(x, y, z);
        } else {
            // 首次添加蜂蜜：替换为蜂蜜炼药锅
            currentLevel = 0;
            world.setBlock(x, y, z, honeyCauldron, 0, 3);
        }

        int newLevel = Math.min(3, currentLevel + 1);
        world.setBlockMetadataWithNotify(x, y, z, newLevel, 3);

        CommonProxy.logger.debug("Melted honey block in cauldron at ({},{},{}), level: {}", x, y, z, newLevel);
    }

    // ==================== 内部类：BlockPos ====================

    public static class BlockPos {
        public final int x;
        public final int y;
        public final int z;

        public BlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockPos blockPos = (BlockPos) o;
            return x == blockPos.x && y == blockPos.y && z == blockPos.z;
        }

        @Override
        public int hashCode() {
            return (x * 31 + y) * 31 + z;
        }
    }
}
