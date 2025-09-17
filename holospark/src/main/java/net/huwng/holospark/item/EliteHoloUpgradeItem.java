
package net.huwng.holospark.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class EliteHoloUpgradeItem extends Item {
	public EliteHoloUpgradeItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
	}
}
