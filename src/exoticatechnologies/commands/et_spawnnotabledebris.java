package exoticatechnologies.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.campaign.ScanUtils;
import exoticatechnologies.config.FactionConfigLoader;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.modifications.exotics.ExoticsGenerator;
import exoticatechnologies.modifications.upgrades.UpgradesGenerator;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;

public class et_spawnnotabledebris implements BaseCommand {
    @Override
    public CommandResult runCommand(String argsString, CommandContext context) {
        if (context.isInCampaign() || context.isInMarket()) {
            DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    350f, // field radius - should not go above 1000 for performance reasons
                    1f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days
                    0f); // days the field will keep generating glowing pieces
            params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params.baseSalvageXP = 350; // base XP for scavenging in field

            SectorEntityToken debris = Misc.addDebrisField(Global.getSector().getPlayerFleet().getContainingLocation(), params, Misc.random);
            debris.setOrbit(null);
            debris.getLocation().set(Global.getSector().getPlayerFleet().getLocation());

            ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData("An ancient debris field that appeared before you.");

            ShipRecoverySpecial.PerShipData perShipData = new ShipRecoverySpecial.PerShipData(getRandomVariantId(), ShipRecoverySpecial.ShipCondition.PRISTINE);

            FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, perShipData.getVariant());
            perShipData.fleetMemberId = member.getId();

            ShipModifications mods = ShipModFactory.generateRandom(member, Factions.HEGEMONY);
            ShipModFactory.GenerationContext generationContext =
                    new ShipModFactory.GenerationContext(member, mods, Factions.HEGEMONY, 100f, 100f);

            ExoticsGenerator.generate(member, mods, generationContext);
            UpgradesGenerator.generate(member, mods, generationContext);

            ShipModLoader.set(member, member.getVariant(), mods);
            data.addShip(perShipData);
            debris.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, data);
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
