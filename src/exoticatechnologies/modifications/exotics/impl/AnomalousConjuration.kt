package exoticatechnologies.modifications.exotics.impl

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect
import com.fs.starfarer.api.impl.combat.RiftLanceEffect
import com.fs.starfarer.api.impl.hullmods.ShardSpawner
import com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AnomalousConjuration(key: String, settings: JSONObject) : Exotic(key, settings) {
    override var color = Color(180, 255, 200)
    override var canDropFromCombat: Boolean = false

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    ) {
        if (expand) {
            StringUtils.getTranslation(key, "longDescription")
                .addToTooltip(tooltip, title)
        }
    }

    override fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI?): Boolean {
        return false
    }

    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return true
    }

    override fun canUseExoticType(type: ExoticType): Boolean {
        return false
    }

    override fun getGenerationChanceMult(member: FleetMemberAPI): Float {
        var spawnChance = 1f

        //increase spawn chance after cycle 305
        val currCycle = (Global.getSector().clock.cycle - 305).coerceAtLeast(0)
        spawnChance += 3f * (currCycle / 5f).coerceAtMost(1f)

        if (member.fleetData != null && member.fleetData.fleet != null) {
            when (member.fleetData.fleet.faction.id) {
                Factions.OMEGA -> spawnChance = 8f
                Factions.REMNANTS -> spawnChance = 2.5f * ((currCycle - 5f) / 5f).coerceIn(0f..1f) //max chance at cycle 310
            }
        }

        return spawnChance
    }

    override fun advanceInCombatUnpaused(
        ship: ShipAPI,
        amount: Float,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        if (Global.getCombatEngine().isPaused) {
            return
        }

        if (advanceReplacementInterval(ship, amount)) {
            val engine = Global.getCombatEngine()

            //effects
            val c = RiftLanceEffect.getColorForDarkening(RiftCascadeEffect.STANDARD_RIFT_COLOR)
            val baseDuration = 2f
            val vel = Vector2f(ship.velocity)
            val size = ship.collisionRadius * 0.35f
            for (i in 0..2) {
                var point: Vector2f? = Vector2f(ship.location)
                point = Misc.getPointWithinRadiusUniform(point, ship.collisionRadius * 0.5f, Misc.random)
                var dur = baseDuration + baseDuration * Math.random().toFloat()
                val pt = Misc.getPointWithinRadius(point, size * 0.5f)
                val v = Misc.getUnitVectorAtDegreeAngle(Math.random().toFloat() * 360f)
                v.scale(size + size * Math.random().toFloat() * 0.5f)
                v.scale(0.2f)
                Vector2f.add(vel, v, v)
                val maxSpeed = size * 1.5f * 0.2f
                val minSpeed = size * 1f * 0.2f
                val overMin = v.length() - minSpeed
                if (overMin > 0) {
                    var durMult = 1f - overMin / (maxSpeed - minSpeed)
                    if (durMult < 0.1f) durMult = 0.1f
                    dur *= 0.5f + 0.5f * durMult
                }
                engine.addNegativeNebulaParticle(
                    pt, v, size * 1f, 2f,
                    0.5f / dur, 0f, dur, c
                )
            }

            //spawning for fighters
            val angle = Math.random().toFloat() - 0.5f * 360
            val facing: Float = ship.facing + 15f * (Math.random().toFloat() - 0.5f)
            val typePicker = getTypePickerBasedOnLocalConditions(ship)
            val type = typePicker!!.pick()
            val variants = ShardSpawner.variantData[HullSize.FIGHTER]
            val variantPicker = variants!![type]
            var variantId = variantPicker.pick()
            Global.getSettings().getFighterWingSpec(variantId) ?: run {
                variantId = "aspect_shock_wing"
            }

            val loc = Misc.getUnitVectorAtDegreeAngle(angle)
            loc.scale(ship.collisionRadius * 0.1f)
            Vector2f.add(loc, ship.location, loc)
            val fleetManager: CombatFleetManagerAPI = engine.getFleetManager(ship.originalOwner)
            val wasSuppressed = fleetManager.isSuppressDeploymentMessages
            fleetManager.isSuppressDeploymentMessages = true

            val captain = Global.getSettings().createPerson()
            captain.setPersonality(Personalities.RECKLESS) // doesn't matter for fighters
            captain.stats.setSkillLevel(Skills.POINT_DEFENSE, 2f)
            captain.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
            captain.stats.setSkillLevel(Skills.IMPACT_MITIGATION, 2f)
            val leader: ShipAPI = engine.getFleetManager(ship.originalOwner)
                .spawnShipOrWing(variantId, loc, facing, 0f, captain)

            val ships = arrayOfNulls<ShipAPI>(leader.wing.wingMembers.size)
            for (i in ships.indices) {
                ships[i] = leader.wing.wingMembers[i]
                ships[i]!!.location.set(loc)
            }

            for (i in ships.indices) {
                ships[i]!!.cloneVariant()
                ships[i]!!.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
                if (Global.getCombatEngine().isInCampaign || Global.getCombatEngine().isInCampaignSim) {
                    val faction = Global.getSector().getFaction(Factions.OMEGA)
                    if (faction != null) {
                        val name = faction.pickRandomShipName()
                        ships[i]!!.name = name
                    }
                }
            }
            fleetManager.isSuppressDeploymentMessages = wasSuppressed

            var sourceMember = fleetManager.getDeployedFleetMemberFromAllEverDeployed(ship)
            val deployed = fleetManager.getDeployedFleetMemberFromAllEverDeployed(ships[0])
            if (sourceMember != null && deployed != null) {
                val map = fleetManager.shardToOriginalShipMap
                while (map.containsKey(sourceMember)) {
                    sourceMember = map[sourceMember]
                }
                if (sourceMember != null) {
                    map[deployed] = sourceMember
                }
            }
        }
    }

    private fun getTypePickerBasedOnLocalConditions(ship: ShipAPI): WeightedRandomPicker<ShardType>? {
        val engine = Global.getCombatEngine()
        val checkRadius = 5000f
        val iter = engine.aiGridShips.getCheckIterator(ship.location, checkRadius * 2f, checkRadius * 2f)
        var weightFighters = 0f
        var weightGoodShields = 0f
        var weightGoodArmor = 0f
        var weightVulnerable = 0f
        var weightCarriers = 0f
        var weightEnemies = 0f
        var weightFriends = 0f
        while (iter.hasNext()) {
            val o = iter.next()
            if (o is ShipAPI) {
                val other = o
                if (other.owner == Misc.OWNER_NEUTRAL) continue
                val enemy = ship.owner != other.owner
                if (enemy) {
                    if (other.isFighter || other.isDrone) {
                        weightFighters += 0.25f
                        weightEnemies += 0.25f
                    } else {
                        val w = Misc.getShipWeight(other)
                        weightEnemies += w
                        if (hasGoodShields(other)) {
                            weightGoodShields += w
                        }
                        if (hasGoodArmor(other)) {
                            weightGoodArmor += w
                        }
                        if (isVulnerableToMissileBarrage(ship, other)) {
                            weightVulnerable += w
                        }
                        if (other.variant.isCarrier) {
                            weightCarriers += w
                        }
                    }
                } else {
                    weightFriends += if (other.isFighter || other.isDrone) {
                        0.25f
                    } else {
                        val w = Misc.getShipWeight(other)
                        w
                    }
                }
            }
        }
        val picker = WeightedRandomPicker<ShardType>()
        var total = weightFighters + weightGoodShields + weightGoodArmor + weightVulnerable + weightCarriers
        if (total <= 1f) total = 1f
        var antiFighter = (weightFighters + weightCarriers) / total
        var antiShield = weightGoodShields / total
        var antiArmor = weightGoodArmor / total
        var missile = weightVulnerable / total
        val friends = weightFriends / Math.max(1f, weightEnemies + weightFriends)
        picker.add(ShardType.GENERAL, 0.0f + (1f - friends) * 0.4f)
        //		picker.add(ShardType.GENERAL, 0.2f);
//		if (friends < 0.3f) {
//			picker.add(ShardType.GENERAL, Math.min(0.25f, (1f - friends) * 0.25f));
//		}
        val unlikelyWeight = 0f
        val unlikelyThreshold = 0.2f
        if (antiFighter < unlikelyThreshold) antiFighter = unlikelyWeight
        picker.add(ShardType.POINT_DEFENSE, antiFighter)
        if (antiShield < unlikelyThreshold) antiShield = unlikelyWeight
        picker.add(ShardType.ANTI_SHIELD, antiShield)
        if (antiArmor < unlikelyThreshold) antiArmor = unlikelyWeight
        picker.add(ShardType.ANTI_ARMOR, antiArmor)
        if (missile < unlikelyThreshold) missile = unlikelyWeight
        picker.add(ShardType.MISSILE, missile)
        return picker
    }

    fun isVulnerableToMissileBarrage(from: ShipAPI, other: ShipAPI): Boolean {
        val incap = Misc.getIncapacitatedTime(other)
        val dist = Misc.getDistance(from.location, other.location)
        if (dist > 2000) return false
        val assumedMissileSpeed = 500f
        var eta = dist / assumedMissileSpeed
        eta += ShardSpawner.SPAWN_TIME
        eta += 2f
        return incap >= eta || other.fluxLevel >= 0.95f && other.fluxTracker.timeToVent >= eta
    }

    fun hasGoodArmor(other: ShipAPI): Boolean {
        val requiredArmor = 1240f
        if (other.armorGrid.armorRating < requiredArmor) return false
        val armor = other.getAverageArmorInSlice(other.facing, 120f)
        return armor >= requiredArmor * 0.8f
    }

    fun hasGoodShields(other: ShipAPI): Boolean {
        val shield = other.shield ?: return false
        if (shield.type == ShieldType.NONE) return false
        if (shield.type == ShieldType.PHASE) return false
        var requiredCapacity = 10000000f
        when (other.hullSize) {
            HullSize.CAPITAL_SHIP -> {
                requiredCapacity = 25000f
                if (shield.type == ShieldType.FRONT && shield.arc < 250) {
                    requiredCapacity = 1000000f
                }
            }

            HullSize.CRUISER -> {
                requiredCapacity = 12500f
                if (shield.type == ShieldType.FRONT && shield.arc < 250) {
                    requiredCapacity = 1000000f
                }
            }

            HullSize.DESTROYER -> requiredCapacity = 8000f
            HullSize.FRIGATE -> requiredCapacity = 4000f
            else -> {}
        }
        val e = other.shield.fluxPerPointOfDamage *
                other.mutableStats.shieldDamageTakenMult.modifiedValue
        var capacity = other.maxFlux
        capacity /= 0.1f.coerceAtLeast(e)
        return capacity >= requiredCapacity && e <= 1f
    }

    companion object {
        private const val REPLACEMENT_INTERVAL_ID = "et_anomalousConjurationInterval"

        fun advanceReplacementInterval(ship: ShipAPI, amount: Float): Boolean {
            val replacementInterval = getReplacementInterval(ship)
            replacementInterval!!.advance(amount)
            return replacementInterval.intervalElapsed()
        }

        private fun getReplacementInterval(ship: ShipAPI): IntervalUtil? {
            if (!ship.customData.containsKey(REPLACEMENT_INTERVAL_ID)) {
                ship.setCustomData(REPLACEMENT_INTERVAL_ID, IntervalUtil(45f, 60f))
            }
            return ship.customData[REPLACEMENT_INTERVAL_ID] as IntervalUtil?
        }
    }
}