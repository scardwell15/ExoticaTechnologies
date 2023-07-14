package exoticatechnologies.modifications.conditions

import exoticatechnologies.modifications.conditions.operators.GreaterThan
import exoticatechnologies.modifications.conditions.operators.GreaterThanOrEqual
import exoticatechnologies.modifications.conditions.operators.Equals
import exoticatechnologies.modifications.conditions.operators.LessThan
import exoticatechnologies.modifications.conditions.operators.LessThanOrEqual
import exoticatechnologies.modifications.conditions.operators.NotEquals
import exoticatechnologies.modifications.conditions.operators.In
import exoticatechnologies.modifications.conditions.operators.NotIn
import exoticatechnologies.modifications.conditions.operators.Operator

enum class OperatorDict(
    val string: String,
    val method: () -> Operator
) {
    ge(">=", { GreaterThanOrEqual() }),
    g(">", { GreaterThan() }),
    e("==", { Equals() }),
    l("<", { LessThan() }),
    le("<=", { LessThanOrEqual() }),
    ne("!=", { NotEquals() }),
    In("in", { In() }),
    notIn("notIn", { NotIn() });

    companion object {
        fun get(operatorString: String): Operator {
            try {
                return OperatorDict.values().first { it.string == operatorString }.method.invoke()
            } catch (e: NoSuchElementException) {
                throw Exception("$operatorString is not a valid operator. Use ${combineStrings()} as valid operators.")
            }
        }

        fun combineStrings(): String {
            return OperatorDict.values()
                .map { it.string }
                .joinToString(", ")
        }
    }
}