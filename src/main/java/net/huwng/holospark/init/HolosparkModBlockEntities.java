
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.huwng.holospark.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

import net.huwng.holospark.block.entity.HoloCoreBlockEntity;
import net.huwng.holospark.HolosparkMod;

public class HolosparkModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HolosparkMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> HOLO_CORE = register("holo_core", HolosparkModBlocks.HOLO_CORE, HoloCoreBlockEntity::new);

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
