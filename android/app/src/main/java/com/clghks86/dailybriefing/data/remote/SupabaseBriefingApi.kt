package com.clghks86.dailybriefing.data.remote

import com.clghks86.dailybriefing.data.remote.dto.BriefingDto
import com.clghks86.dailybriefing.data.remote.dto.BriefingImageDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Supabase PostgREST 엔드포인트. 인증 헤더는 OkHttp 인터셉터가 붙인다. */
interface SupabaseBriefingApi {

    @GET("rest/v1/daily_briefings")
    suspend fun briefings(
        @Query("select") select: String = "*",
        @Query("order") order: String = "briefing_date.desc",
        @Query("limit") limit: Int = 90,
    ): List<BriefingDto>

    @GET("rest/v1/briefing_images")
    suspend fun images(
        @Query("select") select: String = "*",
        @Query("order") order: String = "briefing_date.desc,sort_order.asc",
        @Query("limit") limit: Int = 300,
    ): List<BriefingImageDto>
}
