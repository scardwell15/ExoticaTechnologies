package exoticatechnologies.modifications.conditions.operators

interface Operator {
    val key: String

    fun matches(actual: Any?, expected: Any, extra: Any?): Boolean
    fun calculateWeight(actual: Any, expected: Any, extra: Any?): Float
}