package com.swornhero.steward;

import com.swornhero.steward.module.staffmode.StaffModeModule;
import com.swornhero.steward.core.command.StewardCommands;
import com.swornhero.steward.core.permission.PermissionRegistrationService;
import com.swornhero.steward.core.input.TextPromptService;
import com.swornhero.steward.core.player.KnownPlayerService;
import com.swornhero.steward.module.freeze.FreezeModule;
import com.swornhero.steward.module.punishment.PunishmentModule;
import com.swornhero.steward.module.warning.WarningModule;
import com.swornhero.steward.module.vanish.VanishModule;
import com.swornhero.steward.module.staffchat.StaffChatModule;
import com.swornhero.steward.module.report.ReportModule;
import com.swornhero.steward.module.notes.StaffNoteModule;
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
		PermissionRegistrationService.register();
		TextPromptService.register();
		KnownPlayerService.register();

		FreezeModule.register();
		WarningModule.register();
		StaffChatModule.register();
		PunishmentModule.register();
		StaffModeModule.register();
		VanishModule.register();
		ReportModule.register();
		StaffNoteModule.register();

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
