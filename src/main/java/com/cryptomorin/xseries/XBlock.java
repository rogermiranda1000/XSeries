/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.cryptomorin.xseries;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>XBlock</b> - MaterialData/BlockData Support<br>
 * BlockState (Old): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/BlockState.html
 * BlockData (New): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/BlockData.html
 * MaterialData (Old): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/material/MaterialData.html
 * <p>
 * All the parameters are non-null except the ones marked as nullable.
 * This class doesn't and shouldn't support materials that are {@link Material#isLegacy()}.
 *
 * @author Crypto Morin
 * @version 2.2.1
 * @see Block
 * @see BlockState
 * @see MaterialData
 * @see XMaterial
 */
@SuppressWarnings("deprecation")
public final class XBlock {
    /**
     * This list contains both block and item version of the same material.
     */
    public static final Set<XMaterial> CROPS = Collections.unmodifiableSet(EnumSet.of(
            XMaterial.CARROT, XMaterial.CARROTS, XMaterial.POTATO, XMaterial.POTATOES,
            XMaterial.NETHER_WART, XMaterial.PUMPKIN_SEEDS, XMaterial.WHEAT_SEEDS, XMaterial.WHEAT,
            XMaterial.MELON_SEEDS, XMaterial.BEETROOT_SEEDS, XMaterial.BEETROOTS, XMaterial.SUGAR_CANE,
            XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO, XMaterial.CHORUS_PLANT,
            XMaterial.KELP, XMaterial.KELP_PLANT, XMaterial.SEA_PICKLE, XMaterial.BROWN_MUSHROOM, XMaterial.RED_MUSHROOM,
            XMaterial.MELON_STEM, XMaterial.PUMPKIN_STEM

    ));
    public static final Set<XMaterial> DANGEROUS = Collections.unmodifiableSet(EnumSet.of(
            XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE, XMaterial.SOUL_FIRE
    ));
    public static final byte CAKE_SLICES = 6;
    private static final boolean ISFLAT = XMaterial.supports(13);
    private static final Map<XMaterial, XMaterial> ITEM_TO_BLOCK = new EnumMap<>(XMaterial.class);
    private static final Map<PreFlatteningMaterial,String> LEGACY_TO_BLOCKDATA = new HashMap<>();
    private static final Map<String,PreFlatteningMaterial> BLOCKDATA_TO_LEGACY = new HashMap<>();
    @Nullable
    private static Method setBlockTypePreFlatteningMethod;

    static {
        ITEM_TO_BLOCK.put(XMaterial.MELON_SLICE, XMaterial.MELON_STEM);
        ITEM_TO_BLOCK.put(XMaterial.MELON_SEEDS, XMaterial.MELON_STEM);

        ITEM_TO_BLOCK.put(XMaterial.CARROT_ON_A_STICK, XMaterial.CARROTS);
        ITEM_TO_BLOCK.put(XMaterial.GOLDEN_CARROT, XMaterial.CARROTS);
        ITEM_TO_BLOCK.put(XMaterial.CARROT, XMaterial.CARROTS);

        ITEM_TO_BLOCK.put(XMaterial.POTATO, XMaterial.POTATOES);
        ITEM_TO_BLOCK.put(XMaterial.BAKED_POTATO, XMaterial.POTATOES);
        ITEM_TO_BLOCK.put(XMaterial.POISONOUS_POTATO, XMaterial.POTATOES);

        ITEM_TO_BLOCK.put(XMaterial.PUMPKIN_SEEDS, XMaterial.PUMPKIN_STEM);
        ITEM_TO_BLOCK.put(XMaterial.PUMPKIN_PIE, XMaterial.PUMPKIN);

        if (!ISFLAT) {
            try {
                setBlockTypePreFlatteningMethod = Block.class.getMethod("setTypeIdAndData", int.class, byte.class, boolean.class);
            } catch (NoSuchMethodException ignore) { }
        }

        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(2,(byte)0),"minecraft:grass_block[snowy=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(3,(byte)2),"minecraft:podzol[snowy=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)0),"minecraft:oak_sapling[stage=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)1),"minecraft:spruce_sapling[stage=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)2),"minecraft:birch_sapling[stage=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)3),"minecraft:jungle_sapling[stage=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)4),"minecraft:acacia_sapling[stage=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)5),"minecraft:dark_oak_sapling[stage=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)8),"minecraft:oak_sapling[stage=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)9),"minecraft:spruce_sapling[stage=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)10),"minecraft:birch_sapling[stage=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)11),"minecraft:jungle_sapling[stage=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)12),"minecraft:acacia_sapling[stage=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(6,(byte)13),"minecraft:dark_oak_sapling[stage=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)0),"minecraft:water[level=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)1),"minecraft:water[level=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)2),"minecraft:water[level=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)3),"minecraft:water[level=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)4),"minecraft:water[level=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)5),"minecraft:water[level=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)6),"minecraft:water[level=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)7),"minecraft:water[level=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)8),"minecraft:water[level=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)9),"minecraft:water[level=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)10),"minecraft:water[level=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)11),"minecraft:water[level=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)12),"minecraft:water[level=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)13),"minecraft:water[level=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)14),"minecraft:water[level=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(8,(byte)15),"minecraft:water[level=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)0),"minecraft:water[level=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)1),"minecraft:water[level=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)2),"minecraft:water[level=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)3),"minecraft:water[level=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)4),"minecraft:water[level=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)5),"minecraft:water[level=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)6),"minecraft:water[level=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)7),"minecraft:water[level=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)8),"minecraft:water[level=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)9),"minecraft:water[level=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)10),"minecraft:water[level=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)11),"minecraft:water[level=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)12),"minecraft:water[level=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)13),"minecraft:water[level=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)14),"minecraft:water[level=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(9,(byte)15),"minecraft:water[level=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)0),"minecraft:lava[level=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)1),"minecraft:lava[level=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)2),"minecraft:lava[level=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)3),"minecraft:lava[level=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)4),"minecraft:lava[level=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)5),"minecraft:lava[level=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)6),"minecraft:lava[level=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)7),"minecraft:lava[level=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)8),"minecraft:lava[level=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)9),"minecraft:lava[level=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)10),"minecraft:lava[level=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)11),"minecraft:lava[level=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)12),"minecraft:lava[level=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)13),"minecraft:lava[level=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)14),"minecraft:lava[level=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(10,(byte)15),"minecraft:lava[level=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)0),"minecraft:lava[level=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)1),"minecraft:lava[level=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)2),"minecraft:lava[level=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)3),"minecraft:lava[level=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)4),"minecraft:lava[level=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)5),"minecraft:lava[level=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)6),"minecraft:lava[level=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)7),"minecraft:lava[level=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)8),"minecraft:lava[level=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)9),"minecraft:lava[level=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)10),"minecraft:lava[level=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)11),"minecraft:lava[level=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)12),"minecraft:lava[level=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)13),"minecraft:lava[level=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)14),"minecraft:lava[level=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(11,(byte)15),"minecraft:lava[level=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)0),"minecraft:oak_log[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)1),"minecraft:spruce_log[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)2),"minecraft:birch_log[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)3),"minecraft:jungle_log[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)4),"minecraft:oak_log[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)5),"minecraft:spruce_log[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)6),"minecraft:birch_log[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)7),"minecraft:jungle_log[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)8),"minecraft:oak_log[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)9),"minecraft:spruce_log[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)10),"minecraft:birch_log[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(17,(byte)11),"minecraft:jungle_log[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)0),"minecraft:oak_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)1),"minecraft:spruce_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)2),"minecraft:birch_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)3),"minecraft:jungle_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)4),"minecraft:oak_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)5),"minecraft:spruce_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)6),"minecraft:birch_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)7),"minecraft:jungle_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)8),"minecraft:oak_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)9),"minecraft:spruce_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)10),"minecraft:birch_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)11),"minecraft:jungle_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)12),"minecraft:oak_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)13),"minecraft:spruce_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)14),"minecraft:birch_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(18,(byte)15),"minecraft:jungle_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)0),"minecraft:dispenser[triggered=false,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)1),"minecraft:dispenser[triggered=false,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)2),"minecraft:dispenser[triggered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)3),"minecraft:dispenser[triggered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)4),"minecraft:dispenser[triggered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)5),"minecraft:dispenser[triggered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)8),"minecraft:dispenser[triggered=true,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)9),"minecraft:dispenser[triggered=true,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)10),"minecraft:dispenser[triggered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)11),"minecraft:dispenser[triggered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)12),"minecraft:dispenser[triggered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(23,(byte)13),"minecraft:dispenser[triggered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)0),"minecraft:red_bed[part=foot,facing=south,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)1),"minecraft:red_bed[part=foot,facing=west,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)2),"minecraft:red_bed[part=foot,facing=north,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)3),"minecraft:red_bed[part=foot,facing=east,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)4),"minecraft:red_bed[part=foot,facing=south,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)5),"minecraft:red_bed[part=foot,facing=west,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)6),"minecraft:red_bed[part=foot,facing=north,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)7),"minecraft:red_bed[part=foot,facing=east,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)8),"minecraft:red_bed[part=head,facing=south,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)9),"minecraft:red_bed[part=head,facing=west,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)10),"minecraft:red_bed[part=head,facing=north,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)11),"minecraft:red_bed[part=head,facing=east,occupied=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)12),"minecraft:red_bed[part=head,facing=south,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)13),"minecraft:red_bed[part=head,facing=west,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)14),"minecraft:red_bed[part=head,facing=north,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(26,(byte)15),"minecraft:red_bed[part=head,facing=east,occupied=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)0),"minecraft:powered_rail[shape=north_south,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)1),"minecraft:powered_rail[shape=east_west,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)2),"minecraft:powered_rail[shape=ascending_east,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)3),"minecraft:powered_rail[shape=ascending_west,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)4),"minecraft:powered_rail[shape=ascending_north,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)5),"minecraft:powered_rail[shape=ascending_south,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)8),"minecraft:powered_rail[shape=north_south,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)9),"minecraft:powered_rail[shape=east_west,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)10),"minecraft:powered_rail[shape=ascending_east,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)11),"minecraft:powered_rail[shape=ascending_west,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)12),"minecraft:powered_rail[shape=ascending_north,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(27,(byte)13),"minecraft:powered_rail[shape=ascending_south,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)0),"minecraft:detector_rail[shape=north_south,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)1),"minecraft:detector_rail[shape=east_west,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)2),"minecraft:detector_rail[shape=ascending_east,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)3),"minecraft:detector_rail[shape=ascending_west,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)4),"minecraft:detector_rail[shape=ascending_north,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)5),"minecraft:detector_rail[shape=ascending_south,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)8),"minecraft:detector_rail[shape=north_south,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)9),"minecraft:detector_rail[shape=east_west,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)10),"minecraft:detector_rail[shape=ascending_east,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)11),"minecraft:detector_rail[shape=ascending_west,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)12),"minecraft:detector_rail[shape=ascending_north,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(28,(byte)13),"minecraft:detector_rail[shape=ascending_south,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)0),"minecraft:sticky_piston[facing=down,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)1),"minecraft:sticky_piston[facing=up,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)2),"minecraft:sticky_piston[facing=north,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)3),"minecraft:sticky_piston[facing=south,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)4),"minecraft:sticky_piston[facing=west,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)5),"minecraft:sticky_piston[facing=east,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)8),"minecraft:sticky_piston[facing=down,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)9),"minecraft:sticky_piston[facing=up,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)10),"minecraft:sticky_piston[facing=north,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)11),"minecraft:sticky_piston[facing=south,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)12),"minecraft:sticky_piston[facing=west,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(29,(byte)13),"minecraft:sticky_piston[facing=east,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)0),"minecraft:piston[facing=down,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)1),"minecraft:piston[facing=up,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)2),"minecraft:piston[facing=north,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)3),"minecraft:piston[facing=south,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)4),"minecraft:piston[facing=west,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)5),"minecraft:piston[facing=east,extended=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)8),"minecraft:piston[facing=down,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)9),"minecraft:piston[facing=up,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)10),"minecraft:piston[facing=north,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)11),"minecraft:piston[facing=south,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)12),"minecraft:piston[facing=west,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(33,(byte)13),"minecraft:piston[facing=east,extended=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)0),"minecraft:piston_head[short=false,facing=down,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)1),"minecraft:piston_head[short=false,facing=up,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)2),"minecraft:piston_head[short=false,facing=north,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)3),"minecraft:piston_head[short=false,facing=south,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)4),"minecraft:piston_head[short=false,facing=west,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)5),"minecraft:piston_head[short=false,facing=east,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)8),"minecraft:piston_head[short=false,facing=down,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)9),"minecraft:piston_head[short=false,facing=up,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)10),"minecraft:piston_head[short=false,facing=north,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)11),"minecraft:piston_head[short=false,facing=south,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)12),"minecraft:piston_head[short=false,facing=west,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(34,(byte)13),"minecraft:piston_head[short=false,facing=east,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)0),"minecraft:moving_piston[facing=down,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)1),"minecraft:moving_piston[facing=up,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)2),"minecraft:moving_piston[facing=north,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)3),"minecraft:moving_piston[facing=south,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)4),"minecraft:moving_piston[facing=west,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)5),"minecraft:moving_piston[facing=east,type=normal]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)8),"minecraft:moving_piston[facing=down,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)9),"minecraft:moving_piston[facing=up,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)10),"minecraft:moving_piston[facing=north,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)11),"minecraft:moving_piston[facing=south,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)12),"minecraft:moving_piston[facing=west,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(36,(byte)13),"minecraft:moving_piston[facing=east,type=sticky]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)0),"minecraft:stone_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)1),"minecraft:sandstone_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)2),"minecraft:petrified_oak_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)3),"minecraft:cobblestone_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)4),"minecraft:brick_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)5),"minecraft:stone_brick_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)6),"minecraft:nether_brick_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)7),"minecraft:quartz_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)10),"minecraft:petrified_oak_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)11),"minecraft:cobblestone_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)12),"minecraft:brick_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)13),"minecraft:stone_brick_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(43,(byte)14),"minecraft:nether_brick_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)0),"minecraft:stone_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)1),"minecraft:sandstone_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)2),"minecraft:petrified_oak_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)3),"minecraft:cobblestone_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)4),"minecraft:brick_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)5),"minecraft:stone_brick_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)6),"minecraft:nether_brick_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)7),"minecraft:quartz_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)8),"minecraft:stone_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)9),"minecraft:sandstone_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)10),"minecraft:petrified_oak_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)11),"minecraft:cobblestone_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)12),"minecraft:brick_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)13),"minecraft:stone_brick_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)14),"minecraft:nether_brick_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(44,(byte)15),"minecraft:quartz_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(46,(byte)0),"minecraft:tnt[unstable=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(46,(byte)1),"minecraft:tnt[unstable=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(50,(byte)1),"minecraft:wall_torch[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(50,(byte)2),"minecraft:wall_torch[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(50,(byte)3),"minecraft:wall_torch[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(50,(byte)4),"minecraft:wall_torch[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)0),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)1),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)2),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)3),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)4),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)5),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)6),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)7),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)8),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)9),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)10),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)11),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)12),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)13),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)14),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(51,(byte)15),"minecraft:fire[east=false,south=false,north=false,west=false,up=false,age=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)0),"minecraft:oak_stairs[half=bottom,shape=outer_right,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)1),"minecraft:oak_stairs[half=bottom,shape=outer_right,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)2),"minecraft:oak_stairs[half=bottom,shape=outer_right,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)3),"minecraft:oak_stairs[half=bottom,shape=outer_right,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)4),"minecraft:oak_stairs[half=top,shape=outer_right,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)5),"minecraft:oak_stairs[half=top,shape=outer_right,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)6),"minecraft:oak_stairs[half=top,shape=outer_right,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(53,(byte)7),"minecraft:oak_stairs[half=top,shape=outer_right,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(54,(byte)2),"minecraft:chest[facing=north,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(54,(byte)3),"minecraft:chest[facing=south,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(54,(byte)4),"minecraft:chest[facing=west,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(54,(byte)5),"minecraft:chest[facing=east,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)0),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)1),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)2),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)3),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)4),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)5),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)6),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)7),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)8),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)9),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)10),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)11),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)12),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)13),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)14),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(55,(byte)15),"minecraft:redstone_wire[east=none,south=none,north=none,west=none,power=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)0),"minecraft:wheat[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)1),"minecraft:wheat[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)2),"minecraft:wheat[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)3),"minecraft:wheat[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)4),"minecraft:wheat[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)5),"minecraft:wheat[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)6),"minecraft:wheat[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(59,(byte)7),"minecraft:wheat[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)0),"minecraft:farmland[moisture=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)1),"minecraft:farmland[moisture=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)2),"minecraft:farmland[moisture=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)3),"minecraft:farmland[moisture=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)4),"minecraft:farmland[moisture=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)5),"minecraft:farmland[moisture=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)6),"minecraft:farmland[moisture=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(60,(byte)7),"minecraft:farmland[moisture=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(61,(byte)2),"minecraft:furnace[facing=north,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(61,(byte)3),"minecraft:furnace[facing=south,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(61,(byte)4),"minecraft:furnace[facing=west,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(61,(byte)5),"minecraft:furnace[facing=east,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(62,(byte)2),"minecraft:furnace[facing=north,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(62,(byte)3),"minecraft:furnace[facing=south,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(62,(byte)4),"minecraft:furnace[facing=west,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(62,(byte)5),"minecraft:furnace[facing=east,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)0),"minecraft:sign[rotation=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)1),"minecraft:sign[rotation=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)2),"minecraft:sign[rotation=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)3),"minecraft:sign[rotation=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)4),"minecraft:sign[rotation=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)5),"minecraft:sign[rotation=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)6),"minecraft:sign[rotation=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)7),"minecraft:sign[rotation=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)8),"minecraft:sign[rotation=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)9),"minecraft:sign[rotation=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)10),"minecraft:sign[rotation=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)11),"minecraft:sign[rotation=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)12),"minecraft:sign[rotation=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)13),"minecraft:sign[rotation=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)14),"minecraft:sign[rotation=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(63,(byte)15),"minecraft:sign[rotation=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)0),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)1),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)2),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)3),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)4),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)5),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)6),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)7),"minecraft:oak_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)8),"minecraft:oak_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)9),"minecraft:oak_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)10),"minecraft:oak_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(64,(byte)11),"minecraft:oak_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(65,(byte)2),"minecraft:ladder[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(65,(byte)3),"minecraft:ladder[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(65,(byte)4),"minecraft:ladder[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(65,(byte)5),"minecraft:ladder[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)0),"minecraft:rail[shape=north_south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)1),"minecraft:rail[shape=east_west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)2),"minecraft:rail[shape=ascending_east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)3),"minecraft:rail[shape=ascending_west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)4),"minecraft:rail[shape=ascending_north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)5),"minecraft:rail[shape=ascending_south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)6),"minecraft:rail[shape=south_east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)7),"minecraft:rail[shape=south_west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)8),"minecraft:rail[shape=north_west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(66,(byte)9),"minecraft:rail[shape=north_east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)0),"minecraft:cobblestone_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)1),"minecraft:cobblestone_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)2),"minecraft:cobblestone_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)3),"minecraft:cobblestone_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)4),"minecraft:cobblestone_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)5),"minecraft:cobblestone_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)6),"minecraft:cobblestone_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(67,(byte)7),"minecraft:cobblestone_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(68,(byte)2),"minecraft:wall_sign[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(68,(byte)3),"minecraft:wall_sign[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(68,(byte)4),"minecraft:wall_sign[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(68,(byte)5),"minecraft:wall_sign[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)0),"minecraft:lever[powered=false,facing=north,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)1),"minecraft:lever[powered=false,facing=east,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)2),"minecraft:lever[powered=false,facing=west,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)3),"minecraft:lever[powered=false,facing=south,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)4),"minecraft:lever[powered=false,facing=north,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)5),"minecraft:lever[powered=false,facing=east,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)6),"minecraft:lever[powered=false,facing=north,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)7),"minecraft:lever[powered=false,facing=east,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)8),"minecraft:lever[powered=true,facing=north,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)9),"minecraft:lever[powered=true,facing=east,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)10),"minecraft:lever[powered=true,facing=west,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)11),"minecraft:lever[powered=true,facing=south,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)12),"minecraft:lever[powered=true,facing=north,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)13),"minecraft:lever[powered=true,facing=east,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)14),"minecraft:lever[powered=true,facing=north,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(69,(byte)15),"minecraft:lever[powered=true,facing=east,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(70,(byte)0),"minecraft:stone_pressure_plate[powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(70,(byte)1),"minecraft:stone_pressure_plate[powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)0),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)1),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)2),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)3),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)4),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)5),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)6),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)7),"minecraft:iron_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)8),"minecraft:iron_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)9),"minecraft:iron_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)10),"minecraft:iron_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(71,(byte)11),"minecraft:iron_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(72,(byte)0),"minecraft:oak_pressure_plate[powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(72,(byte)1),"minecraft:oak_pressure_plate[powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(73,(byte)0),"minecraft:redstone_ore[lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(74,(byte)0),"minecraft:redstone_ore[lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(75,(byte)1),"minecraft:redstone_wall_torch[facing=east,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(75,(byte)2),"minecraft:redstone_wall_torch[facing=west,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(75,(byte)3),"minecraft:redstone_wall_torch[facing=south,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(75,(byte)4),"minecraft:redstone_wall_torch[facing=north,lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(75,(byte)5),"minecraft:redstone_torch[lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(76,(byte)1),"minecraft:redstone_wall_torch[facing=east,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(76,(byte)2),"minecraft:redstone_wall_torch[facing=west,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(76,(byte)3),"minecraft:redstone_wall_torch[facing=south,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(76,(byte)4),"minecraft:redstone_wall_torch[facing=north,lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(76,(byte)5),"minecraft:redstone_torch[lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)0),"minecraft:stone_button[powered=false,facing=east,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)1),"minecraft:stone_button[powered=false,facing=east,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)2),"minecraft:stone_button[powered=false,facing=west,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)3),"minecraft:stone_button[powered=false,facing=south,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)4),"minecraft:stone_button[powered=false,facing=north,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)5),"minecraft:stone_button[powered=false,facing=east,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)8),"minecraft:stone_button[powered=true,facing=south,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)9),"minecraft:stone_button[powered=true,facing=east,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)10),"minecraft:stone_button[powered=true,facing=west,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)11),"minecraft:stone_button[powered=true,facing=south,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)12),"minecraft:stone_button[powered=true,facing=north,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(77,(byte)13),"minecraft:stone_button[powered=true,facing=south,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)0),"minecraft:snow[layers=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)1),"minecraft:snow[layers=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)2),"minecraft:snow[layers=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)3),"minecraft:snow[layers=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)4),"minecraft:snow[layers=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)5),"minecraft:snow[layers=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)6),"minecraft:snow[layers=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(78,(byte)7),"minecraft:snow[layers=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)0),"minecraft:cactus[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)1),"minecraft:cactus[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)2),"minecraft:cactus[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)3),"minecraft:cactus[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)4),"minecraft:cactus[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)5),"minecraft:cactus[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)6),"minecraft:cactus[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)7),"minecraft:cactus[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)8),"minecraft:cactus[age=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)9),"minecraft:cactus[age=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)10),"minecraft:cactus[age=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)11),"minecraft:cactus[age=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)12),"minecraft:cactus[age=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)13),"minecraft:cactus[age=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)14),"minecraft:cactus[age=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(81,(byte)15),"minecraft:cactus[age=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)0),"minecraft:sugar_cane[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)1),"minecraft:sugar_cane[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)2),"minecraft:sugar_cane[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)3),"minecraft:sugar_cane[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)4),"minecraft:sugar_cane[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)5),"minecraft:sugar_cane[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)6),"minecraft:sugar_cane[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)7),"minecraft:sugar_cane[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)8),"minecraft:sugar_cane[age=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)9),"minecraft:sugar_cane[age=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)10),"minecraft:sugar_cane[age=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)11),"minecraft:sugar_cane[age=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)12),"minecraft:sugar_cane[age=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)13),"minecraft:sugar_cane[age=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)14),"minecraft:sugar_cane[age=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(83,(byte)15),"minecraft:sugar_cane[age=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(84,(byte)0),"minecraft:jukebox[has_record=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(84,(byte)1),"minecraft:jukebox[has_record=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(85,(byte)0),"minecraft:oak_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(86,(byte)0),"minecraft:carved_pumpkin[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(86,(byte)1),"minecraft:carved_pumpkin[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(86,(byte)2),"minecraft:carved_pumpkin[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(86,(byte)3),"minecraft:carved_pumpkin[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(90,(byte)1),"minecraft:nether_portal[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(90,(byte)2),"minecraft:nether_portal[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(91,(byte)0),"minecraft:jack_o_lantern[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(91,(byte)1),"minecraft:jack_o_lantern[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(91,(byte)2),"minecraft:jack_o_lantern[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(91,(byte)3),"minecraft:jack_o_lantern[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)0),"minecraft:cake[bites=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)1),"minecraft:cake[bites=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)2),"minecraft:cake[bites=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)3),"minecraft:cake[bites=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)4),"minecraft:cake[bites=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)5),"minecraft:cake[bites=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(92,(byte)6),"minecraft:cake[bites=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)0),"minecraft:repeater[delay=1,facing=south,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)1),"minecraft:repeater[delay=1,facing=west,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)2),"minecraft:repeater[delay=1,facing=north,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)3),"minecraft:repeater[delay=1,facing=east,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)4),"minecraft:repeater[delay=2,facing=south,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)5),"minecraft:repeater[delay=2,facing=west,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)6),"minecraft:repeater[delay=2,facing=north,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)7),"minecraft:repeater[delay=2,facing=east,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)8),"minecraft:repeater[delay=3,facing=south,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)9),"minecraft:repeater[delay=3,facing=west,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)10),"minecraft:repeater[delay=3,facing=north,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)11),"minecraft:repeater[delay=3,facing=east,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)12),"minecraft:repeater[delay=4,facing=south,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)13),"minecraft:repeater[delay=4,facing=west,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)14),"minecraft:repeater[delay=4,facing=north,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(93,(byte)15),"minecraft:repeater[delay=4,facing=east,locked=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)0),"minecraft:repeater[delay=1,facing=south,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)1),"minecraft:repeater[delay=1,facing=west,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)2),"minecraft:repeater[delay=1,facing=north,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)3),"minecraft:repeater[delay=1,facing=east,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)4),"minecraft:repeater[delay=2,facing=south,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)5),"minecraft:repeater[delay=2,facing=west,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)6),"minecraft:repeater[delay=2,facing=north,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)7),"minecraft:repeater[delay=2,facing=east,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)8),"minecraft:repeater[delay=3,facing=south,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)9),"minecraft:repeater[delay=3,facing=west,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)10),"minecraft:repeater[delay=3,facing=north,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)11),"minecraft:repeater[delay=3,facing=east,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)12),"minecraft:repeater[delay=4,facing=south,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)13),"minecraft:repeater[delay=4,facing=west,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)14),"minecraft:repeater[delay=4,facing=north,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(94,(byte)15),"minecraft:repeater[delay=4,facing=east,locked=false,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)0),"minecraft:oak_trapdoor[half=bottom,facing=north,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)1),"minecraft:oak_trapdoor[half=bottom,facing=south,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)2),"minecraft:oak_trapdoor[half=bottom,facing=west,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)3),"minecraft:oak_trapdoor[half=bottom,facing=east,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)4),"minecraft:oak_trapdoor[half=bottom,facing=north,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)5),"minecraft:oak_trapdoor[half=bottom,facing=south,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)6),"minecraft:oak_trapdoor[half=bottom,facing=west,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)7),"minecraft:oak_trapdoor[half=bottom,facing=east,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)8),"minecraft:oak_trapdoor[half=top,facing=north,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)9),"minecraft:oak_trapdoor[half=top,facing=south,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)10),"minecraft:oak_trapdoor[half=top,facing=west,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)11),"minecraft:oak_trapdoor[half=top,facing=east,open=false,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)12),"minecraft:oak_trapdoor[half=top,facing=north,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)13),"minecraft:oak_trapdoor[half=top,facing=south,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)14),"minecraft:oak_trapdoor[half=top,facing=west,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(96,(byte)15),"minecraft:oak_trapdoor[half=top,facing=east,open=true,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)0),"minecraft:brown_mushroom_block[north=false,east=false,south=false,west=false,up=false,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)1),"minecraft:brown_mushroom_block[north=true,east=false,south=false,west=true,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)2),"minecraft:brown_mushroom_block[north=true,east=false,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)3),"minecraft:brown_mushroom_block[north=true,east=true,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)4),"minecraft:brown_mushroom_block[north=false,east=false,south=false,west=true,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)5),"minecraft:brown_mushroom_block[north=false,east=false,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)6),"minecraft:brown_mushroom_block[north=false,east=true,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)7),"minecraft:brown_mushroom_block[north=false,east=false,south=true,west=true,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)8),"minecraft:brown_mushroom_block[north=false,east=false,south=true,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)9),"minecraft:brown_mushroom_block[north=false,east=true,south=true,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)10),"minecraft:mushroom_stem[north=true,east=true,south=true,west=true,up=false,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)14),"minecraft:brown_mushroom_block[north=true,east=true,south=true,west=true,up=true,down=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(99,(byte)15),"minecraft:mushroom_stem[north=true,east=true,south=true,west=true,up=true,down=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)0),"minecraft:red_mushroom_block[north=false,east=false,south=false,west=false,up=false,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)1),"minecraft:red_mushroom_block[north=true,east=false,south=false,west=true,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)2),"minecraft:red_mushroom_block[north=true,east=false,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)3),"minecraft:red_mushroom_block[north=true,east=true,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)4),"minecraft:red_mushroom_block[north=false,east=false,south=false,west=true,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)5),"minecraft:red_mushroom_block[north=false,east=false,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)6),"minecraft:red_mushroom_block[north=false,east=true,south=false,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)7),"minecraft:red_mushroom_block[north=false,east=false,south=true,west=true,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)8),"minecraft:red_mushroom_block[north=false,east=false,south=true,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)9),"minecraft:red_mushroom_block[north=false,east=true,south=true,west=false,up=true,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)10),"minecraft:mushroom_stem[north=true,east=true,south=true,west=true,up=false,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)14),"minecraft:red_mushroom_block[north=true,east=true,south=true,west=true,up=true,down=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(100,(byte)15),"minecraft:mushroom_stem[north=true,east=true,south=true,west=true,up=true,down=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(101,(byte)0),"minecraft:iron_bars[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(102,(byte)0),"minecraft:glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)0),"minecraft:pumpkin_stem[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)1),"minecraft:pumpkin_stem[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)2),"minecraft:pumpkin_stem[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)3),"minecraft:pumpkin_stem[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)4),"minecraft:pumpkin_stem[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)5),"minecraft:pumpkin_stem[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)6),"minecraft:pumpkin_stem[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(104,(byte)7),"minecraft:pumpkin_stem[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)0),"minecraft:melon_stem[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)1),"minecraft:melon_stem[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)2),"minecraft:melon_stem[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)3),"minecraft:melon_stem[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)4),"minecraft:melon_stem[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)5),"minecraft:melon_stem[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)6),"minecraft:melon_stem[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(105,(byte)7),"minecraft:melon_stem[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)0),"minecraft:vine[east=false,south=false,north=false,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)1),"minecraft:vine[east=false,south=true,north=false,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)2),"minecraft:vine[east=false,south=false,north=false,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)3),"minecraft:vine[east=false,south=true,north=false,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)4),"minecraft:vine[east=false,south=false,north=true,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)5),"minecraft:vine[east=false,south=true,north=true,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)6),"minecraft:vine[east=false,south=false,north=true,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)7),"minecraft:vine[east=false,south=true,north=true,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)8),"minecraft:vine[east=true,south=false,north=false,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)9),"minecraft:vine[east=true,south=true,north=false,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)10),"minecraft:vine[east=true,south=false,north=false,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)11),"minecraft:vine[east=true,south=true,north=false,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)12),"minecraft:vine[east=true,south=false,north=true,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)13),"minecraft:vine[east=true,south=true,north=true,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)14),"minecraft:vine[east=true,south=false,north=true,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(106,(byte)15),"minecraft:vine[east=true,south=true,north=true,west=true,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)0),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)1),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)2),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)3),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)4),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)5),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)6),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)7),"minecraft:oak_fence_gate[in_wall=false,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)8),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)9),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)10),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)11),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)12),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)13),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)14),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(107,(byte)15),"minecraft:oak_fence_gate[in_wall=false,powered=true,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)0),"minecraft:brick_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)1),"minecraft:brick_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)2),"minecraft:brick_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)3),"minecraft:brick_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)4),"minecraft:brick_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)5),"minecraft:brick_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)6),"minecraft:brick_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(108,(byte)7),"minecraft:brick_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)0),"minecraft:stone_brick_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)1),"minecraft:stone_brick_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)2),"minecraft:stone_brick_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)3),"minecraft:stone_brick_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)4),"minecraft:stone_brick_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)5),"minecraft:stone_brick_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)6),"minecraft:stone_brick_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(109,(byte)7),"minecraft:stone_brick_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(110,(byte)0),"minecraft:mycelium[snowy=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(113,(byte)0),"minecraft:nether_brick_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)0),"minecraft:nether_brick_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)1),"minecraft:nether_brick_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)2),"minecraft:nether_brick_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)3),"minecraft:nether_brick_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)4),"minecraft:nether_brick_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)5),"minecraft:nether_brick_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)6),"minecraft:nether_brick_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(114,(byte)7),"minecraft:nether_brick_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(115,(byte)0),"minecraft:nether_wart[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(115,(byte)1),"minecraft:nether_wart[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(115,(byte)2),"minecraft:nether_wart[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(115,(byte)3),"minecraft:nether_wart[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)0),"minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=false,has_bottle_2=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)1),"minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=false,has_bottle_2=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)2),"minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=true,has_bottle_2=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)3),"minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=true,has_bottle_2=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)4),"minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=false,has_bottle_2=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)5),"minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=false,has_bottle_2=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)6),"minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=true,has_bottle_2=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(117,(byte)7),"minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=true,has_bottle_2=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(118,(byte)0),"minecraft:cauldron[level=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(118,(byte)1),"minecraft:cauldron[level=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(118,(byte)2),"minecraft:cauldron[level=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(118,(byte)3),"minecraft:cauldron[level=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)0),"minecraft:end_portal_frame[eye=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)1),"minecraft:end_portal_frame[eye=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)2),"minecraft:end_portal_frame[eye=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)3),"minecraft:end_portal_frame[eye=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)4),"minecraft:end_portal_frame[eye=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)5),"minecraft:end_portal_frame[eye=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)6),"minecraft:end_portal_frame[eye=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(120,(byte)7),"minecraft:end_portal_frame[eye=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(123,(byte)0),"minecraft:redstone_lamp[lit=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(124,(byte)0),"minecraft:redstone_lamp[lit=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(125,(byte)0),"minecraft:oak_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(125,(byte)1),"minecraft:spruce_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(125,(byte)2),"minecraft:birch_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(125,(byte)3),"minecraft:jungle_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(125,(byte)4),"minecraft:acacia_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(125,(byte)5),"minecraft:dark_oak_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)0),"minecraft:oak_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)1),"minecraft:spruce_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)2),"minecraft:birch_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)3),"minecraft:jungle_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)4),"minecraft:acacia_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)5),"minecraft:dark_oak_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)8),"minecraft:oak_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)9),"minecraft:spruce_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)10),"minecraft:birch_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)11),"minecraft:jungle_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)12),"minecraft:acacia_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(126,(byte)13),"minecraft:dark_oak_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)0),"minecraft:cocoa[facing=south,age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)1),"minecraft:cocoa[facing=west,age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)2),"minecraft:cocoa[facing=north,age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)3),"minecraft:cocoa[facing=east,age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)4),"minecraft:cocoa[facing=south,age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)5),"minecraft:cocoa[facing=west,age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)6),"minecraft:cocoa[facing=north,age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)7),"minecraft:cocoa[facing=east,age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)8),"minecraft:cocoa[facing=south,age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)9),"minecraft:cocoa[facing=west,age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)10),"minecraft:cocoa[facing=north,age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(127,(byte)11),"minecraft:cocoa[facing=east,age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)0),"minecraft:sandstone_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)1),"minecraft:sandstone_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)2),"minecraft:sandstone_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)3),"minecraft:sandstone_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)4),"minecraft:sandstone_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)5),"minecraft:sandstone_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)6),"minecraft:sandstone_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(128,(byte)7),"minecraft:sandstone_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(130,(byte)2),"minecraft:ender_chest[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(130,(byte)3),"minecraft:ender_chest[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(130,(byte)4),"minecraft:ender_chest[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(130,(byte)5),"minecraft:ender_chest[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)0),"minecraft:tripwire_hook[powered=false,attached=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)1),"minecraft:tripwire_hook[powered=false,attached=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)2),"minecraft:tripwire_hook[powered=false,attached=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)3),"minecraft:tripwire_hook[powered=false,attached=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)4),"minecraft:tripwire_hook[powered=false,attached=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)5),"minecraft:tripwire_hook[powered=false,attached=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)6),"minecraft:tripwire_hook[powered=false,attached=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)7),"minecraft:tripwire_hook[powered=false,attached=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)8),"minecraft:tripwire_hook[powered=true,attached=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)9),"minecraft:tripwire_hook[powered=true,attached=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)10),"minecraft:tripwire_hook[powered=true,attached=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)11),"minecraft:tripwire_hook[powered=true,attached=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)12),"minecraft:tripwire_hook[powered=true,attached=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)13),"minecraft:tripwire_hook[powered=true,attached=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)14),"minecraft:tripwire_hook[powered=true,attached=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(131,(byte)15),"minecraft:tripwire_hook[powered=true,attached=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)0),"minecraft:tripwire[disarmed=false,east=false,powered=false,south=false,north=false,west=false,attached=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)1),"minecraft:tripwire[disarmed=false,east=false,powered=true,south=false,north=false,west=false,attached=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)4),"minecraft:tripwire[disarmed=false,east=false,powered=false,south=false,north=false,west=false,attached=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)5),"minecraft:tripwire[disarmed=false,east=false,powered=true,south=false,north=false,west=false,attached=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)8),"minecraft:tripwire[disarmed=true,east=false,powered=false,south=false,north=false,west=false,attached=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)9),"minecraft:tripwire[disarmed=true,east=false,powered=true,south=false,north=false,west=false,attached=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)12),"minecraft:tripwire[disarmed=true,east=false,powered=false,south=false,north=false,west=false,attached=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(132,(byte)13),"minecraft:tripwire[disarmed=true,east=false,powered=true,south=false,north=false,west=false,attached=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)0),"minecraft:spruce_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)1),"minecraft:spruce_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)2),"minecraft:spruce_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)3),"minecraft:spruce_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)4),"minecraft:spruce_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)5),"minecraft:spruce_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)6),"minecraft:spruce_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(134,(byte)7),"minecraft:spruce_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)0),"minecraft:birch_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)1),"minecraft:birch_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)2),"minecraft:birch_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)3),"minecraft:birch_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)4),"minecraft:birch_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)5),"minecraft:birch_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)6),"minecraft:birch_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(135,(byte)7),"minecraft:birch_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)0),"minecraft:jungle_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)1),"minecraft:jungle_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)2),"minecraft:jungle_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)3),"minecraft:jungle_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)4),"minecraft:jungle_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)5),"minecraft:jungle_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)6),"minecraft:jungle_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(136,(byte)7),"minecraft:jungle_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)0),"minecraft:command_block[conditional=false,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)1),"minecraft:command_block[conditional=false,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)2),"minecraft:command_block[conditional=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)3),"minecraft:command_block[conditional=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)4),"minecraft:command_block[conditional=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)5),"minecraft:command_block[conditional=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)8),"minecraft:command_block[conditional=true,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)9),"minecraft:command_block[conditional=true,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)10),"minecraft:command_block[conditional=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)11),"minecraft:command_block[conditional=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)12),"minecraft:command_block[conditional=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(137,(byte)13),"minecraft:command_block[conditional=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(139,(byte)0),"minecraft:cobblestone_wall[east=false,south=false,north=false,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(139,(byte)1),"minecraft:mossy_cobblestone_wall[east=false,south=false,north=false,west=false,up=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)0),"minecraft:carrots[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)1),"minecraft:carrots[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)2),"minecraft:carrots[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)3),"minecraft:carrots[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)4),"minecraft:carrots[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)5),"minecraft:carrots[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)6),"minecraft:carrots[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(141,(byte)7),"minecraft:carrots[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)0),"minecraft:potatoes[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)1),"minecraft:potatoes[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)2),"minecraft:potatoes[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)3),"minecraft:potatoes[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)4),"minecraft:potatoes[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)5),"minecraft:potatoes[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)6),"minecraft:potatoes[age=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(142,(byte)7),"minecraft:potatoes[age=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)0),"minecraft:oak_button[powered=false,facing=east,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)1),"minecraft:oak_button[powered=false,facing=east,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)2),"minecraft:oak_button[powered=false,facing=west,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)3),"minecraft:oak_button[powered=false,facing=south,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)4),"minecraft:oak_button[powered=false,facing=north,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)5),"minecraft:oak_button[powered=false,facing=east,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)8),"minecraft:oak_button[powered=true,facing=south,face=ceiling]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)9),"minecraft:oak_button[powered=true,facing=east,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)10),"minecraft:oak_button[powered=true,facing=west,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)11),"minecraft:oak_button[powered=true,facing=south,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)12),"minecraft:oak_button[powered=true,facing=north,face=wall]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(143,(byte)13),"minecraft:oak_button[powered=true,facing=south,face=floor]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)0),"minecraft:skeleton_skull[rotation=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)1),"minecraft:skeleton_skull[rotation=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)2),"minecraft:skeleton_wall_skull[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)3),"minecraft:skeleton_wall_skull[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)4),"minecraft:skeleton_wall_skull[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)5),"minecraft:skeleton_wall_skull[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)8),"minecraft:skeleton_skull[rotation=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)9),"minecraft:skeleton_skull[rotation=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)10),"minecraft:skeleton_wall_skull[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)11),"minecraft:skeleton_wall_skull[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)12),"minecraft:skeleton_wall_skull[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(144,(byte)13),"minecraft:skeleton_wall_skull[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)0),"minecraft:anvil[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)1),"minecraft:anvil[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)2),"minecraft:anvil[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)3),"minecraft:anvil[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)4),"minecraft:chipped_anvil[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)5),"minecraft:chipped_anvil[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)6),"minecraft:chipped_anvil[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)7),"minecraft:chipped_anvil[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)8),"minecraft:damaged_anvil[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)9),"minecraft:damaged_anvil[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)10),"minecraft:damaged_anvil[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(145,(byte)11),"minecraft:damaged_anvil[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(146,(byte)2),"minecraft:trapped_chest[facing=north,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(146,(byte)3),"minecraft:trapped_chest[facing=south,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(146,(byte)4),"minecraft:trapped_chest[facing=west,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(146,(byte)5),"minecraft:trapped_chest[facing=east,type=single]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)0),"minecraft:light_weighted_pressure_plate[power=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)1),"minecraft:light_weighted_pressure_plate[power=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)2),"minecraft:light_weighted_pressure_plate[power=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)3),"minecraft:light_weighted_pressure_plate[power=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)4),"minecraft:light_weighted_pressure_plate[power=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)5),"minecraft:light_weighted_pressure_plate[power=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)6),"minecraft:light_weighted_pressure_plate[power=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)7),"minecraft:light_weighted_pressure_plate[power=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)8),"minecraft:light_weighted_pressure_plate[power=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)9),"minecraft:light_weighted_pressure_plate[power=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)10),"minecraft:light_weighted_pressure_plate[power=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)11),"minecraft:light_weighted_pressure_plate[power=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)12),"minecraft:light_weighted_pressure_plate[power=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)13),"minecraft:light_weighted_pressure_plate[power=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)14),"minecraft:light_weighted_pressure_plate[power=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(147,(byte)15),"minecraft:light_weighted_pressure_plate[power=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)0),"minecraft:heavy_weighted_pressure_plate[power=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)1),"minecraft:heavy_weighted_pressure_plate[power=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)2),"minecraft:heavy_weighted_pressure_plate[power=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)3),"minecraft:heavy_weighted_pressure_plate[power=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)4),"minecraft:heavy_weighted_pressure_plate[power=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)5),"minecraft:heavy_weighted_pressure_plate[power=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)6),"minecraft:heavy_weighted_pressure_plate[power=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)7),"minecraft:heavy_weighted_pressure_plate[power=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)8),"minecraft:heavy_weighted_pressure_plate[power=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)9),"minecraft:heavy_weighted_pressure_plate[power=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)10),"minecraft:heavy_weighted_pressure_plate[power=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)11),"minecraft:heavy_weighted_pressure_plate[power=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)12),"minecraft:heavy_weighted_pressure_plate[power=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)13),"minecraft:heavy_weighted_pressure_plate[power=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)14),"minecraft:heavy_weighted_pressure_plate[power=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(148,(byte)15),"minecraft:heavy_weighted_pressure_plate[power=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)0),"minecraft:comparator[mode=compare,powered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)1),"minecraft:comparator[mode=compare,powered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)2),"minecraft:comparator[mode=compare,powered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)3),"minecraft:comparator[mode=compare,powered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)4),"minecraft:comparator[mode=subtract,powered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)5),"minecraft:comparator[mode=subtract,powered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)6),"minecraft:comparator[mode=subtract,powered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)7),"minecraft:comparator[mode=subtract,powered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)8),"minecraft:comparator[mode=compare,powered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)9),"minecraft:comparator[mode=compare,powered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)10),"minecraft:comparator[mode=compare,powered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)11),"minecraft:comparator[mode=compare,powered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)12),"minecraft:comparator[mode=subtract,powered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)13),"minecraft:comparator[mode=subtract,powered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)14),"minecraft:comparator[mode=subtract,powered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(149,(byte)15),"minecraft:comparator[mode=subtract,powered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)0),"minecraft:comparator[mode=compare,powered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)1),"minecraft:comparator[mode=compare,powered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)2),"minecraft:comparator[mode=compare,powered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)3),"minecraft:comparator[mode=compare,powered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)4),"minecraft:comparator[mode=subtract,powered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)5),"minecraft:comparator[mode=subtract,powered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)6),"minecraft:comparator[mode=subtract,powered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)7),"minecraft:comparator[mode=subtract,powered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)8),"minecraft:comparator[mode=compare,powered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)9),"minecraft:comparator[mode=compare,powered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)10),"minecraft:comparator[mode=compare,powered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)11),"minecraft:comparator[mode=compare,powered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)12),"minecraft:comparator[mode=subtract,powered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)13),"minecraft:comparator[mode=subtract,powered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)14),"minecraft:comparator[mode=subtract,powered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(150,(byte)15),"minecraft:comparator[mode=subtract,powered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)0),"minecraft:daylight_detector[inverted=false,power=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)1),"minecraft:daylight_detector[inverted=false,power=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)2),"minecraft:daylight_detector[inverted=false,power=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)3),"minecraft:daylight_detector[inverted=false,power=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)4),"minecraft:daylight_detector[inverted=false,power=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)5),"minecraft:daylight_detector[inverted=false,power=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)6),"minecraft:daylight_detector[inverted=false,power=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)7),"minecraft:daylight_detector[inverted=false,power=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)8),"minecraft:daylight_detector[inverted=false,power=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)9),"minecraft:daylight_detector[inverted=false,power=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)10),"minecraft:daylight_detector[inverted=false,power=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)11),"minecraft:daylight_detector[inverted=false,power=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)12),"minecraft:daylight_detector[inverted=false,power=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)13),"minecraft:daylight_detector[inverted=false,power=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)14),"minecraft:daylight_detector[inverted=false,power=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(151,(byte)15),"minecraft:daylight_detector[inverted=false,power=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)0),"minecraft:hopper[facing=down,enabled=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)2),"minecraft:hopper[facing=north,enabled=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)3),"minecraft:hopper[facing=south,enabled=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)4),"minecraft:hopper[facing=west,enabled=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)5),"minecraft:hopper[facing=east,enabled=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)8),"minecraft:hopper[facing=down,enabled=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)10),"minecraft:hopper[facing=north,enabled=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)11),"minecraft:hopper[facing=south,enabled=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)12),"minecraft:hopper[facing=west,enabled=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(154,(byte)13),"minecraft:hopper[facing=east,enabled=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(155,(byte)2),"minecraft:quartz_pillar[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(155,(byte)3),"minecraft:quartz_pillar[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(155,(byte)4),"minecraft:quartz_pillar[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(155,(byte)6),"minecraft:quartz_pillar[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(155,(byte)10),"minecraft:quartz_pillar[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)0),"minecraft:quartz_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)1),"minecraft:quartz_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)2),"minecraft:quartz_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)3),"minecraft:quartz_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)4),"minecraft:quartz_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)5),"minecraft:quartz_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)6),"minecraft:quartz_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(156,(byte)7),"minecraft:quartz_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)0),"minecraft:activator_rail[shape=north_south,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)1),"minecraft:activator_rail[shape=east_west,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)2),"minecraft:activator_rail[shape=ascending_east,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)3),"minecraft:activator_rail[shape=ascending_west,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)4),"minecraft:activator_rail[shape=ascending_north,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)5),"minecraft:activator_rail[shape=ascending_south,powered=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)8),"minecraft:activator_rail[shape=north_south,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)9),"minecraft:activator_rail[shape=east_west,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)10),"minecraft:activator_rail[shape=ascending_east,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)11),"minecraft:activator_rail[shape=ascending_west,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)12),"minecraft:activator_rail[shape=ascending_north,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(157,(byte)13),"minecraft:activator_rail[shape=ascending_south,powered=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)0),"minecraft:dropper[triggered=false,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)1),"minecraft:dropper[triggered=false,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)2),"minecraft:dropper[triggered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)3),"minecraft:dropper[triggered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)4),"minecraft:dropper[triggered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)5),"minecraft:dropper[triggered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)8),"minecraft:dropper[triggered=true,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)9),"minecraft:dropper[triggered=true,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)10),"minecraft:dropper[triggered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)11),"minecraft:dropper[triggered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)12),"minecraft:dropper[triggered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(158,(byte)13),"minecraft:dropper[triggered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)0),"minecraft:white_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)1),"minecraft:orange_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)2),"minecraft:magenta_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)3),"minecraft:light_blue_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)4),"minecraft:yellow_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)5),"minecraft:lime_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)6),"minecraft:pink_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)7),"minecraft:gray_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)8),"minecraft:light_gray_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)9),"minecraft:cyan_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)10),"minecraft:purple_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)11),"minecraft:blue_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)12),"minecraft:brown_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)13),"minecraft:green_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)14),"minecraft:red_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(160,(byte)15),"minecraft:black_stained_glass_pane[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)0),"minecraft:acacia_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)1),"minecraft:dark_oak_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)4),"minecraft:acacia_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)5),"minecraft:dark_oak_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)8),"minecraft:acacia_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)9),"minecraft:dark_oak_leaves[persistent=false,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)12),"minecraft:acacia_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(161,(byte)13),"minecraft:dark_oak_leaves[persistent=true,distance=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(162,(byte)0),"minecraft:acacia_log[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(162,(byte)1),"minecraft:dark_oak_log[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(162,(byte)4),"minecraft:acacia_log[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(162,(byte)5),"minecraft:dark_oak_log[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(162,(byte)8),"minecraft:acacia_log[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(162,(byte)9),"minecraft:dark_oak_log[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)0),"minecraft:acacia_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)1),"minecraft:acacia_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)2),"minecraft:acacia_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)3),"minecraft:acacia_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)4),"minecraft:acacia_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)5),"minecraft:acacia_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)6),"minecraft:acacia_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(163,(byte)7),"minecraft:acacia_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)0),"minecraft:dark_oak_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)1),"minecraft:dark_oak_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)2),"minecraft:dark_oak_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)3),"minecraft:dark_oak_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)4),"minecraft:dark_oak_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)5),"minecraft:dark_oak_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)6),"minecraft:dark_oak_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(164,(byte)7),"minecraft:dark_oak_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)0),"minecraft:iron_trapdoor[half=bottom,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)1),"minecraft:iron_trapdoor[half=bottom,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)2),"minecraft:iron_trapdoor[half=bottom,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)3),"minecraft:iron_trapdoor[half=bottom,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)4),"minecraft:iron_trapdoor[half=bottom,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)5),"minecraft:iron_trapdoor[half=bottom,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)6),"minecraft:iron_trapdoor[half=bottom,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)7),"minecraft:iron_trapdoor[half=bottom,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)8),"minecraft:iron_trapdoor[half=top,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)9),"minecraft:iron_trapdoor[half=top,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)10),"minecraft:iron_trapdoor[half=top,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)11),"minecraft:iron_trapdoor[half=top,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)12),"minecraft:iron_trapdoor[half=top,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)13),"minecraft:iron_trapdoor[half=top,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)14),"minecraft:iron_trapdoor[half=top,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(167,(byte)15),"minecraft:iron_trapdoor[half=top,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(170,(byte)0),"minecraft:hay_block[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(170,(byte)4),"minecraft:hay_block[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(170,(byte)8),"minecraft:hay_block[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)0),"minecraft:sunflower[half=lower]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)1),"minecraft:lilac[half=lower]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)2),"minecraft:tall_grass[half=lower]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)3),"minecraft:large_fern[half=lower]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)4),"minecraft:rose_bush[half=lower]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)5),"minecraft:peony[half=lower]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)8),"minecraft:sunflower[half=upper]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)9),"minecraft:lilac[half=upper]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)10),"minecraft:tall_grass[half=upper]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)11),"minecraft:large_fern[half=upper]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)12),"minecraft:rose_bush[half=upper]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(175,(byte)13),"minecraft:peony[half=upper]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)0),"minecraft:white_banner[rotation=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)1),"minecraft:white_banner[rotation=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)2),"minecraft:white_banner[rotation=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)3),"minecraft:white_banner[rotation=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)4),"minecraft:white_banner[rotation=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)5),"minecraft:white_banner[rotation=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)6),"minecraft:white_banner[rotation=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)7),"minecraft:white_banner[rotation=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)8),"minecraft:white_banner[rotation=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)9),"minecraft:white_banner[rotation=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)10),"minecraft:white_banner[rotation=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)11),"minecraft:white_banner[rotation=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)12),"minecraft:white_banner[rotation=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)13),"minecraft:white_banner[rotation=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)14),"minecraft:white_banner[rotation=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(176,(byte)15),"minecraft:white_banner[rotation=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(177,(byte)2),"minecraft:white_wall_banner[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(177,(byte)3),"minecraft:white_wall_banner[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(177,(byte)4),"minecraft:white_wall_banner[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(177,(byte)5),"minecraft:white_wall_banner[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)0),"minecraft:daylight_detector[inverted=true,power=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)1),"minecraft:daylight_detector[inverted=true,power=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)2),"minecraft:daylight_detector[inverted=true,power=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)3),"minecraft:daylight_detector[inverted=true,power=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)4),"minecraft:daylight_detector[inverted=true,power=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)5),"minecraft:daylight_detector[inverted=true,power=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)6),"minecraft:daylight_detector[inverted=true,power=6]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)7),"minecraft:daylight_detector[inverted=true,power=7]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)8),"minecraft:daylight_detector[inverted=true,power=8]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)9),"minecraft:daylight_detector[inverted=true,power=9]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)10),"minecraft:daylight_detector[inverted=true,power=10]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)11),"minecraft:daylight_detector[inverted=true,power=11]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)12),"minecraft:daylight_detector[inverted=true,power=12]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)13),"minecraft:daylight_detector[inverted=true,power=13]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)14),"minecraft:daylight_detector[inverted=true,power=14]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(178,(byte)15),"minecraft:daylight_detector[inverted=true,power=15]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)0),"minecraft:red_sandstone_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)1),"minecraft:red_sandstone_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)2),"minecraft:red_sandstone_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)3),"minecraft:red_sandstone_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)4),"minecraft:red_sandstone_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)5),"minecraft:red_sandstone_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)6),"minecraft:red_sandstone_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(180,(byte)7),"minecraft:red_sandstone_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(181,(byte)0),"minecraft:red_sandstone_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(182,(byte)0),"minecraft:red_sandstone_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(182,(byte)8),"minecraft:red_sandstone_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)0),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)1),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)2),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)3),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)4),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)5),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)6),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)7),"minecraft:spruce_fence_gate[in_wall=false,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)8),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)9),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)10),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)11),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)12),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)13),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)14),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(183,(byte)15),"minecraft:spruce_fence_gate[in_wall=false,powered=true,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)0),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)1),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)2),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)3),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)4),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)5),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)6),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)7),"minecraft:birch_fence_gate[in_wall=false,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)8),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)9),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)10),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)11),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)12),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)13),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)14),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(184,(byte)15),"minecraft:birch_fence_gate[in_wall=false,powered=true,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)0),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)1),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)2),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)3),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)4),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)5),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)6),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)7),"minecraft:jungle_fence_gate[in_wall=false,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)8),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)9),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)10),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)11),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)12),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)13),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)14),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(185,(byte)15),"minecraft:jungle_fence_gate[in_wall=false,powered=true,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)0),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)1),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)2),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)3),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)4),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)5),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)6),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)7),"minecraft:dark_oak_fence_gate[in_wall=false,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)8),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)9),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)10),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)11),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)12),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)13),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)14),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(186,(byte)15),"minecraft:dark_oak_fence_gate[in_wall=false,powered=true,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)0),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)1),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)2),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)3),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)4),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)5),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)6),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)7),"minecraft:acacia_fence_gate[in_wall=false,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)8),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)9),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)10),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)11),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)12),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)13),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)14),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(187,(byte)15),"minecraft:acacia_fence_gate[in_wall=false,powered=true,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(188,(byte)0),"minecraft:spruce_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(189,(byte)0),"minecraft:birch_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(190,(byte)0),"minecraft:jungle_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(191,(byte)0),"minecraft:dark_oak_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(192,(byte)0),"minecraft:acacia_fence[east=false,south=false,north=false,west=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)0),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)1),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)2),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)3),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)4),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)5),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)6),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)7),"minecraft:spruce_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)8),"minecraft:spruce_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)9),"minecraft:spruce_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)10),"minecraft:spruce_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(193,(byte)11),"minecraft:spruce_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)0),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)1),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)2),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)3),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)4),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)5),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)6),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)7),"minecraft:birch_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)8),"minecraft:birch_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)9),"minecraft:birch_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)10),"minecraft:birch_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(194,(byte)11),"minecraft:birch_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)0),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)1),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)2),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)3),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)4),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)5),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)6),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)7),"minecraft:jungle_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)8),"minecraft:jungle_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)9),"minecraft:jungle_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)10),"minecraft:jungle_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(195,(byte)11),"minecraft:jungle_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)0),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)1),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)2),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)3),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)4),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)5),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)6),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)7),"minecraft:acacia_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)8),"minecraft:acacia_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)9),"minecraft:acacia_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)10),"minecraft:acacia_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(196,(byte)11),"minecraft:acacia_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)0),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)1),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=south,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)2),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=west,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)3),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=north,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)4),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=east,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)5),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=south,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)6),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=west,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)7),"minecraft:dark_oak_door[hinge=right,half=lower,powered=false,facing=north,open=true]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)8),"minecraft:dark_oak_door[hinge=left,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)9),"minecraft:dark_oak_door[hinge=right,half=upper,powered=false,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)10),"minecraft:dark_oak_door[hinge=left,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(197,(byte)11),"minecraft:dark_oak_door[hinge=right,half=upper,powered=true,facing=east,open=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(198,(byte)0),"minecraft:end_rod[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(198,(byte)1),"minecraft:end_rod[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(198,(byte)2),"minecraft:end_rod[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(198,(byte)3),"minecraft:end_rod[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(198,(byte)4),"minecraft:end_rod[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(198,(byte)5),"minecraft:end_rod[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(199,(byte)0),"minecraft:chorus_plant[east=false,south=false,north=false,west=false,up=false,down=false]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(200,(byte)0),"minecraft:chorus_flower[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(200,(byte)1),"minecraft:chorus_flower[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(200,(byte)2),"minecraft:chorus_flower[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(200,(byte)3),"minecraft:chorus_flower[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(200,(byte)4),"minecraft:chorus_flower[age=4]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(200,(byte)5),"minecraft:chorus_flower[age=5]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(202,(byte)0),"minecraft:purpur_pillar[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(202,(byte)4),"minecraft:purpur_pillar[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(202,(byte)8),"minecraft:purpur_pillar[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)0),"minecraft:purpur_stairs[half=bottom,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)1),"minecraft:purpur_stairs[half=bottom,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)2),"minecraft:purpur_stairs[half=bottom,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)3),"minecraft:purpur_stairs[half=bottom,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)4),"minecraft:purpur_stairs[half=top,shape=straight,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)5),"minecraft:purpur_stairs[half=top,shape=straight,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)6),"minecraft:purpur_stairs[half=top,shape=straight,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(203,(byte)7),"minecraft:purpur_stairs[half=top,shape=straight,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(204,(byte)0),"minecraft:purpur_slab[type=double]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(205,(byte)0),"minecraft:purpur_slab[type=bottom]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(205,(byte)8),"minecraft:purpur_slab[type=top]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(207,(byte)0),"minecraft:beetroots[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(207,(byte)1),"minecraft:beetroots[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(207,(byte)2),"minecraft:beetroots[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(207,(byte)3),"minecraft:beetroots[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)0),"minecraft:repeating_command_block[conditional=false,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)1),"minecraft:repeating_command_block[conditional=false,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)2),"minecraft:repeating_command_block[conditional=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)3),"minecraft:repeating_command_block[conditional=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)4),"minecraft:repeating_command_block[conditional=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)5),"minecraft:repeating_command_block[conditional=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)8),"minecraft:repeating_command_block[conditional=true,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)9),"minecraft:repeating_command_block[conditional=true,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)10),"minecraft:repeating_command_block[conditional=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)11),"minecraft:repeating_command_block[conditional=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)12),"minecraft:repeating_command_block[conditional=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(210,(byte)13),"minecraft:repeating_command_block[conditional=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)0),"minecraft:chain_command_block[conditional=false,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)1),"minecraft:chain_command_block[conditional=false,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)2),"minecraft:chain_command_block[conditional=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)3),"minecraft:chain_command_block[conditional=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)4),"minecraft:chain_command_block[conditional=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)5),"minecraft:chain_command_block[conditional=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)8),"minecraft:chain_command_block[conditional=true,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)9),"minecraft:chain_command_block[conditional=true,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)10),"minecraft:chain_command_block[conditional=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)11),"minecraft:chain_command_block[conditional=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)12),"minecraft:chain_command_block[conditional=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(211,(byte)13),"minecraft:chain_command_block[conditional=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(212,(byte)0),"minecraft:frosted_ice[age=0]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(212,(byte)1),"minecraft:frosted_ice[age=1]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(212,(byte)2),"minecraft:frosted_ice[age=2]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(212,(byte)3),"minecraft:frosted_ice[age=3]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(216,(byte)0),"minecraft:bone_block[axis=y]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(216,(byte)4),"minecraft:bone_block[axis=x]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(216,(byte)8),"minecraft:bone_block[axis=z]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)0),"minecraft:observer[powered=false,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)1),"minecraft:observer[powered=false,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)2),"minecraft:observer[powered=false,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)3),"minecraft:observer[powered=false,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)4),"minecraft:observer[powered=false,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)5),"minecraft:observer[powered=false,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)8),"minecraft:observer[powered=true,facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)9),"minecraft:observer[powered=true,facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)10),"minecraft:observer[powered=true,facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)11),"minecraft:observer[powered=true,facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)12),"minecraft:observer[powered=true,facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(218,(byte)13),"minecraft:observer[powered=true,facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(219,(byte)0),"minecraft:white_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(219,(byte)1),"minecraft:white_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(219,(byte)2),"minecraft:white_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(219,(byte)3),"minecraft:white_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(219,(byte)4),"minecraft:white_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(219,(byte)5),"minecraft:white_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(220,(byte)0),"minecraft:orange_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(220,(byte)1),"minecraft:orange_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(220,(byte)2),"minecraft:orange_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(220,(byte)3),"minecraft:orange_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(220,(byte)4),"minecraft:orange_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(220,(byte)5),"minecraft:orange_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(221,(byte)0),"minecraft:magenta_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(221,(byte)1),"minecraft:magenta_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(221,(byte)2),"minecraft:magenta_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(221,(byte)3),"minecraft:magenta_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(221,(byte)4),"minecraft:magenta_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(221,(byte)5),"minecraft:magenta_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(222,(byte)0),"minecraft:light_blue_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(222,(byte)1),"minecraft:light_blue_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(222,(byte)2),"minecraft:light_blue_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(222,(byte)3),"minecraft:light_blue_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(222,(byte)4),"minecraft:light_blue_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(222,(byte)5),"minecraft:light_blue_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(223,(byte)0),"minecraft:yellow_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(223,(byte)1),"minecraft:yellow_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(223,(byte)2),"minecraft:yellow_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(223,(byte)3),"minecraft:yellow_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(223,(byte)4),"minecraft:yellow_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(223,(byte)5),"minecraft:yellow_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(224,(byte)0),"minecraft:lime_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(224,(byte)1),"minecraft:lime_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(224,(byte)2),"minecraft:lime_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(224,(byte)3),"minecraft:lime_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(224,(byte)4),"minecraft:lime_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(224,(byte)5),"minecraft:lime_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(225,(byte)0),"minecraft:pink_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(225,(byte)1),"minecraft:pink_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(225,(byte)2),"minecraft:pink_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(225,(byte)3),"minecraft:pink_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(225,(byte)4),"minecraft:pink_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(225,(byte)5),"minecraft:pink_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(226,(byte)0),"minecraft:gray_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(226,(byte)1),"minecraft:gray_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(226,(byte)2),"minecraft:gray_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(226,(byte)3),"minecraft:gray_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(226,(byte)4),"minecraft:gray_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(226,(byte)5),"minecraft:gray_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(227,(byte)0),"minecraft:light_gray_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(227,(byte)1),"minecraft:light_gray_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(227,(byte)2),"minecraft:light_gray_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(227,(byte)3),"minecraft:light_gray_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(227,(byte)4),"minecraft:light_gray_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(227,(byte)5),"minecraft:light_gray_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(228,(byte)0),"minecraft:cyan_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(228,(byte)1),"minecraft:cyan_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(228,(byte)2),"minecraft:cyan_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(228,(byte)3),"minecraft:cyan_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(228,(byte)4),"minecraft:cyan_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(228,(byte)5),"minecraft:cyan_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(229,(byte)0),"minecraft:purple_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(229,(byte)1),"minecraft:purple_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(229,(byte)2),"minecraft:purple_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(229,(byte)3),"minecraft:purple_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(229,(byte)4),"minecraft:purple_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(229,(byte)5),"minecraft:purple_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(230,(byte)0),"minecraft:blue_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(230,(byte)1),"minecraft:blue_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(230,(byte)2),"minecraft:blue_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(230,(byte)3),"minecraft:blue_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(230,(byte)4),"minecraft:blue_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(230,(byte)5),"minecraft:blue_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(231,(byte)0),"minecraft:brown_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(231,(byte)1),"minecraft:brown_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(231,(byte)2),"minecraft:brown_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(231,(byte)3),"minecraft:brown_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(231,(byte)4),"minecraft:brown_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(231,(byte)5),"minecraft:brown_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(232,(byte)0),"minecraft:green_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(232,(byte)1),"minecraft:green_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(232,(byte)2),"minecraft:green_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(232,(byte)3),"minecraft:green_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(232,(byte)4),"minecraft:green_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(232,(byte)5),"minecraft:green_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(233,(byte)0),"minecraft:red_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(233,(byte)1),"minecraft:red_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(233,(byte)2),"minecraft:red_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(233,(byte)3),"minecraft:red_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(233,(byte)4),"minecraft:red_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(233,(byte)5),"minecraft:red_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(234,(byte)0),"minecraft:black_shulker_box[facing=down]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(234,(byte)1),"minecraft:black_shulker_box[facing=up]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(234,(byte)2),"minecraft:black_shulker_box[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(234,(byte)3),"minecraft:black_shulker_box[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(234,(byte)4),"minecraft:black_shulker_box[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(234,(byte)5),"minecraft:black_shulker_box[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(235,(byte)0),"minecraft:white_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(235,(byte)1),"minecraft:white_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(235,(byte)2),"minecraft:white_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(235,(byte)3),"minecraft:white_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(236,(byte)0),"minecraft:orange_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(236,(byte)1),"minecraft:orange_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(236,(byte)2),"minecraft:orange_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(236,(byte)3),"minecraft:orange_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(237,(byte)0),"minecraft:magenta_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(237,(byte)1),"minecraft:magenta_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(237,(byte)2),"minecraft:magenta_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(237,(byte)3),"minecraft:magenta_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(238,(byte)0),"minecraft:light_blue_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(238,(byte)1),"minecraft:light_blue_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(238,(byte)2),"minecraft:light_blue_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(238,(byte)3),"minecraft:light_blue_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(239,(byte)0),"minecraft:yellow_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(239,(byte)1),"minecraft:yellow_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(239,(byte)2),"minecraft:yellow_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(239,(byte)3),"minecraft:yellow_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(240,(byte)0),"minecraft:lime_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(240,(byte)1),"minecraft:lime_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(240,(byte)2),"minecraft:lime_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(240,(byte)3),"minecraft:lime_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(241,(byte)0),"minecraft:pink_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(241,(byte)1),"minecraft:pink_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(241,(byte)2),"minecraft:pink_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(241,(byte)3),"minecraft:pink_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(242,(byte)0),"minecraft:gray_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(242,(byte)1),"minecraft:gray_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(242,(byte)2),"minecraft:gray_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(242,(byte)3),"minecraft:gray_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(243,(byte)0),"minecraft:light_gray_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(243,(byte)1),"minecraft:light_gray_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(243,(byte)2),"minecraft:light_gray_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(243,(byte)3),"minecraft:light_gray_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(244,(byte)0),"minecraft:cyan_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(244,(byte)1),"minecraft:cyan_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(244,(byte)2),"minecraft:cyan_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(244,(byte)3),"minecraft:cyan_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(245,(byte)0),"minecraft:purple_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(245,(byte)1),"minecraft:purple_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(245,(byte)2),"minecraft:purple_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(245,(byte)3),"minecraft:purple_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(246,(byte)0),"minecraft:blue_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(246,(byte)1),"minecraft:blue_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(246,(byte)2),"minecraft:blue_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(246,(byte)3),"minecraft:blue_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(247,(byte)0),"minecraft:brown_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(247,(byte)1),"minecraft:brown_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(247,(byte)2),"minecraft:brown_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(247,(byte)3),"minecraft:brown_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(248,(byte)0),"minecraft:green_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(248,(byte)1),"minecraft:green_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(248,(byte)2),"minecraft:green_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(248,(byte)3),"minecraft:green_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(249,(byte)0),"minecraft:red_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(249,(byte)1),"minecraft:red_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(249,(byte)2),"minecraft:red_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(249,(byte)3),"minecraft:red_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(250,(byte)0),"minecraft:black_glazed_terracotta[facing=south]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(250,(byte)1),"minecraft:black_glazed_terracotta[facing=west]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(250,(byte)2),"minecraft:black_glazed_terracotta[facing=north]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(250,(byte)3),"minecraft:black_glazed_terracotta[facing=east]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(255,(byte)0),"minecraft:structure_block[mode=save]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(255,(byte)1),"minecraft:structure_block[mode=load]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(255,(byte)2),"minecraft:structure_block[mode=corner]");
        LEGACY_TO_BLOCKDATA.put(new PreFlatteningMaterial(255,(byte)3),"minecraft:structure_block[mode=data]");

        for (Map.Entry<PreFlatteningMaterial,String> e : LEGACY_TO_BLOCKDATA.entrySet()) BLOCKDATA_TO_LEGACY.put(e.getValue(), e.getKey());
    }

    private XBlock() {
    }

    private static String legacyToBlockData(PreFlatteningMaterial material) {
        String match = LEGACY_TO_BLOCKDATA.get(material);
        if (match != null) return match; // already hashed

        // not in list; use the default name
        return "minecraft:" + XMaterial.matchXMaterial(material.getId(), material.getSubId()).get().name().toLowerCase();
    }

    public static boolean isLit(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Lightable)) return false;
            org.bukkit.block.data.Lightable lightable = (org.bukkit.block.data.Lightable) block.getBlockData();
            return lightable.isLit();
        }

        return isMaterial(block, BlockMaterial.REDSTONE_LAMP_ON, BlockMaterial.REDSTONE_TORCH_ON, BlockMaterial.BURNING_FURNACE);
    }

    /**
     * Checks if the block is a container.
     * Containers are chests, hoppers, enderchests and everything that
     * has an inventory.
     *
     * @param block the block to check.
     * @return true if the block is a container, otherwise false.
     */
    public static boolean isContainer(@Nullable Block block) {
        return block != null && block.getState() instanceof InventoryHolder;
    }

    /**
     * Can be furnaces or redstone lamps.
     *
     * @param block the block to change.
     * @param lit   if it should be lit or not.
     */
    public static void setLit(Block block, boolean lit) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Lightable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Lightable lightable = (org.bukkit.block.data.Lightable) data;
            lightable.setLit(lit);
            block.setBlockData(data, false);
            return;
        }

        String name = block.getType().name();
        if (name.endsWith("FURNACE")) block.setType(BlockMaterial.BURNING_FURNACE.material);
        else if (name.startsWith("REDSTONE_LAMP")) block.setType(BlockMaterial.REDSTONE_LAMP_ON.material);
        else block.setType(BlockMaterial.REDSTONE_TORCH_ON.material);
    }

    /**
     * Any material that can be planted which is from {@link #CROPS}
     *
     * @param material the material to check.
     * @return true if this material is a crop, otherwise false.
     */
    public static boolean isCrop(XMaterial material) {
        return CROPS.contains(material);
    }

    /**
     * Any material that can damage players, usually by interacting with the block.
     *
     * @param material the material to check.
     * @return true if this material is dangerous, otherwise false.
     */
    public static boolean isDangerous(XMaterial material) {
        return DANGEROUS.contains(material);
    }

    /**
     * Wool and Dye. But Dye is not a block itself.
     */
    public static DyeColor getColor(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Colorable)) return null;
            Colorable colorable = (Colorable) block.getBlockData();
            return colorable.getColor();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Wool) {
            Wool wool = (Wool) data;
            return wool.getColor();
        }
        return null;
    }

    public static boolean isCake(@Nullable Material material) {
        return material == Material.CAKE || material == BlockMaterial.CAKE_BLOCK.material;
    }

    public static boolean isWheat(@Nullable Material material) {
        return material == Material.WHEAT || material == BlockMaterial.CROPS.material;
    }

    public static boolean isSugarCane(@Nullable Material material) {
        return material == Material.SUGAR_CANE || material == BlockMaterial.SUGAR_CANE_BLOCK.material;
    }

    public static boolean isBeetroot(@Nullable Material material) {
        return material == Material.BEETROOT || material == Material.BEETROOTS || material == BlockMaterial.BEETROOT_BLOCK.material;
    }

    public static boolean isNetherWart(@Nullable Material material) {
        return material == Material.NETHER_WART || material == BlockMaterial.NETHER_WARTS.material;
    }

    public static boolean isCarrot(@Nullable Material material) {
        return material == Material.CARROT || material == Material.CARROTS;
    }

    public static boolean isMelon(@Nullable Material material) {
        return material == Material.MELON || material == Material.MELON_SLICE || material == BlockMaterial.MELON_BLOCK.material;
    }

    public static boolean isPotato(@Nullable Material material) {
        return material == Material.POTATO || material == Material.POTATOES;
    }

    public static BlockFace getDirection(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Directional)) return BlockFace.SELF;
            org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional) block.getBlockData();
            return direction.getFacing();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof org.bukkit.material.Directional)
            return ((org.bukkit.material.Directional) data).getFacing();
        return BlockFace.SELF;
    }

    public static boolean setDirection(Block block, BlockFace facing) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Directional)) return false;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional) data;
            direction.setFacing(facing);
            block.setBlockData(data, false);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Directional) {
            if (XMaterial.matchXMaterial(block.getType()) == XMaterial.LADDER) facing = facing.getOppositeFace();
            ((Directional) data).setFacingDirection(facing);
            state.update(true);
            return true;
        }
        return false;
    }

    public static boolean setType(@Nonnull Block block, @Nullable XMaterial material, boolean applyPhysics) {
        Objects.requireNonNull(block, "Cannot set type of null block");
        if (material == null) material = XMaterial.AIR;
        XMaterial smartConversion = ITEM_TO_BLOCK.get(material);
        if (smartConversion != null) material = smartConversion;
        if (material.parseMaterial() == null) return false;

        block.setType(material.parseMaterial(), applyPhysics);
        if (ISFLAT) return false;

        String parsedName = material.parseMaterial().name();
        if (parsedName.endsWith("_ITEM")) {
            String blockName = parsedName.substring(0, parsedName.length() - "_ITEM".length());
            Material blockMaterial = Objects.requireNonNull(Material.getMaterial(blockName),
                    () -> "Could not find block material for item '" + parsedName + "' as '" + blockName + '\'');
            block.setType(blockMaterial, applyPhysics);
        } else if (parsedName.contains("CAKE")) {
            Material blockMaterial = Material.getMaterial("CAKE_BLOCK");
            block.setType(blockMaterial, applyPhysics);
        }

        LegacyMaterial legacyMaterial = LegacyMaterial.getMaterial(parsedName);
        if (legacyMaterial == LegacyMaterial.BANNER)
            block.setType(LegacyMaterial.STANDING_BANNER.material, applyPhysics);
        LegacyMaterial.Handling handling = legacyMaterial == null ? null : legacyMaterial.handling;

        BlockState state = block.getState();
        boolean update = false;

        if (handling == LegacyMaterial.Handling.COLORABLE) {
            if (state instanceof Banner) {
                Banner banner = (Banner) state;
                String xName = material.name();
                int colorIndex = xName.indexOf('_');
                String color = xName.substring(0, colorIndex);
                if (color.equals("LIGHT")) color = xName.substring(0, "LIGHT_".length() + 4);

                banner.setBaseColor(DyeColor.valueOf(color));
            } else state.setRawData(material.getData());
            update = true;
        } else if (handling == LegacyMaterial.Handling.WOOD_SPECIES) {
            // Wood doesn't exist in 1.8
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/material/Wood.java?until=7d83cba0f2575112577ed7a091ed8a193bfc261a&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2Fmaterial%2FWood.java
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/TreeSpecies.java

            String name = material.name();
            int firstIndicator = name.indexOf('_');
            if (firstIndicator < 0) return false;
            String woodType = name.substring(0, firstIndicator);

            TreeSpecies species;
            switch (woodType) {
                case "OAK":
                    species = TreeSpecies.GENERIC;
                    break;
                case "DARK":
                    species = TreeSpecies.DARK_OAK;
                    break;
                case "SPRUCE":
                    species = TreeSpecies.REDWOOD;
                    break;
                default: {
                    try {
                        species = TreeSpecies.valueOf(woodType);
                    } catch (IllegalArgumentException ex) {
                        throw new AssertionError("Unknown material " + legacyMaterial + " for wood species");
                    }
                }
            }

            // Doesn't handle stairs, slabs, fence and fence gates as they had their own separate materials.
            boolean firstType = false;
            switch (legacyMaterial) {
                case WOOD:
                case WOOD_DOUBLE_STEP:
                    state.setRawData(species.getData());
                    update = true;
                    break;
                case LOG:
                case LEAVES:
                    firstType = true;
                    // fall through to next switch statement below
                case LOG_2:
                case LEAVES_2:
                    switch (species) {
                        case GENERIC:
                        case REDWOOD:
                        case BIRCH:
                        case JUNGLE:
                            if (!firstType)
                                throw new AssertionError("Invalid tree species " + species + " for block type" + legacyMaterial + ", use block type 2 instead");
                            break;
                        case ACACIA:
                        case DARK_OAK:
                            if (firstType)
                                throw new AssertionError("Invalid tree species " + species + " for block type 2 " + legacyMaterial + ", use block type instead");
                            break;
                    }
                    state.setRawData((byte) ((state.getRawData() & 0xC) | (species.getData() & 0x3)));
                    update = true;
                    break;
                case SAPLING:
                case WOOD_STEP:
                    state.setRawData((byte) ((state.getRawData() & 0x8) | species.getData()));
                    update = true;
                    break;
                default:
                    throw new AssertionError("Unknown block type " + legacyMaterial + " for tree species: " + species);
            }
        } else if (material.getData() != 0) {
            state.setRawData(material.getData());
            update = true;
        }

        if (update) state.update(false, applyPhysics);
        return update;
    }

    public static boolean setType(@Nonnull Block block, @Nullable XMaterial material) {
        return setType(block, material, true);
    }

    public static boolean setType(@Nonnull Block block, String blockData) throws IllegalArgumentException {
        String originalBlockData = XBlock.getBlockData(block);

        if (ISFLAT) {
            block.setBlockData(Bukkit.createBlockData(blockData));
        }
        else {
            PreFlatteningMaterial set = BLOCKDATA_TO_LEGACY.get(blockData);
            if (set != null) {
                try {
                    setBlockTypePreFlatteningMethod.invoke(block, set.getId(), set.getSubId(), true); // TODO gravity
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | NullPointerException ignored) {}
            }
            else {
                // not cached; get default XMaterial
                Pattern r = Pattern.compile("^minecraft:([^\\]]+)");
                Matcher m = r.matcher(blockData);
                if (!m.find()) throw new IllegalArgumentException("Invalid blockData: " + blockData);

                Optional<XMaterial> mat = XMaterial.matchXMaterial(m.group(1));
                if (!mat.isPresent()) throw new IllegalArgumentException("Material '" + m.group(1) + "' not found");

                XBlock.setType(block, mat.get());
            }
        }

        boolean updated = !originalBlockData.equals(XBlock.getBlockData(block));
        return updated; // `setType` returns if the block was updated
    }

    public static String getBlockData(@Nonnull Block block) {
        if (ISFLAT) {
            return block.getBlockData().toString();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        byte subid = data.getData();
        return XBlock.legacyToBlockData(new PreFlatteningMaterial(data.getItemType().getId(), subid));
    }

    public static int getAge(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Ageable)) return 0;
            org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) block.getBlockData();
            return ageable.getAge();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        return data.getData();
    }

    public static void setAge(Block block, int age) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Ageable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) data;
            ageable.setAge(age);
            block.setBlockData(data, false);
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        data.setData((byte) age);
        state.update(true);
    }

    /**
     * Sets the type of any block that can be colored.
     *
     * @param block the block to color.
     * @param color the color to use.
     * @return true if the block can be colored, otherwise false.
     */
    public static boolean setColor(Block block, DyeColor color) {
        if (ISFLAT) {
            String type = block.getType().name();
            int index = type.indexOf('_');
            if (index == -1) return false;

            String realType = type.substring(index);
            Material material = Material.getMaterial(color.name() + '_' + realType);
            if (material == null) return false;
            block.setType(material);
            return true;
        }

        BlockState state = block.getState();
        state.setRawData(color.getWoolData());
        state.update(true);
        return false;
    }

    /**
     * Can be used on cauldrons as well.
     *
     * @param block the block to set the fluid level of.
     * @param level the level of fluid.
     * @return true if this block can have a fluid level, otherwise false.
     */
    public static boolean setFluidLevel(Block block, int level) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Levelled)) return false;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Levelled levelled = (org.bukkit.block.data.Levelled) data;
            levelled.setLevel(level);
            block.setBlockData(data, false);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        data.setData((byte) level);
        state.update(true);
        return false;
    }

    public static int getFluidLevel(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Levelled)) return -1;
            org.bukkit.block.data.Levelled levelled = (org.bukkit.block.data.Levelled) block.getBlockData();
            return levelled.getLevel();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        return data.getData();
    }

    public static boolean isWaterStationary(Block block) {
        return ISFLAT ? getFluidLevel(block) < 7 : block.getType() == BlockMaterial.STATIONARY_WATER.material;
    }

    public static boolean isWater(Material material) {
        return material == Material.WATER || material == BlockMaterial.STATIONARY_WATER.material;
    }

    public static boolean isLava(Material material) {
        return material == Material.LAVA || material == BlockMaterial.STATIONARY_LAVA.material;
    }

    public static boolean isOneOf(Block block, Collection<String> blocks) {
        if (blocks == null || blocks.isEmpty()) return false;
        String name = block.getType().name();
        XMaterial matched = XMaterial.matchXMaterial(block.getType());

        for (String comp : blocks) {
            String checker = comp.toUpperCase(Locale.ENGLISH);
            if (checker.startsWith("CONTAINS:")) {
                comp = XMaterial.format(checker.substring(9));
                if (name.contains(comp)) return true;
                continue;
            }
            if (checker.startsWith("REGEX:")) {
                comp = comp.substring(6);
                if (name.matches(comp)) return true;
                continue;
            }

            // Direct Object Equals
            Optional<XMaterial> xMat = XMaterial.matchXMaterial(comp);
            if (xMat.isPresent()) {
                if (matched == xMat.get() || isType(block, xMat.get())) return true;
            }
        }
        return false;
    }

    public static void setCakeSlices(Block block, int amount) {
        if (!isCake(block.getType())) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
        if (ISFLAT) {
            BlockData data = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) data;
            int remaining = cake.getMaximumBites() - (cake.getBites() + amount);
            if (remaining > 0) {
                cake.setBites(remaining);
                block.setBlockData(data);
            } else {
                block.breakNaturally();
            }

            return;
        }

        BlockState state = block.getState();
        Cake cake = (Cake) state.getData();
        if (amount > 0) {
            cake.setSlicesRemaining(amount);
            state.update(true);
        } else {
            block.breakNaturally();
        }
    }

    public static int addCakeSlices(Block block, int slices) {
        if (!isCake(block.getType())) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
        if (ISFLAT) {
            BlockData data = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) data;
            int bites = cake.getBites() - slices;
            int remaining = cake.getMaximumBites() - bites;

            if (remaining > 0) {
                cake.setBites(bites);
                block.setBlockData(data);
                return remaining;
            } else {
                block.breakNaturally();
                return 0;
            }
        }

        BlockState state = block.getState();
        Cake cake = (Cake) state.getData();
        int remaining = cake.getSlicesRemaining() + slices;

        if (remaining > 0) {
            cake.setSlicesRemaining(remaining);
            state.update(true);
            return remaining;
        } else {
            block.breakNaturally();
            return 0;
        }
    }

    public static void setEnderPearlOnFrame(Block endPortalFrame, boolean eye) {
        BlockState state = endPortalFrame.getState();
        if (ISFLAT) {
            org.bukkit.block.data.BlockData data = state.getBlockData();
            org.bukkit.block.data.type.EndPortalFrame frame = (org.bukkit.block.data.type.EndPortalFrame) data;
            frame.setEye(eye);
            state.setBlockData(data);
        } else {
            state.setRawData((byte) (eye ? 4 : 0));
        }
        state.update(true);
    }

    /**
     * @param block the block to get its XMaterial type.
     * @return the XMaterial of the block.
     * @deprecated Not stable, use {@link #isType(Block, XMaterial)} or {@link #isSimilar(Block, XMaterial)} instead.
     * If you want to save a block material somewhere, you need to use {@link XMaterial#matchXMaterial(Material)}
     */
    @Deprecated
    public static XMaterial getType(Block block) {
        if (ISFLAT) return XMaterial.matchXMaterial(block.getType());
        String type = block.getType().name();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        byte dataValue;

        if (data instanceof Wood) {
            TreeSpecies species = ((Wood) data).getSpecies();
            dataValue = species.getData();
        } else if (data instanceof Colorable) {
            DyeColor color = ((Colorable) data).getColor();
            dataValue = color.getDyeData();
        } else {
            dataValue = data.getData();
        }

        return XMaterial.matchDefinedXMaterial(type, dataValue)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported material for block " + dataValue + ": " + block.getType().name()));
    }

    /**
     * Same as {@link #isType(Block, XMaterial)} except it also does a simple {@link XMaterial#matchXMaterial(Material)}
     * comparison with the given block and material.
     *
     * @param block    the block to compare.
     * @param material the material to compare with.
     * @return true if block type is similar to the given material.
     * @see #isType(Block, XMaterial)
     * @since 1.3.0
     */
    public static boolean isSimilar(Block block, XMaterial material) {
        return material == XMaterial.matchXMaterial(block.getType()) || isType(block, material);
    }

    /**
     * <b>Universal Method</b>
     * <p>
     * Check if the block type matches the specified XMaterial.
     * Note that this method assumes that you've already tried doing {@link XMaterial#matchXMaterial(Material)} using
     * {@link Block#getType()} and compared it with the other XMaterial. If not, use {@link #isSimilar(Block, XMaterial)}
     *
     * @param block    the block to check.
     * @param material the XMaterial similar to this block type.
     * @return true if the raw block type matches with the material.
     * @see #isSimilar(Block, XMaterial)
     */
    public static boolean isType(Block block, XMaterial material) {
        Material mat = block.getType();
        switch (material) {
            case CAKE:
                return isCake(mat);
            case NETHER_WART:
                return isNetherWart(mat);
            case MELON:
            case MELON_SLICE:
                return isMelon(mat);
            case CARROT:
            case CARROTS:
                return isCarrot(mat);
            case POTATO:
            case POTATOES:
                return isPotato(mat);
            case WHEAT:
            case WHEAT_SEEDS:
                return isWheat(mat);
            case BEETROOT:
            case BEETROOT_SEEDS:
            case BEETROOTS:
                return isBeetroot(mat);
            case SUGAR_CANE:
                return isSugarCane(mat);
            case WATER:
                return isWater(mat);
            case LAVA:
                return isLava(mat);
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                return isAir(mat);
        }
        return false;
    }

    public static boolean isAir(@Nullable Material material) {
        if (ISFLAT) {
            // material.isAir() doesn't exist for 1.13
            switch (material) {
                case AIR:
                case CAVE_AIR:
                case VOID_AIR:
                    return true;
                default:
                    return false;
            }
        }
        return material == Material.AIR;
    }

    public static boolean isPowered(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Powerable)) return false;
            org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) block.getBlockData();
            return powerable.isPowered();
        }

        String name = block.getType().name();
        if (name.startsWith("REDSTONE_COMPARATOR"))
            return block.getType() == BlockMaterial.REDSTONE_COMPARATOR_ON.material;
        return false;
    }

    public static void setPowered(Block block, boolean powered) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Powerable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) data;
            powerable.setPowered(powered);
            block.setBlockData(data, false);
            return;
        }

        String name = block.getType().name();
        if (name.startsWith("REDSTONE_COMPARATOR")) block.setType(BlockMaterial.REDSTONE_COMPARATOR_ON.material);
    }

    public static boolean isOpen(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Openable)) return false;
            org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) block.getBlockData();
            return openable.isOpen();
        }

        BlockState state = block.getState();
        if (!(state instanceof Openable)) return false;
        Openable openable = (Openable) state.getData();
        return openable.isOpen();
    }

    public static void setOpened(Block block, boolean opened) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Openable)) return;
            // These useless "data" variables are used because JVM doesn't like upcasts/downcasts for
            // non-existing classes even if unused.
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) data;
            openable.setOpen(opened);
            block.setBlockData(data, false);
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Openable)) return;
        Openable openable = (Openable) state.getData();
        openable.setOpen(opened);
        state.setData((MaterialData) openable);
        state.update();
    }

    public static BlockFace getRotation(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Rotatable)) return null;
            org.bukkit.block.data.Rotatable rotatable = (org.bukkit.block.data.Rotatable) block.getBlockData();
            return rotatable.getRotation();
        }

        return null;
    }

    public static void setRotation(Block block, BlockFace facing) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Rotatable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Rotatable rotatable = (org.bukkit.block.data.Rotatable) data;
            rotatable.setRotation(facing);
            block.setBlockData(data, false);
        }
    }

    private static boolean isMaterial(Block block, BlockMaterial... materials) {
        Material type = block.getType();
        for (BlockMaterial material : materials) {
            if (type == material.material) return true;
        }
        return false;
    }

    private enum LegacyMaterial {
        // Colorable
        STANDING_BANNER(Handling.COLORABLE), WALL_BANNER(Handling.COLORABLE), BANNER(Handling.COLORABLE),
        CARPET(Handling.COLORABLE), WOOL(Handling.COLORABLE), STAINED_CLAY(Handling.COLORABLE),
        STAINED_GLASS(Handling.COLORABLE), STAINED_GLASS_PANE(Handling.COLORABLE), THIN_GLASS(Handling.COLORABLE),

        // Wood Species
        WOOD(Handling.WOOD_SPECIES), WOOD_STEP(Handling.WOOD_SPECIES), WOOD_DOUBLE_STEP(Handling.WOOD_SPECIES),
        LEAVES(Handling.WOOD_SPECIES), LEAVES_2(Handling.WOOD_SPECIES),
        LOG(Handling.WOOD_SPECIES), LOG_2(Handling.WOOD_SPECIES),
        SAPLING(Handling.WOOD_SPECIES);

        private static final Map<String, LegacyMaterial> LOOKUP = new HashMap<>();

        static {
            for (LegacyMaterial legacyMaterial : values()) {
                LOOKUP.put(legacyMaterial.name(), legacyMaterial);
            }
        }

        private final Material material = Material.getMaterial(name());
        private final Handling handling;

        LegacyMaterial(Handling handling) {
            this.handling = handling;
        }

        private static LegacyMaterial getMaterial(String name) {
            return LOOKUP.get(name);
        }

        private enum Handling {COLORABLE, WOOD_SPECIES;}
    }

    /**
     * An enum with cached legacy materials which can be used when comparing blocks with blocks and blocks with items.
     *
     * @since 2.0.0
     */
    public enum BlockMaterial {
        // Blocks
        CAKE_BLOCK, CROPS, SUGAR_CANE_BLOCK, BEETROOT_BLOCK, NETHER_WARTS, MELON_BLOCK,

        // Others
        BURNING_FURNACE, STATIONARY_WATER, STATIONARY_LAVA,

        // Toggleable
        REDSTONE_LAMP_ON, REDSTONE_LAMP_OFF,
        REDSTONE_TORCH_ON, REDSTONE_TORCH_OFF,
        REDSTONE_COMPARATOR_ON, REDSTONE_COMPARATOR_OFF;

        @Nullable
        private final Material material;

        BlockMaterial() {
            this.material = Material.getMaterial(this.name());
        }
    }

    private static class PreFlatteningMaterial {
        private final int id;
        private final byte subId;
        private final int hash;

        public PreFlatteningMaterial(int id, byte subId) {
            this.id = id;
            this.subId = subId;

            this.hash = Objects.hash(id, subId);
        }

        public int getId() {
            return this.id;
        }

        public byte getSubId() {
            return this.subId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PreFlatteningMaterial)) return false;
            PreFlatteningMaterial that = (PreFlatteningMaterial)o;
            return this.id == that.id && this.subId == that.subId;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }
}
