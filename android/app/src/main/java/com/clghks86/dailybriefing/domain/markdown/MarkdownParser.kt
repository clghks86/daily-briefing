package com.clghks86.dailybriefing.domain.markdown

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Bullet(val text: String, val ordered: Boolean, val marker: String?) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data object Divider : MarkdownBlock
    data object Spacer : MarkdownBlock
}

data class InlineRun(val text: String, val bold: Boolean = false, val code: Boolean = false)

/**
 * 브리핑 `raw_markdown` 전문 뷰어용 최소 파서.
 * 제목(#), 목록(-, *, 1.), 인용(>), 구분선(---)만 다룬다 — 원문 구조를 유지하는 게 목적이다.
 */
object MarkdownParser {

    fun parse(markdown: String): List<MarkdownBlock> {
        val blocks = mutableListOf<MarkdownBlock>()
        val paragraph = StringBuilder()

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                blocks += MarkdownBlock.Paragraph(paragraph.toString().trim())
                paragraph.clear()
            }
        }

        markdown.lineSequence().forEach { rawLine ->
            val line = rawLine.trimEnd()
            val trimmed = line.trim()

            when {
                // 빈 줄은 문단 경계로만 쓴다.
                trimmed.isEmpty() -> flushParagraph()

                trimmed.matches(Regex("^([-*_])\\1{2,}$")) -> {
                    flushParagraph()
                    blocks += MarkdownBlock.Divider
                }

                trimmed.startsWith("#") -> {
                    flushParagraph()
                    val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(6)
                    val text = trimmed.drop(level).trim()
                    if (text.isNotEmpty()) blocks += MarkdownBlock.Heading(level, text)
                }

                trimmed.startsWith(">") -> {
                    flushParagraph()
                    blocks += MarkdownBlock.Quote(trimmed.trimStart('>').trim())
                }

                isBullet(trimmed) -> {
                    flushParagraph()
                    blocks += bulletOf(trimmed)
                }

                else -> {
                    if (paragraph.isNotEmpty()) paragraph.append(' ')
                    paragraph.append(trimmed)
                }
            }
        }
        flushParagraph()
        return blocks
    }

    private fun isBullet(line: String): Boolean =
        line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") ||
            Regex("^\\d{1,2}\\.\\s+.*").matches(line)

    private fun bulletOf(line: String): MarkdownBlock.Bullet {
        val orderedMatch = Regex("^(\\d{1,2})\\.\\s+(.*)$").find(line)
        if (orderedMatch != null) {
            val (number, text) = orderedMatch.destructured
            return MarkdownBlock.Bullet(text = text.trim(), ordered = true, marker = "$number.")
        }
        val text = line.drop(2).trim()
        return MarkdownBlock.Bullet(text = text, ordered = false, marker = null)
    }

    /** **굵게** 와 `코드` 표기를 런 목록으로 나눈다. */
    fun parseInline(text: String): List<InlineRun> {
        val runs = mutableListOf<InlineRun>()
        var index = 0
        while (index < text.length) {
            val boldStart = text.indexOf("**", index)
            val codeStart = text.indexOf('`', index)
            val next = listOf(boldStart, codeStart).filter { it >= 0 }.minOrNull()
            if (next == null) {
                runs += InlineRun(text.substring(index))
                break
            }
            if (next > index) runs += InlineRun(text.substring(index, next))
            val isBold = next == boldStart
            val marker = if (isBold) "**" else "`"
            val close = text.indexOf(marker, next + marker.length)
            if (close < 0) {
                // 닫히지 않은 표기는 그대로 둔다.
                runs += InlineRun(text.substring(next))
                break
            }
            runs += InlineRun(
                text = text.substring(next + marker.length, close),
                bold = isBold,
                code = !isBold,
            )
            index = close + marker.length
        }
        return runs.filter { it.text.isNotEmpty() }
    }

    /** 테스트·검색용: 마크다운 기호를 걷어낸 평문. */
    fun plainText(markdown: String): String = parse(markdown).joinToString("\n") { block ->
        when (block) {
            is MarkdownBlock.Heading -> block.text
            is MarkdownBlock.Paragraph -> block.text
            is MarkdownBlock.Bullet -> block.text
            is MarkdownBlock.Quote -> block.text
            MarkdownBlock.Divider, MarkdownBlock.Spacer -> ""
        }
    }.replace(Regex("\\*\\*|`"), "")
}
