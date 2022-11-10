package exoticatechnologies.modifications.upgrades.impl;

import exoticatechnologies.modifications.stats.impl.logistics.CrewSalaryEffect;
import org.json.JSONObject;

import java.util.Map;

//todo - remove this class for next release.
@Deprecated
public class CommissionedCrews {
    public String key;
    public String name;
    public String description;
    public JSONObject upgradeSettings;
    public Map<String, Float> resourceRatios;
    public float bandwidthUsage;

    public static class CommissionedSalaryListener extends CrewSalaryEffect.SalaryListener {

    }
}
