
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.huwng.holospark.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.huwng.holospark.item.UltimateHoloUpgradeItem;
import net.huwng.holospark.item.SuperHoloUpgradeItem;
import net.huwng.holospark.item.EliteHoloUpgradeItem;
import net.huwng.holospark.item.BasicHoloUpgradeItem;
import net.huwng.holospark.item.AdvancedHoloUpgradeItem;
import net.huwng.holospark.item.AbsoluteHoloUpgradeItem;
import net.huwng.holospark.HolosparkMod;

public class HolosparkModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, HolosparkMod.MODID);
	public static final RegistryObject<Item> HOLO_CORE = block(HolosparkModBlocks.HOLO_CORE);
	public static final RegistryObject<Item> BASIC_HOLO_UPGRADE = REGISTRY.register("basic_holo_upgrade", () -> new BasicHoloUpgradeItem());
	public static final RegistryObject<Item> ADVANCED_HOLO_UPGRADE = REGISTRY.register("advanced_holo_upgrade", () -> new AdvancedHoloUpgradeItem());
	public static final RegistryObject<Item> ELITE_HOLO_UPGRADE = REGISTRY.register("elite_holo_upgrade", () -> new EliteHoloUpgradeItem());
	public static final RegistryObject<Item> SUPER_HOLO_UPGRADE = REGISTRY.register("super_holo_upgrade", () -> new SuperHoloUpgradeItem());
	public static final RegistryObject<Item> ULTIMATE_HOLO_UPGRADE = REGISTRY.register("ultimate_holo_upgrade", () -> new UltimateHoloUpgradeItem());
	public static final RegistryObject<Item> ABSOLUTE_HOLO_UPGRADE = REGISTRY.register("absolute_holo_upgrade", () -> new AbsoluteHoloUpgradeItem());

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
