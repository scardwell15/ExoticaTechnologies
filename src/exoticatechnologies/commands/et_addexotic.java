package exoticatechnologies.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.util.Utilities;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class et_addexotic implements BaseCommand {
    @Override
    public CommandResult runCommand(String argsString, CommandContext context) {
        if (context.isInCampaign() || context.isInMarket()) {
            String[] args = argsString.split(" ");
            if (args.length != 1) {
                Console.showMessage("et_addexotic <exoticId>");
                return CommandResult.BAD_SYNTAX;
            }

            try {
                String upgradeKey = args[0];
                Exotic exotic = ExoticsHandler.EXOTICS.get(upgradeKey);

                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                CargoStackAPI stack = Utilities.getExoticChip(fleet.getCargo(), exotic.getKey());
                if (stack != null) {
                    stack.add(1);
                } else {
                    SpecialItemData data = exotic.getNewSpecialItemData();
                    fleet.getCargo().addSpecial(data, 1);
                }

                return CommandResult.SUCCESS;
            } catch (Throwable ex) {
                Console.showMessage("Caught exception, see starsector.log");
                ex.printStackTrace();
                return CommandResult.ERROR;
            }
        } else {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
    }
}