package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.domain.markdown.MarkdownBlock
import com.clghks86.dailybriefing.domain.markdown.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    private val sample = """
        # 오늘의 브리핑

        최치환님, 2026년 11월 24일 동향입니다.
        기준 시각은 09시 24분입니다.

        ---

        ## ① 핵심 요약

        - **나스닥** +6.51%
        - 코스피 장중 5,000 터치
        1. 한은 금통위
        2. 미국 CPI

        > 휴장일에는 직전 종가입니다.
    """.trimIndent()

    @Test
    fun `headings carry their level`() {
        val headings = MarkdownParser.parse(sample).filterIsInstance<MarkdownBlock.Heading>()

        assertEquals(2, headings.size)
        assertEquals(1, headings[0].level)
        assertEquals("오늘의 브리핑", headings[0].text)
        assertEquals(2, headings[1].level)
        assertEquals("① 핵심 요약", headings[1].text)
    }

    @Test
    fun `consecutive lines merge into one paragraph`() {
        val paragraphs = MarkdownParser.parse(sample).filterIsInstance<MarkdownBlock.Paragraph>()

        assertEquals(1, paragraphs.size)
        assertTrue(paragraphs.first().text.contains("최치환님"))
        assertTrue(paragraphs.first().text.contains("기준 시각"))
    }

    @Test
    fun `bullets and numbered items are separated`() {
        val bullets = MarkdownParser.parse(sample).filterIsInstance<MarkdownBlock.Bullet>()

        assertEquals(4, bullets.size)
        assertTrue(!bullets[0].ordered)
        assertEquals("한은 금통위", bullets[2].text)
        assertTrue(bullets[2].ordered)
        assertEquals("1.", bullets[2].marker)
    }

    @Test
    fun `divider and quote are recognized`() {
        val blocks = MarkdownParser.parse(sample)

        assertTrue(blocks.contains(MarkdownBlock.Divider))
        val quote = blocks.filterIsInstance<MarkdownBlock.Quote>().single()
        assertEquals("휴장일에는 직전 종가입니다.", quote.text)
    }

    @Test
    fun `inline bold and code become runs`() {
        val runs = MarkdownParser.parseInline("**나스닥** +6.51% `code` 끝")

        assertEquals(4, runs.size)
        assertEquals("나스닥", runs[0].text)
        assertTrue(runs[0].bold)
        assertEquals(" +6.51% ", runs[1].text)
        assertEquals("code", runs[2].text)
        assertTrue(runs[2].code)
        assertEquals(" 끝", runs[3].text)
    }

    @Test
    fun `unclosed bold marker stays as text`() {
        val runs = MarkdownParser.parseInline("**닫히지 않은 강조")

        assertEquals(1, runs.size)
        assertEquals("**닫히지 않은 강조", runs[0].text)
        assertTrue(!runs[0].bold)
    }

    @Test
    fun `plain text drops markup characters`() {
        val plain = MarkdownParser.plainText(sample)

        assertTrue(!plain.contains("#"))
        assertTrue(!plain.contains("**"))
        assertTrue(plain.contains("나스닥"))
    }

    @Test
    fun `empty input yields no blocks`() {
        assertTrue(MarkdownParser.parse("").isEmpty())
        assertTrue(MarkdownParser.parse("   \n \n").isEmpty())
    }
}
