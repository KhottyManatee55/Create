package com.simibubi.create.foundation.behaviour.inventory;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

public class SynchronizedExtraction {

	static boolean extractSynchronized(IEnviromentBlockReader reader, BlockPos inventoryPos) {
		List<SingleTargetAutoExtractingBehaviour> actors = getAllSyncedExtractors(reader, inventoryPos);
		int startIndex = actors.size() - 1;
		boolean success = false;

		for (; startIndex > 0; startIndex--)
			if (actors.get(startIndex).advantageOnNextSync)
				break;
		for (int i = 0; i < actors.size(); i++)
			success |= actors.get((startIndex + i) % actors.size()).extractFromInventory();

		if (success) {
			actors.get(startIndex).advantageOnNextSync = false;
			actors.get((startIndex + 1) % actors.size()).advantageOnNextSync = true;
		}

		return success;
	}

	private static List<SingleTargetAutoExtractingBehaviour> getAllSyncedExtractors(IEnviromentBlockReader reader,
			BlockPos inventoryPos) {
		List<SingleTargetAutoExtractingBehaviour> list = new ArrayList<>();
		List<BlockPos> inventoryPositions = new ArrayList<>();
		inventoryPositions.add(inventoryPos);

		// Sync across double chests
		BlockState blockState = reader.getBlockState(inventoryPos);
		if (blockState.getBlock() instanceof ChestBlock)
			if (blockState.get(ChestBlock.TYPE) != ChestType.SINGLE)
				inventoryPositions.add(inventoryPos.offset(ChestBlock.getDirectionToAttached(blockState)));

		for (BlockPos pos : inventoryPositions) {
			for (Direction direction : Direction.values()) {
				SingleTargetAutoExtractingBehaviour behaviour = TileEntityBehaviour.get(reader, pos.offset(direction),
						SingleTargetAutoExtractingBehaviour.TYPE);
				if (behaviour == null)
					continue;
				if (!behaviour.synced)
					continue;
				if (behaviour.shouldPause.get())
					continue;
				if (!behaviour.shouldExtract.get())
					continue;

				list.add(behaviour);
			}
		}
		return list;
	}

}