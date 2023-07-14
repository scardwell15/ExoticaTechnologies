package exoticatechnologies.modifications.conditions.operators

class NotEquals: Operator {
    override val key: String
        get() = "!="

    override fun matches(actual: Any?, expected: Any, extra: Any?): Boolean {
        return actual != expected
    }

    override fun calculateWeight(actual: Any, expected: Any, extra: Any?): Float {
        return 1f
    }
}