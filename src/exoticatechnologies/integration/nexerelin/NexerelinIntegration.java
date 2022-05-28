package exoticatechnologies.integration.nexerelin;

import com.fs.starfarer.api.Global;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.SectorManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NexerelinIntegration {
    public static boolean isEnabled() {
        return Global.getSettings().getModManager().isModEnabled("nexerelin");
    }

    public static boolean isInAlliance(String faction) {
        if (!isEnabled()) return false;

        return AllianceManager.getPlayerAlliance(true) == AllianceManager.getFactionAlliance("faction");
    }

    public static boolean isHardMode() {
        if (!isEnabled()) return false;

        return SectorManager.getManager().isHardMode();
    }
}
