package com.clghks86.dailybriefing.domain.repository

import com.clghks86.dailybriefing.domain.model.Briefing

interface BriefingRepository {
    /** 최신순 정렬된 브리핑 목록. forceRefresh면 캐시를 무시한다. */
    suspend fun briefings(forceRefresh: Boolean = false): Result<List<Briefing>>

    suspend fun latest(forceRefresh: Boolean = false): Result<Briefing?> =
        briefings(forceRefresh).map { it.firstOrNull() }

    fun invalidateCache()
}
