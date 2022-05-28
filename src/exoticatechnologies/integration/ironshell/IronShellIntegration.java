package exoticatechnologies.integration.ironshell;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.integration.nexerelin.NexerelinIntegration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IronShellIntegration {
    public static boolean isEnabled() {
        return Global.getSettings().getModManager().isModEnabled("timid_xiv");
    }

    public static void setSalaryTax(float commissionSalary) {
        if ("hegemony".equals(Misc.getCommissionFactionId()) || "ironshell".equals(Misc.getCommissionFactionId()) || NexerelinIntegration.isInAlliance("ironshell")) {
            MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
            float wealthCap = NexerelinIntegration.isHardMode() ? 25000f : 50000f;
            float payrollTax = sectorMemory.getFloat("$EIS_PayrollTax");

            if (Math.abs(payrollTax) <= sectorMemory.getFloat("$EIS_WealthTax") + wealthCap) {

                float taxRefund = commissionSalary * (NexerelinIntegration.isHardMode() ? 0.05f : 0.1f);
                sectorMemory.set("$EIS_taxburden", sectorMemory.getFloat("$EIS_taxburden") - taxRefund);
                sectorMemory.set("$EIS_PayrollTax", payrollTax - taxRefund);
            }
        }
    }
}
