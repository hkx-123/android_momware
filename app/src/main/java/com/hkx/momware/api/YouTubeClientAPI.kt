package com.hkx.momware.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class YouTubeVideoInfo(
    val title: String
)


/**
 * Use the Retrofit builder with the YouTube URL...
 */
private const val BASE_URL = "https://www.youtube.com/"

private val json = Json { ignoreUnknownKeys = true }

private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

/**
 * A public API object that exposes the lazy-initialized Retrofit service
 */
object YouTubeVideoInfosApi {
    val retrofitService: YouTubeVideoInfosAPIService by lazy {
        retrofit.create(YouTubeVideoInfosAPIService::class.java)
    }

    suspend fun getVideoInfo(videoUrl : String) : YouTubeVideoInfo {
        return retrofitService.getVideoInfo( "youtube.com/watch?v=${videoUrl}" )
    }
}

/**
 * Retrofit service object for creating api calls
 */
interface YouTubeVideoInfosAPIService {

    /***
        e.g. Output:
        {
            "title": "Audioslave - Like a Stone (Official Video)",
            "author_name": "AudioslaveVEVO",
            "author_url": "https://www.youtube.com/@AudioslaveVEVO",
            "type": "video",
            "height": 150,
            "width": 200,
            "version": "1.0",
            "provider_name": "YouTube",
            "provider_url": "https://www.youtube.com/",
            "thumbnail_height": 360,
            "thumbnail_width": 480,
            "thumbnail_url": "https://i.ytimg.com/vi/7QU1nvuxaMA/hqdefault.jpg",
            "html": "\u003Ciframe width=\"200\" height=\"150\" src=\"https://www.youtube.com/embed/7QU1nvuxaMA?feature=oembed\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen title=\"Audioslave - Like a Stone (Official Video)\"\u003E\u003C/iframe\u003E"
        }
     */
    @GET("/oembed")
    suspend fun getVideoInfo(@Query("url") videoUrl : String): YouTubeVideoInfo

}
