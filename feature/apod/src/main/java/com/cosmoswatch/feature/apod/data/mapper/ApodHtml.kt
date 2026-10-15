package com.cosmoswatch.feature.apod.data.mapper

private val WHITESPACE_REGEX = Regex("""\s+""")
private val LINE_BREAK_TAG_REGEX = Regex("""<br\s*/?>|</p>""", RegexOption.IGNORE_CASE)
private val TAG_REGEX = Regex("""<[^>]*>""")
private val ENTITY_REGEX = Regex("""&(#[xX][0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);""")
private val BLANK_LINES_REGEX = Regex("""\n{3,}""")

private val NAMED_ENTITIES = mapOf(
    "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to " ",
    "ndash" to "–", "mdash" to "—", "hellip" to "…", "lsquo" to "‘", "rsquo" to "’",
    "ldquo" to "“", "rdquo" to "”", "deg" to "°", "times" to "×", "eacute" to "é",
)

internal fun String.htmlToPlainText(): String = replace(WHITESPACE_REGEX, " ")
    .replace(LINE_BREAK_TAG_REGEX, "\n")
    .replace(TAG_REGEX, "")
    .decodeHtmlEntities()
    .lines()
    .joinToString("\n") { it.trim() }
    .replace(BLANK_LINES_REGEX, "\n\n")
    .trim()

private fun String.decodeHtmlEntities(): String = replace(ENTITY_REGEX) { match ->
    val entity = match.groupValues[1]
    when {
        entity.startsWith("#x", ignoreCase = true) -> entity.drop(2).toIntOrNull(16)?.let(::codePointToString)
        entity.startsWith("#") -> entity.drop(1).toIntOrNull()?.let(::codePointToString)
        else -> NAMED_ENTITIES[entity]
    } ?: match.value
}

private fun codePointToString(codePoint: Int): String? =
    if (Character.isValidCodePoint(codePoint)) String(Character.toChars(codePoint)) else null
