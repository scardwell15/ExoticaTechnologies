package exoticatechnologies.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.impl.campaign.skills.FluxRegulation;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.List;

public class MySkill extends FluxRegulation {
    private static String MY_SKILL_KEY = "guerillaSkillKey";
    private static int UNMODIFIED_DP_THRESHOLD = 150;

    public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {

        protected float computeAndCacheUnmodifiedDPBonus(FleetDataAPI data, MutableCharacterStatsAPI cStats,
                                                      String key, float maxBonus) {
            if (data == null) return maxBonus;
            if (cStats.getFleet() == null) return maxBonus;

            Float bonus = (Float) data.getCacheClearedOnSync().get(key);
            if (bonus != null) return bonus;

            float currValue = getTotalUnmodifiedCombatOP(data, cStats);
            float threshold = UNMODIFIED_DP_THRESHOLD;

            bonus = getThresholdBasedRoundedBonus(maxBonus, currValue, threshold);
            data.getCacheClearedOnSync().put(key, bonus);
            return bonus;
        }

        public static float getTotalUnmodifiedCombatOP(FleetDataAPI data, MutableCharacterStatsAPI stats) {
            float op = 0;
            for (FleetMemberAPI curr : data.getMembersListCopy()) {
                if (curr.isMothballed()) continue;
                if (isCivilian(curr)) continue;
                op += getUnmodifiedDP(curr, stats);
            }
            return Math.round(op);
        }

        public static float getUnmodifiedDP(FleetMemberAPI member, MutableCharacterStatsAPI stats) {
            return member.getHullSpec().getSuppliesToRecover();
        }

        public FleetTotalItem getUnmodifiedDPTotal() {
            final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
            FleetTotalItem item = new FleetTotalItem();
            item.label = "Combat ships";
            item.value = "" + (int) getTotalUnmodifiedCombatOP(fleet.getFleetData(), stats);
            item.sortOrder = 100;

            item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
                public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara("The total unmodified deployment points of all the combat ships in your fleet.", 0f);
                }
                public List<FleetMemberPointContrib> getContributors() {
                    return getTotalUnmodifiedDPDetail(fleet.getFleetData(), stats);
                }
            });

            return item;
        }

        public static List<FleetMemberPointContrib> getTotalUnmodifiedDPDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
            List<FleetMemberPointContrib> result = new ArrayList<FleetMemberPointContrib>();
            for (FleetMemberAPI curr : data.getMembersListCopy()) {
                if (curr.isMothballed()) continue;
                if (isCivilian(curr)) continue;
                int pts = (int) Math.round(getUnmodifiedDP(curr, stats));
                result.add(new FleetMemberPointContrib(curr, pts));
            }
            return result;
        }

        @Override
        public FleetTotalItem getFleetTotalItem() {
            return getUnmodifiedDPTotal();
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

        }
    }

}