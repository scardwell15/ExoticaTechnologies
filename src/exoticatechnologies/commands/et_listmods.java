package exoticatechnologies.commands;

import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.Map;

public class et_listmods implements BaseCommand {
    @Override
    public CommandResult runCommand(String argsString, CommandContext context) {
        if (context.isInCampaign() || context.isInMarket()) {
            Console.showMessage("Upgrades:");
            for (Map.Entry<String, Upgrade> upgrade : UpgradesHandler.UPGRADES.entrySet()) {
                Console.showMessage(String.format("%s -- %s", upgrade.getKey(), upgrade.getValue().getName()));
            }

            Console.showMessage("Exotics:");
            for (Map.Entry<String, Exotic> exotic : ExoticsHandler.EXOTICS.entrySet()) {
                Console.showMessage(String.format("%s -- %s", exotic.getKey(), exotic.getValue().getName()));
            }

            return CommandResult.SUCCESS;
        } else {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
    }
}
