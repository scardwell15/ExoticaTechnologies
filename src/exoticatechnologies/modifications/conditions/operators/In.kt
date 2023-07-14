package exoticatechnologies.modifications.conditions.operators

import exoticatechnologies.modifications.exotics.ETExotics
import exoticatechnologies.modifications.upgrades.ETUpgrades

class In: Operator {
    override val key: String
        get() = "in"

    override fun matches(actual: Any?, expected: Any, extra: Any?): Boolean {
        if (actual is ETUpgrades) {
            if (expected is String) {
                return actual.getUpgrade(expected) > 0
            } else if (expected is Collection<*>) {
                return expected
                    .any { actual.getUpgrade(it as String) > 0 }
            }
        } else if (actual is ETExotics) {
            if (expected is String) {
                return actual.hasExotic(expected)
            } else if (expected is Collection<*>) {
                return expected
                    .any { actual.hasExotic(it as String) }
            }
        } else if (actual is Collection<*>) {
            if (expected is String) {
                return actual.contains(expected)
            } else if (expected is Collection<*>) {
                return expected.any { actual.contains(it) }
            }
        } else {
            return (expected as Collection<*>).contains(actual.toString())
        }
        throw IllegalArgumentException("in operator only takes String or JSON array")
    }

    override fun calculateWeight(actual: Any, expected: Any, extra: Any?): Float {
        return 1f
    }
}