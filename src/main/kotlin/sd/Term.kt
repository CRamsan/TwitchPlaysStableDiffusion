package sd

import kotlin.math.absoluteValue

/**
 * Data class to manage a single term within an SD prompt. Consider a [Term] as a word or list of words that represent
 * a descriptor used by SD. A term's relevance is described by [emphasis].
 */
data class Term(
    val term: String,
    val emphasis: Int = 0,
)

/**
 * Increase the weight of this term when processed by SD.
 */
fun Term.emphasize(): Term {
    val diff = if (emphasis < 5) { 1 } else { 0 }
    return copy(
        emphasis = emphasis + diff
    )
}

/**
 * Lower the weight of this term when processed by SD.
 */
fun Term.deemphasize(): Term {
    val diff = if (emphasis >- 5) { -1 } else { 0 }
    return copy(
        emphasis = emphasis + diff
    )
}

/**
 * Convert a list of [Term] into an actual prompt by joining them all
 * and separating them with a comma.
 */
fun List<Term>.toPrompt(): String {
    return buildString {
        this@toPrompt.forEachIndexed { index, term ->
            val isPositive = term.emphasis > 0
            repeat(term.emphasis.absoluteValue) {
                append(
                    if (isPositive)
                        "("
                    else
                        "["
                )
            }
            append(term.term)
            repeat(term.emphasis.absoluteValue) {
                append(
                    if (isPositive)
                        ")"
                    else
                        "]"
                )
            }
            if (index != this@toPrompt.lastIndex) {
                append(", ")
            }
        }
    }
}