package extrasystemreloaded.commands;

import extrasystemreloaded.Es_ModPlugin;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class esr_removeUpgradeCosts implements BaseCommand
{
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if ( context.isInCampaign() || context.isInMarket() )
        {
            Es_ModPlugin.setDebugUpgradeCosts(true);
            Console.showMessage("DEBUG_UPGRADES_REMOVE_COST flag set to "+Es_ModPlugin.isDebugUpgradeCosts());
            return CommandResult.SUCCESS;
        } else {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

    }
}
