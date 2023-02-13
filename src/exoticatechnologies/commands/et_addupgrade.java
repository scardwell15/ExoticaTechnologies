package exoticatechnologies.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.util.Utilities;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class et_addupgrade implements BaseCommand {
    @Override
    public CommandResult runCommand(String argsString, CommandContext context) {
        if (context.isInCampaign() || context.isInMarket()) {
            String[] args = argsString.split(" ");
            if (args.length != 2) {
                Console.showMessage("et_addupgrade <upgradeId> <level>");
                return CommandResult.BAD_SYNTAX;
            }

            try {
                String upgradeKey = args[0];
                int level = Integer.parseInt(args[1]);
                Upgrade upgrade = UpgradesHandler.UPGRADES.get(upgradeKey);

                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                CargoStackAPI stack = Utilities.getUpgradeChip(fleet.getCargo(), upgrade.getKey(), level);
                if (stack != null) {
                    stack.add(1);
                } else {
                    SpecialItemData data = upgrade.getNewSpecialItemData(level);
                    fleet.getCargo().addSpecial(data, 1);
                }

                return CommandResult.SUCCESS;
            } catch (NumberFormatException ex) {
                Console.showMessage("Second argument must be a Integer greater than 1.");
                return CommandResult.ERROR;
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
