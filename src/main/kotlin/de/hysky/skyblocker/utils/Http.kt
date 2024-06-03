package de.hysky.skyblocker.utils

import de.hysky.skyblocker.SkyblockerMod
import net.minecraft.SharedConstants
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

/**
 * @implNote All http requests are sent using HTTP 2
 */
object Http {
	private const val NAME_2_UUID = "https://api.minecraftservices.com/minecraft/profile/lookup/name/"
	private const val HYPIXEL_PROXY = "https://hysky.de/api/hypixel/v2/"
	private val USER_AGENT = "Skyblocker/" + SkyblockerMod.VERSION + " (" + SharedConstants.getGameVersion().name + ")"
	private val HTTP_CLIENT: HttpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build()

	@Throws(IOException::class, InterruptedException::class)
	private fun sendCacheableGetRequest(url: String): ApiResponse {
		val request = HttpRequest.newBuilder()
			.GET()
			.header("Accept", "application/json")
			.header("Accept-Encoding", "gzip, deflate")
			.header("User-Agent", USER_AGENT)
			.version(HttpClient.Version.HTTP_2)
			.uri(URI.create(url))
			.build()

		val response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream())
		val decodedInputStream = getDecodedInputStream(response)

		val body = String(decodedInputStream.readAllBytes())
		val headers = response.headers()

		return ApiResponse(body, response.statusCode(), getCacheStatuses(headers), getAge(headers))
	}

	@Throws(IOException::class, InterruptedException::class)
	fun downloadContent(url: String): InputStream {
		val request = HttpRequest.newBuilder()
			.GET()
			.header("Accept", "*/*")
			.header("Accept-Encoding", "gzip, deflate")
			.header("User-Agent", USER_AGENT)
			.version(HttpClient.Version.HTTP_2)
			.uri(URI.create(url))
			.build()

		val response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream())
		return getDecodedInputStream(response)
	}

	@Throws(IOException::class, InterruptedException::class)
	fun sendGetRequest(url: String): String {
		return sendCacheableGetRequest(url).content
	}

	@Throws(IOException::class, InterruptedException::class)
	fun sendHeadRequest(url: String?): HttpHeaders {
		val request = HttpRequest.newBuilder()
			.method("HEAD", BodyPublishers.noBody())
			.header("User-Agent", USER_AGENT)
			.version(HttpClient.Version.HTTP_2)
			.uri(URI.create(url))
			.build()

		val response = HTTP_CLIENT.send(request, BodyHandlers.discarding())
		return response.headers()
	}

	@Throws(IOException::class, InterruptedException::class)
	fun sendName2UuidRequest(name: String): ApiResponse {
		return sendCacheableGetRequest(NAME_2_UUID + name)
	}

	/**
	 * @param endpoint the endpoint - do not include any leading or trailing slashes
	 * @param query the query string - use empty string if n/a
	 * @return the requested data with zero pre-processing applied
	 *
	 * @implNote the `v2` prefix is automatically added
	 */
	@JvmStatic
	@Throws(IOException::class, InterruptedException::class)
	fun sendHypixelRequest(endpoint: String, query: String): ApiResponse {
		return sendCacheableGetRequest(HYPIXEL_PROXY + endpoint + query)
	}

	private fun getDecodedInputStream(response: HttpResponse<InputStream>): InputStream {
		val encoding = getContentEncoding(response.headers())

		try {
			return when (encoding) {
				"" -> response.body()
				"gzip" -> GZIPInputStream(response.body())
				"deflate" -> InflaterInputStream(response.body())
				else -> throw UnsupportedOperationException("The server sent content in an unexpected encoding: $encoding")
			}
		} catch (e: IOException) {
			throw UncheckedIOException(e)
		}
	}

	private fun getContentEncoding(headers: HttpHeaders): String {
		return headers.firstValue("Content-Encoding").orElse("")
	}

	@JvmStatic
	fun getEtag(headers: HttpHeaders): String {
		return headers.firstValue("Etag").orElse("")
	}

	@JvmStatic
	fun getLastModified(headers: HttpHeaders): String {
		return headers.firstValue("Last-Modified").orElse("")
	}

	/**
	 * Returns the cache statuses of the resource. All possible cache status values conform to Cloudflare's.
	 *
	 * @see [Cloudflare Cache Docs](https://developers.cloudflare.com/cache/concepts/cache-responses/)
	 */
	private fun getCacheStatuses(headers: HttpHeaders): Array<String> {
		return arrayOf(headers.firstValue("CF-Cache-Status").orElse("UNKNOWN"), headers.firstValue("Local-Cache-Status").orElse("UNKNOWN"))
	}

	private fun getAge(headers: HttpHeaders): Int {
		return headers.firstValue("Age").orElse("-1").toInt()
	}

	//TODO If ever needed, we could just replace cache status with the response headers and go from there
	@JvmRecord
	data class ApiResponse(@JvmField val content: String, @JvmField val statusCode: Int, val cacheStatuses: Array<String>, @JvmField val age: Int) : AutoCloseable {
		fun ok(): Boolean {
			return statusCode == 200
		}

		fun ratelimited(): Boolean {
			return statusCode == 429
		}

		fun cached(): Boolean {
			return cacheStatuses[0] == "HIT" || cacheStatuses[1] == "HIT"
		}

		override fun close() {
			//Allows for nice syntax when dealing with api requests in try catch blocks
			//Maybe one day we'll have some resources to free
		}
	}
}
