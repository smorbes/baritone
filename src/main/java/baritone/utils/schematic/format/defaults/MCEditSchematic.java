/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils.schematic.format.defaults;

import baritone.utils.schematic.StaticSchematic;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Brady
 * @since 12/27/2019
 */
public final class MCEditSchematic extends StaticSchematic {

    public MCEditSchematic(CompoundTag schematic) {
        String type = schematic.getString("Materials").orElseThrow();
        if (!type.equals("Alpha")) {
            throw new IllegalStateException("bad schematic " + type);
        }
        this.x = schematic.getInt("Width").orElse(0);
        this.y = schematic.getInt("Height").orElse(0);
        this.z = schematic.getInt("Length").orElse(0);
        byte[] blocks = schematic.getByteArray("Blocks").orElseThrow();
//        byte[] metadata = schematic.getByteArray("Data");

        byte[] additional = null;
        if (schematic.contains("AddBlocks")) {
            byte[] addBlocks = schematic.getByteArray("AddBlocks").orElseThrow();
            additional = new byte[addBlocks.length * 2];
            for (int i = 0; i < addBlocks.length; i++) {
                additional[i * 2 + 0] = (byte) ((addBlocks[i] >> 4) & 0xF); // lower nibble
                additional[i * 2 + 1] = (byte) ((addBlocks[i] >> 0) & 0xF); // upper nibble
            }
        }
        this.states = new BlockState[this.x][this.z][this.y];
        for (int y = 0; y < this.y; y++) {
            for (int z = 0; z < this.z; z++) {
                for (int x = 0; x < this.x; x++) {
                    int blockInd = (y * this.z + z) * this.x + x;

                    int blockID = blocks[blockInd] & 0xFF;
                    if (additional != null) {
                        // additional is 0 through 15 inclusive since it's & 0xF above
                        blockID |= additional[blockInd] << 8;
                    }
                    ResourceLocation blockKey = ResourceLocation.tryParse(ItemIdFix.getItem(blockID));
                    Block block = blockKey == null
                            ? Blocks.AIR
                            : BuiltInRegistries.BLOCK.get(blockKey)
                            .map(Holder.Reference::value)
                            .orElse(Blocks.AIR);

//                    int meta = metadata[blockInd] & 0xFF;
//                    this.states[x][z][y] = block.getStateFromMeta(meta);
                    this.states[x][z][y] = block.defaultBlockState();
                }
            }
        }
    }
}