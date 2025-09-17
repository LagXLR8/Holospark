
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.huwng.holospark.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.huwng.holospark.block.HoloCoreBlock;
import net.huwng.holospark.HolosparkMod;

public class HolosparkModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, HolosparkMod.MODID);
	public static final RegistryObject<Block> HOLO_CORE = REGISTRY.register("holo_core", () -> new HoloCoreBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
