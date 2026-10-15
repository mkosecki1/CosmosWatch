package com.cosmoswatch.feature.apod.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApodHtmlTest {

    @Test
    fun `tags are stripped and source line wrapping collapses into single spaces`() {
        assertEquals("A bright comet", "<b>A</b>\n  <a href=\"x\">bright</a>   comet".htmlToPlainText())
    }

    @Test
    fun `line break tags become newlines`() {
        assertEquals("First\n\nSecond", "First<br><br/><BR />Second".htmlToPlainText())
    }

    @Test
    fun `named and numeric entities are decoded once`() {
        assertEquals("Sun & Moon — 5° ’ é &amp;", "Sun &amp; Moon &mdash; 5&#176; &#x2019; &eacute; &amp;amp;".htmlToPlainText())
    }

    @Test
    fun `unknown entities are kept as they are`() {
        assertEquals("&foo; stays", "&foo; stays".htmlToPlainText())
    }
}
