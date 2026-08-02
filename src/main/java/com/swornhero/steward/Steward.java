package com.swornhero.steward;

import com.swornhero.steward.core.command.StewardCommands;
import com.swornhero.steward.module.freeze.FreezeModule;
import com.swornhero.steward.module.punishment.PunishmentModule;
import com.swornhero.steward.module.warning.WarningModule;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Steward implements ModInitializer {

	public static final String MOD_ID =
			"steward";

	public static final String MOD_NAME =
			"Steward";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_NAME);

	@Override
	public void onInitialize() {
		LOGGER.info(
				"{} is initializing.",
				MOD_NAME
		);

		StewardCommands.register();

		FreezeModule.register();
		WarningModule.register();
		PunishmentModule.register();

		LOGGER.info(
				"{} commands registered.",
				MOD_NAME
		);

		LOGGER.info(
				"{} modules registered.",
				MOD_NAME
		);
	}
}