package exoticatechnologies.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.types.ExoticType;
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
            if (args.length < 1) {
                Console.showMessage("et_addexotic <exoticId> [exoticType]");
                return CommandResult.BAD_SYNTAX;
            }

            try {
                String exoticKey = args[0];
                Exotic exotic = ExoticsHandler.EXOTICS.get(exoticKey);

                ExoticType exoticType = ExoticType.Companion.getNORMAL();
                if (args.length > 1) {
                    String exoticTypeString = args[1];
                    exoticType = ExoticType.Companion.valueOf(exoticTypeString);
                }

                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                CargoStackAPI stack = Utilities.getExoticChip(fleet.getCargo(), exotic.getKey(), exoticType.getNameKey());
                if (stack != null) {
                    stack.add(1);
                } else {
                    SpecialItemData data = exotic.getNewSpecialItemData(exoticType);
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
