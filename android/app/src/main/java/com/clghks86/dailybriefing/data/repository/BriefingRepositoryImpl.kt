package com.clghks86.dailybriefing.data.repository

import com.clghks86.dailybriefing.data.remote.SupabaseBriefingApi
import com.clghks86.dailybriefing.domain.mapper.BriefingMapper
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.repository.BriefingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 메모리 캐시(TTL)를 둔 저장소. 화면 전환·위젯·워커가 같은 인스턴스를 공유한다.
 */
class BriefingRepositoryImpl(
    private val api: SupabaseBriefingApi,
    private val cacheTtlMillis: Long = DEFAULT_TTL_MILLIS,
    private val now: () -> Long = System::currentTimeMillis,
    private val onLoaded: ((Briefing) -> Unit)? = null,
) : BriefingRepository {

    private val mutex = Mutex()
    private var cached: List<Briefing>? = null
    private var cachedAt: Long = 0L

    override suspend fun briefings(forceRefresh: Boolean): Result<List<Briefing>> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val hit = cached
                if (!forceRefresh && hit != null && now() - cachedAt < cacheTtlMillis) {
                    return@withLock Result.success(hit)
                }
                val fetched = runCatching {
                    val rows = api.briefings()
                    val images = runCatching { api.images() }.getOrDefault(emptyList())
                    BriefingMapper.map(rows, images)
                }
                fetched.fold(
                    onSuccess = { mapped ->
                        cached = mapped
                        cachedAt = now()
                        mapped.firstOrNull()?.let { onLoaded?.invoke(it) }
                        Result.success(mapped)
                    },
                    onFailure = { error ->
                        // 실패해도 만료된 캐시가 있으면 그걸 보여 준다(네트워크 끊김 대응).
                        val stale = cached
                        if (stale != null) Result.success(stale) else Result.failure(error)
                    },
                )
            }
        }

    override fun invalidateCache() {
        cached = null
        cachedAt = 0L
    }

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 5 * 60 * 1000L
    }
}
