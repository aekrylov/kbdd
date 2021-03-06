package ru.fix.kbdd.asserts

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.math.abs

interface Expression {
    fun print(): String
    fun evaluate(): Boolean

    infix fun and(other: Expression): Expression = AndExpression(this, other)
    infix fun or(other: Expression): Expression = OrExpression(this, other)
    infix fun not(other: Expression): Expression = NotExpression(this)
}

class AndExpression(
        private val left: Expression,
        private val right: Expression) : Expression {

    override fun print(): String = "(${left.print()}) and (${right.print()})"
    override fun evaluate(): Boolean = left.evaluate() && right.evaluate()
}

class OrExpression(
        private val left: Expression,
        private val right: Expression) : Expression {

    override fun print(): String = "(${left.print()}) or (${right.print()})"
    override fun evaluate(): Boolean = left.evaluate() || right.evaluate()
}

class NotExpression(private val exp: Expression) : Expression {
    override fun print(): String = "not (${exp.print()})"
    override fun evaluate(): Boolean = !exp.evaluate()
}

interface Source {
    fun print(): String
    fun evaluate(): Any?
}

interface Checkable {
    fun express(expression: (Source) -> Expression): Expression
}


infix fun Checkable.isEquals(other: Any?) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} == ${
        if (other != null && other is String) "\"$other\"" else "$other"
        }"

        override fun evaluate(): Boolean = checkValuesEqualityWithStringAutoCast(source.evaluate(), other)
    }
}

infix fun Checkable.isNotEquals(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} != $other"
        override fun evaluate(): Boolean = source.evaluate() != other
    }
}

fun Checkable.isEquals(other: Number, delta: Double) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} == $other (with delta = $delta)"

        override fun evaluate(): Boolean = checkValuesEqualityWithDelta(source.evaluate(), other, delta)
    }
}

fun Checkable.isNotEquals(other: Number, delta: Double) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} != $other (with delta = $delta)"

        override fun evaluate(): Boolean = !checkValuesEqualityWithDelta(source.evaluate(), other, delta)
    }
}

fun Checkable.isNull() = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} == null"
        override fun evaluate(): Boolean = source.evaluate() == null
    }
}

fun Checkable.isNotNull() = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} != null"
        override fun evaluate(): Boolean = source.evaluate() != null
    }
}

private fun compareValuesWithStringAutoCast(first: Any, second: Any): Int {
    if (first is String) {
        when (second) {
            is Boolean -> return first.toBoolean().compareTo(second)
            is Byte -> return first.toDouble().compareTo(second)
            is Short -> return first.toDouble().compareTo(second)
            is Int -> return first.toDouble().compareTo(second)
            is Float -> return first.toDouble().compareTo(second)
            is Double -> return first.toDouble().compareTo(second)
            is Long -> return first.toDouble().compareTo(second)
            is BigDecimal -> return first.toBigDecimal().compareTo(second)
            is BigInteger -> return first.toBigDecimal().compareTo(BigDecimal(second))
            is UUID -> return UUID.fromString(first).compareTo(second)
        }
    }
    if (first is Number) {
        when (second) {
            is Byte -> return first.toByte().compareTo(second)
            is Short -> return first.toShort().compareTo(second)
            is Int -> return first.toInt().compareTo(second)
            is Float -> return first.toFloat().compareTo(second)
            is Double -> return first.toDouble().compareTo(second)
            is Long -> return first.toLong().compareTo(second)
            is BigDecimal -> return BigDecimal(first.toDouble()).compareTo(second)
            is BigInteger -> return BigInteger.valueOf(first.toLong()).compareTo(second)
        }
    }
    return (first as Comparable<Any>).compareTo(second)
}

private fun checkValuesEqualityWithStringAutoCast(first: Any?, second: Any?): Boolean {
    if (first == null && second != null) return false
    if (first != null && second == null) return false
    if (first == null && second == null) return true
    if (first is String) {
        when (second) {
            is Boolean -> return first.toBoolean() == second
            is Byte -> return first.toByte() == second
            is Short -> return first.toShort() == second
            is Int -> return first.toInt() == second
            is Float -> return first.toFloat() == second
            is Double -> return first.toDouble() == second
            is Long -> return first.toLong() == second
            is BigDecimal -> return first.toBigDecimal() == second
            is BigInteger -> return first.toBigInteger() == second
            is UUID -> return UUID.fromString(first) == second
        }
    }
    if (first is Number) {
        when (second) {
            is Boolean -> return false
            is Byte -> return first.toByte() == second
            is Short -> return first.toShort() == second
            is Int -> return first.toInt() == second
            is Float -> return first.toFloat() == second
            is Double -> return first.toDouble() == second
            is Long -> return first.toLong() == second
            is BigDecimal -> return BigDecimal(first.toDouble()) == second
            is BigInteger -> return BigInteger.valueOf(first.toLong()) == second
        }
    }
    return first == second
}

private fun checkValuesEqualityWithDelta(first: Any?, second: Number, delta: Double): Boolean {
    if (first == null) return false

    val firstDoubleValue = when (first) {
        is Number -> first.toDouble()
        is String -> first.toDouble()
        else -> return false
    }

    return abs(firstDoubleValue - second.toDouble()) < delta
}

infix fun Checkable.isLessThan(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} < $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) < 0
    }
}

infix fun Checkable.isLessThanOrEqual(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} <= $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) <= 0
    }
}

infix fun Checkable.isGreaterThan(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} > $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) > 0
    }
}

infix fun Checkable.isGreaterThanOrEqual(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} >= $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) >= 0
    }
}


infix fun Checkable.isContains(text: String) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()}.isContains($text)"
        override fun evaluate(): Boolean = source.evaluate().toString().contains(text)
    }
}

infix fun Checkable.isMatches(regex: String) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()}.isMatches(\"$regex\")"
        override fun evaluate(): Boolean = source.evaluate().toString().matches(regex.toRegex())
    }
}

fun Checkable.shouldBeIn(vararg values: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} in ${values.toList()}"
        override fun evaluate(): Boolean = source.evaluate() in values
    }
}

infix fun Checkable.shouldBeIn(values: Collection<Any>) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} in $values"
        override fun evaluate(): Boolean = source.evaluate() in values
    }
}

fun Checkable.shouldNotBeIn(vararg values: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} not in ${values.toList()}"
        override fun evaluate(): Boolean = source.evaluate() !in values
    }
}

infix fun Checkable.shouldNotBeIn(values: Collection<Any>) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} not in $values"
        override fun evaluate(): Boolean = source.evaluate() !in values
    }
}