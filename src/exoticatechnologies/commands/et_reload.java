package exoticatechnologies.commands;

import exoticatechnologies.ETModSettings;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class et_reload implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context.isInCampaign()) {

            ETModSettings.loadModSettings();
            UpgradesHandler.loadConfigs();
            ExoticsHandler.loadConfigs();

            return CommandResult.SUCCESS;
        } else {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
    }
}
