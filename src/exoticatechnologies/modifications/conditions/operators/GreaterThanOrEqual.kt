package exoticatechnologies.modifications.conditions.operators

class GreaterThanOrEqual: Operator {
    override val key: String
        get() = ">="

    override fun matches(actual: Any?, expected: Any, extra: Any?): Boolean {
        return (actual as Number).toDouble() >= (expected as Number).toDouble()
    }

    override fun calculateWeight(actual: Any, expected: Any, extra: Any?): Float {
        return ((actual as Number).toDouble() / (expected as Number).toDouble()).toFloat()
    }
}