package exoticatechnologies.modifications.conditions.operators

class LessThan: Operator {
    override val key: String
        get() = "<"

    override fun matches(actual: Any?, expected: Any, extra: Any?): Boolean {
        return (actual as Number).toDouble() < (expected as Number).toDouble()
    }

    override fun calculateWeight(actual: Any, expected: Any, extra: Any?): Float {
        val expectedNum = (expected as Number).toDouble()
        val actualNum = (actual as Number).toDouble()

        val slope = -4 / (0.5 * expectedNum)
        val intercept = 2 - slope * 0.5 * expectedNum
        return (slope * actualNum + intercept).toFloat()
    }
}