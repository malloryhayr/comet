package dev.igalaxy.comet.mixin;

import dev.igalaxy.comet.modules.discord.CometDiscordModule;
import dev.igalaxy.comet.modules.hud.CometHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onScoreboardPlayerUpdate", at = @At("HEAD"))
	private void comet$onScoreboardPlayerUpdate(ScoreboardPlayerUpdateS2CPacket packet, CallbackInfo ci) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player != null && packet.getPlayerName().equals(player.getName().getString())) {
			String name = packet.getObjectiveName();
			if (name.equals("exi_playtime_h") || name.equals("exi_playtime_c")) {
				CometHudModule.INSTANCE.setPlaytimeCheck(System.currentTimeMillis());
				if (name.equals("exi_playtime_h")) {
					CometHudModule.INSTANCE.setPlaytimeHours(packet.getScore());
				} else {
					CometHudModule.INSTANCE.setPlaytimeSeconds(packet.getScore());
				}
			}
		}
	}

	@Inject(method = "onGameJoin", at = @At("TAIL"))
	private void comet$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		System.out.println(this.client.getCurrentServerEntry());
		if (this.client.getCurrentServerEntry() != null) {
			CometDiscordModule.INSTANCE.updateServer(this.client.getCurrentServerEntry());
		}
	}

	@Inject(method = "disconnect", at = @At("TAIL"))
	private void comet$disconnect(CallbackInfo ci) {
		CometDiscordModule.INSTANCE.clientReady();
	}
}
