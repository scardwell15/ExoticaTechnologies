package exoticatechnologies.modifications.exotics;

import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j
public class ETExotics {
    private final List<String> exotics;

    public ETExotics() {
        this.exotics = new ArrayList<>();
    }

    public ETExotics(List<String> exotics) {
        if (exotics == null) {
            this.exotics = new ArrayList<>();
        } else {
            this.exotics = exotics;
        }
    }

    public List<String> getList() {
        return exotics;
    }

    public boolean hasExotic(Exotic exotic) {
        return this.hasExotic(exotic.getKey());
    }

    public boolean hasExotic(String key) {
        return this.exotics.contains(key);
    }

    public void putExotic(Exotic exotic) {
        this.putExotic(exotic.getKey());
    }

    public void putExotic(String key) {
        this.exotics.add(key);
    }

    public void removeExotic(String key) {
        this.exotics.remove(key);
    }

    public void removeExotic(Exotic exotic) {
        this.exotics.remove(exotic.getKey());
    }

    public boolean hasAnyExotic() {
        return !this.exotics.isEmpty();
    }

    public List<String> getTags() {
        Set<String> tagSet = new HashSet<>();
        for (String key : exotics) {
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
        for (String key : this.exotics) {
            Exotic exotic = ExoticsHandler.EXOTICS.get(key);
            if (exotic.getTag() != null && exotic.getTag().equals(tag)) {
                exotics.add(exotic);
            }
        }
        return exotics;
    }

    @Override
    public String toString() {
        return "ETExotics{" +
                "exotics=" + exotics +
                '}';
    }
}
