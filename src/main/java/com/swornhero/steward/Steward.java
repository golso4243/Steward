package com.swornhero.steward;

import com.swornhero.steward.command.StewardCommands;
import com.swornhero.steward.freeze.PendingNotificationService;
import com.swornhero.steward.freeze.FreezeService;
import com.swornhero.steward.freeze.FreezeProtectionService;
import com.swornhero.steward.freeze.FreezeConnectionService;
import com.swornhero.steward.freeze.FreezeHistoryService;
import com.swornhero.steward.config.FreezePolicyService;
import com.swornhero.steward.freeze.FreezeDamageService;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Steward implements ModInitializer {
	public static final String MOD_ID = "steward";
	public static final String MOD_NAME = "Steward";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	@Override
	public void onInitialize() {
		LOGGER.info("{} is initializing.", MOD_NAME);

		FreezePolicyService.register();

		FreezeHistoryService.register();
		PendingNotificationService.register();

		StewardCommands.register();
		FreezeService.register();
		FreezeDamageService.register();
		FreezeProtectionService.register();
		FreezeConnectionService.register();

		LOGGER.info("{} commands registered.", MOD_NAME);
		LOGGER.info("{} freeze services registered.", MOD_NAME);
	}
}