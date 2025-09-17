package net.huwng.holospark.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

import net.huwng.holospark.block.entity.HoloCoreBlockEntity;
import net.huwng.holospark.init.HolosparkModBlockEntities;
import net.huwng.holospark.init.HolosparkModItems;

public class HoloCoreBlock extends Block implements EntityBlock {
    public HoloCoreBlock() {
        super(BlockBehaviour.Properties
                .of()
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.METAL)
                .strength(2f)
                .requiresCorrectToolForDrops()
        );
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return 15;
    }
    
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
	    if (level.isClientSide) return InteractionResult.SUCCESS;
	    ItemStack stack = player.getItemInHand(hand);
	    var be = level.getBlockEntity(pos);
	    if (!(be instanceof HoloCoreBlockEntity)) return InteractionResult.PASS;
	    HoloCoreBlockEntity core = (HoloCoreBlockEntity) be;
	
	    // If player holds Basic upgrade
	    if (!stack.isEmpty() && stack.getItem() == HolosparkModItems.BASIC_HOLO_UPGRADE.get()) {
	        if (!player.isCreative()) stack.shrink(1);
	        core.applyUpgrade(level, pos, state, "basic");
	        return InteractionResult.CONSUME;
	    }

	    if (!stack.isEmpty() && stack.getItem() == HolosparkModItems.ADVANCED_HOLO_UPGRADE.get()) {
	        if (!player.isCreative()) stack.shrink(1);
	        core.applyUpgrade(level, pos, state, "advanced");
	        return InteractionResult.CONSUME;
	    }

	    if (!stack.isEmpty() && stack.getItem() == HolosparkModItems.ELITE_HOLO_UPGRADE.get()) {
	        if (!player.isCreative()) stack.shrink(1);
	        core.applyUpgrade(level, pos, state, "elite");
	        return InteractionResult.CONSUME;
	    }
	    
	    if (!stack.isEmpty() && stack.getItem() == HolosparkModItems.SUPER_HOLO_UPGRADE.get()) {
	        if (!player.isCreative()) stack.shrink(1);
	        core.applyUpgrade(level, pos, state, "super");
	        return InteractionResult.CONSUME;
	    }
	    
	    if (!stack.isEmpty() && stack.getItem() == HolosparkModItems.ULTIMATE_HOLO_UPGRADE.get()) {
	        if (!player.isCreative()) stack.shrink(1);
	        core.applyUpgrade(level, pos, state, "ultimate");
	        return InteractionResult.CONSUME;
	    }
	    
	    if (!stack.isEmpty() && stack.getItem() == HolosparkModItems.ABSOLUTE_HOLO_UPGRADE.get()) {
	        if (!player.isCreative()) stack.shrink(1);
	        core.applyUpgrade(level, pos, state, "absolute");
	        return InteractionResult.CONSUME;
	    }
	    // Empty hand: toggle on/off
	    if (stack.isEmpty()) {
	        core.togglePower(level, pos, state);
	        return InteractionResult.SUCCESS;
	    }
	
	    return InteractionResult.PASS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
	    return new HoloCoreBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
	    return type == HolosparkModBlockEntities.HOLO_CORE.get()
	            ? (lvl, pos, st, be) -> HoloCoreBlockEntity.tick(lvl, pos, st, (HoloCoreBlockEntity) be)
	            : null;
	}
}
