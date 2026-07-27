package com.swornhero.steward;

import com.swornhero.steward.command.StewardCommands;
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

		StewardCommands.register();

		LOGGER.info("{} commands registered.", MOD_NAME);
	}
}