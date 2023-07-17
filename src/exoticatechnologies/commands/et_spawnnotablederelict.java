package exoticatechnologies.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.ScanUtils;
import exoticatechnologies.config.FactionConfigLoader;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.ExoticsGenerator;
import exoticatechnologies.modifications.upgrades.UpgradesGenerator;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;

public class et_spawnnotablederelict implements BaseCommand {
    @Override
    public CommandResult runCommand(String argsString, CommandContext context) {
        if (context.isInCampaign() || context.isInMarket()) {

            ShipRecoverySpecial.PerShipData perShipData = new ShipRecoverySpecial.PerShipData(getRandomVariantId(), ShipRecoverySpecial.ShipCondition.PRISTINE);

            FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, perShipData.getVariant());
            perShipData.fleetMemberId = member.getId();

            ShipModifications mods = ShipModFactory.generateRandom(member, Factions.HEGEMONY);
            ShipModFactory.GenerationContext generationContext =
                    new ShipModFactory.GenerationContext(member, mods, Factions.HEGEMONY, 100f, 100f);

            ExoticsGenerator.generate(member, mods, generationContext);
            UpgradesGenerator.generate(member, mods, generationContext);

            ShipModLoader.set(member, member.getVariant(), mods);

            DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(perShipData, false);
            SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(Global.getSector().getPlayerFleet().getContainingLocation(), Entities.WRECK, Factions.NEUTRAL, params);
            ship.setDiscoverable(true);
            ship.getLocation().set(Global.getSector().getPlayerFleet().getLocation());

            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));

            return CommandResult.SUCCESS;
        } else {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
    }

    private static String getRandomVariantId() {
        List<String> variantIds = new ArrayList<>(Global.getSector().getFaction(Factions.HEGEMONY).getVariantsForRole(ShipRoles.COMBAT_LARGE));
        return variantIds.get(Misc.random.nextInt(variantIds.size()));
    }
}
