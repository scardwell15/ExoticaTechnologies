package exoticatechnologies.campaign.listeners;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import lombok.extern.log4j.Log4j;

import java.util.*;

@Log4j
public class SalvageListener implements ShowLootListener {
    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        SectorEntityToken entity = dialog.getInteractionTarget();

        List<SalvageEntityGenDataSpec.DropData> dropData = getDropDataFromEntity(entity);

        MemoryAPI memory = entity.getMemoryWithoutUpdate();
        long randomSeed = memory.getLong(MemFlags.SALVAGE_SEED);
        Random random = Misc.getRandom(randomSeed, 100);

        List<SalvageEntityGenDataSpec.DropData> dropValue = generateDropValueList(dropData);
        List<SalvageEntityGenDataSpec.DropData> dropRandom = generateDropRandomList(dropData);

        CargoAPI salvage = SalvageEntity.generateSalvage(random,
                1f, 1f, 1f, 1f, dropValue, dropRandom);
        loot.addAll(salvage);
    }

    private static List<SalvageEntityGenDataSpec.DropData> getDropDataFromEntity(SectorEntityToken entity) {
        List<SalvageEntityGenDataSpec.DropData> dropData = new ArrayList<>();

        //first get drops assigned directly to entity
        if (entity.getDropRandom() != null) {
            dropData.addAll(entity.getDropRandom());
        }

        if (entity.getDropValue() != null) {
            dropData.addAll(entity.getDropValue());
        }

        //then try to get spec from entity and the spec's drops
        String specId = entity.getCustomEntityType();
        if (specId == null || entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID_OVERRIDE)) {
            specId = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID_OVERRIDE);
        }

        if (specId != null
                && SalvageEntityGeneratorOld.hasSalvageSpec(specId)) {
            SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(specId);

            //get drop randoms from that spec
            if (spec != null && spec.getDropRandom() != null) {
                dropData.addAll(spec.getDropRandom());
            }

            if (spec != null && spec.getDropValue() != null) {
                dropData.addAll(spec.getDropValue());
            }
        }

        return dropData;
    }

    private static List<SalvageEntityGenDataSpec.DropData> generateDropValueList(List<SalvageEntityGenDataSpec.DropData> dropData) {
        List<SalvageEntityGenDataSpec.DropData> dropValueList = new ArrayList<>();

        //iterate through drop groups to find groups that should add drops
        for (SalvageEntityGenDataSpec.DropData data : dropData) {
            if (data.group == null) continue;
            if (data.value == -1) continue;

            int value = -1;
            //rare_tech is more valuable tech-wise than rare_tech_low
            if (data.group.equals("rare_tech")) {
                value = Math.round(data.value * 0.3f);
            } else if (data.group.equals("rare_tech_low")) {
                value = Math.round(data.value * 0.1f);
            }

            if(value != -1) {
                SalvageEntityGenDataSpec.DropData dropValue = new SalvageEntityGenDataSpec.DropData();
                dropValue.group = "et_exotic";
                dropValue.valueMult = data.valueMult;
                dropValue.value = value;

                dropValueList.add(dropValue);
            }
        }

        return dropValueList;
    }

    private static List<SalvageEntityGenDataSpec.DropData> generateDropRandomList(List<SalvageEntityGenDataSpec.DropData> dropData) {
        List<SalvageEntityGenDataSpec.DropData> dropRandomList = new ArrayList<>();

        //iterate through drop groups to find groups that should add drops
        for (SalvageEntityGenDataSpec.DropData data : dropData) {
            if (data.group == null) continue;
            if (data.chances == -1) continue;

            int chances = -1;
            //rare_tech is more valuable tech-wise than rare_tech_low
            if (data.group.equals("rare_tech")) {
                chances = Math.round(data.chances * 1.5f);
            } else if (data.group.equals("rare_tech_low")) {
                chances = Math.round(data.chances * 0.5f);
            }

            if(chances != -1) {
                SalvageEntityGenDataSpec.DropData dropRandom = new SalvageEntityGenDataSpec.DropData();
                dropRandom.group = "et_exotic";
                dropRandom.maxChances = data.maxChances;
                dropRandom.chances = chances;

                dropRandomList.add(dropRandom);
            }
        }

        return dropRandomList;
    }
}
