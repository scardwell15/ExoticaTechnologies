package exoticatechnologies.modifications.exotics;

import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;

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
        if (this.exotics.contains(key)) {
            return true;
        }
        return false;
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

    @Override
    public String toString() {
        return "ETExotics{" +
                "exotics=" + exotics +
                '}';
    }
}
