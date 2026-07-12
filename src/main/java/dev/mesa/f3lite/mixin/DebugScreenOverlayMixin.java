package dev.mesa.f3lite.mixin;

import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private Font font;

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void f3lite$replaceDebugScreen(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		if (!this.shouldRenderF3Lite()) {
			return;
		}

		List<String> leftLines = this.f3lite$buildLeftLines();
		List<String> rightLines = this.f3lite$buildRightLines();

		this.f3lite$extractLines(graphics, leftLines, true);
		this.f3lite$extractLines(graphics, rightLines, false);

		ci.cancel();
	}

	private boolean shouldRenderF3Lite() {
		if (!this.minecraft.isGameLoadFinished()) {
			return false;
		}

		if (this.minecraft.gui.hud.isHidden() && this.minecraft.gui.screen() == null) {
			return false;
		}

		return this.minecraft.debugEntries.isOverlayVisible()
				|| !this.minecraft.debugEntries.getCurrentlyEnabled().isEmpty();
	}

	private List<String> f3lite$buildLeftLines() {
		List<String> lines = new ArrayList<>();

		if (this.minecraft.player == null || this.minecraft.level == null) {
			lines.add("F3 Lite");
			return lines;
		}

		BlockPos blockPos = this.minecraft.player.blockPosition();
		ChunkPos chunkPos = this.minecraft.player.chunkPosition();
		Direction facing = this.minecraft.player.getDirection();

		double x = this.minecraft.player.getX();
		double y = this.minecraft.player.getY();
		double z = this.minecraft.player.getZ();

		int blockLight = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockPos);
		int skyLight = this.minecraft.level.getBrightness(LightLayer.SKY, blockPos);

		String biome = this.minecraft.level.getBiome(blockPos)
				.unwrapKey()
				.map(key -> key.identifier().toString())
				.orElse("unknown");

		String dimension = this.minecraft.level.dimension().identifier().toString();

		lines.add("F3 Lite-est");

		return lines;
	}

	private List<String> f3lite$buildRightLines() {
		List<String> lines = new ArrayList<>();

		if (this.minecraft.player == null || this.minecraft.level == null) {
			return lines;
		}

		return lines;
	}

	private String f3lite$getMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();

		long used = runtime.totalMemory() - runtime.freeMemory();
		long max = runtime.maxMemory();

		long usedMb = used / 1024L / 1024L;
		long maxMb = max / 1024L / 1024L;

		return usedMb + " / " + maxMb + " MB";
	}

	private int f3lite$getPing() {
		if (this.minecraft.player == null || this.minecraft.getConnection() == null) {
			return 0;
		}

		var info = this.minecraft.getConnection().getPlayerInfo(this.minecraft.player.getUUID());

		if (info == null) {
			return 0;
		}

		return info.getLatency();
	}

	private String f3lite$formatDirection(Direction direction) {
		return switch (direction) {
			case NORTH -> "North";
			case SOUTH -> "South";
			case EAST -> "East";
			case WEST -> "West";
			case UP -> "Up";
			case DOWN -> "Down";
		};
	}

	private void f3lite$extractLines(GuiGraphicsExtractor graphics, List<String> lines, boolean alignLeft) {
		int height = 9;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			if (!Strings.isNullOrEmpty(line)) {
				int width = this.font.width(line);
				int left = alignLeft ? 2 : graphics.guiWidth() - 2 - width;
				int top = 2 + height * i;

				graphics.fill(
						left - 1,
						top - 1,
						left + width + 1,
						top + height - 1,
						-1873784752
				);
			}
		}

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			if (!Strings.isNullOrEmpty(line)) {
				int width = this.font.width(line);
				int left = alignLeft ? 2 : graphics.guiWidth() - 2 - width;
				int top = 2 + height * i;

				graphics.text(
						this.font,
						line,
						left,
						top,
						-2039584,
						false
				);
			}
		}
	}
}
