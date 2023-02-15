package exoticatechnologies.modifications.exotics;

import lombok.extern.log4j.Log4j;

import java.util.*;

@Log4j
public class ETExotics {
    private List<String> exotics = null;
    private final Map<String, ExoticData> exoticData = new HashMap<>();

    public ETExotics() {
        fixExoticsList();
    }

    public ETExotics(List<String> exotics) {
        for(String key : exotics) {
            this.putExotic(key);
        }
    }

    public Collection<String> getList() {
        fixExoticsList();
        return new ArrayList<>(exoticData.keySet());
    }

    public ExoticData getData(Exotic exotic) {
        return exoticData.get(exotic.getKey());
    }

    public boolean hasExotic(Exotic exotic) {
        return this.hasExotic(exotic.getKey());
    }

    public boolean hasExotic(String key) {
        fixExoticsList();
        return this.exoticData.containsKey(key);
    }

    public void putExotic(Exotic exotic) {
        this.putExotic(exotic.getKey());
    }

    public void putExotic(String key) {
        this.exoticData.put(key, new ExoticData(key));
    }

    public void removeExotic(String key) {
        this.exoticData.remove(key);
    }

    public void removeExotic(Exotic exotic) {
        this.exoticData.remove(exotic.getKey());
    }

    public boolean hasAnyExotic() {
        return !this.exoticData.isEmpty();
    }

    public List<String> getTags() {
        Set<String> tagSet = new HashSet<>();
        for (String key : exoticData.keySet()) {
            Exotic exotic = ExoticsHandler.EXOTICS.get(key);
            if (exotic.getTag() != null) {
                tagSet.add(exotic.getTag());
            }
        }

        List<String> tags = new ArrayList<>(tagSet);
        return tags;
    }

    public List<Exotic> getConflicts(String tag) {
        List<Exotic> exotics = new ArrayList<>();
        for (String key : this.exoticData.keySet()) {
            Exotic exotic = ExoticsHandler.EXOTICS.get(key);
            if (exotic.getTag() != null && exotic.getTag().equals(tag)) {
                exotics.add(exotic);
            }
        }
        return exotics;
    }

    public void fixExoticsList() {
        if (this.exotics != null) {
            for (String exoticKey : this.exotics) {
                this.exoticData.put(exoticKey, new ExoticData(exoticKey));
            }
            this.exotics = null;
        }
    }

    @Override
    public String toString() {
        return "ETExotics{" +
                "exotics=" + exotics +
                '}';
    }
}
