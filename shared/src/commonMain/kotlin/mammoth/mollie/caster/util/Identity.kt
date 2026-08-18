@file:OptIn(ExperimentalUnsignedTypes::class)

package mammoth.mollie.caster.util

import mammoth.mollie.caster.model.Enclosure
import mammoth.mollie.caster.model.EpisodeId
import mammoth.mollie.caster.model.PodcastId

fun normalizeFeedUrl(input: String): String {
    val raw = input.trim().substringBefore('#')
    val schemeEnd = raw.indexOf("://")
    if (schemeEnd <= 0) return raw
    val scheme = raw.substring(0, schemeEnd).lowercase()
    val remainder = raw.substring(schemeEnd + 3)
    val authorityEnd = remainder.indexOfAny(charArrayOf('/', '?')).let { if (it < 0) remainder.length else it }
    val authority = remainder.substring(0, authorityEnd)
    val suffix = remainder.substring(authorityEnd)
    val userInfoEnd = authority.lastIndexOf('@')
    val userInfo = if (userInfoEnd >= 0) authority.substring(0, userInfoEnd + 1) else ""
    val hostPort = authority.substring(userInfoEnd + 1)
    val normalizedHostPort = when {
        scheme == "http" && hostPort.endsWith(":80") -> hostPort.dropLast(3)
        scheme == "https" && hostPort.endsWith(":443") -> hostPort.dropLast(4)
        else -> hostPort
    }.lowercase()
    val normalizedSuffix = when {
        suffix.isEmpty() -> "/"
        suffix.startsWith('?') -> "/$suffix"
        else -> suffix
    }
    return "$scheme://$userInfo$normalizedHostPort$normalizedSuffix"
}

fun podcastIdFor(feedUrl: String): PodcastId =
    PodcastId(sha256("podcast\u0000${normalizeFeedUrl(feedUrl)}"))

/** SHA-1 is required by Podcast Index's request-signing contract. */
fun sha1(value: String): String {
    val input = value.encodeToByteArray()
    val bitLength = input.size.toLong() * 8L
    val paddedSize = ((input.size + 9 + 63) / 64) * 64
    val data = ByteArray(paddedSize)
    input.copyInto(data)
    data[input.size] = 0x80.toByte()
    for (i in 0 until 8) data[paddedSize - 1 - i] = (bitLength ushr (i * 8)).toByte()

    var h0 = 0x67452301u
    var h1 = 0xefcdab89u
    var h2 = 0x98badcfeu
    var h3 = 0x10325476u
    var h4 = 0xc3d2e1f0u
    val words = UIntArray(80)
    for (offset in data.indices step 64) {
        for (i in 0 until 16) {
            val p = offset + i * 4
            words[i] = ((data[p].toUInt() and 0xffu) shl 24) or
                ((data[p + 1].toUInt() and 0xffu) shl 16) or
                ((data[p + 2].toUInt() and 0xffu) shl 8) or
                (data[p + 3].toUInt() and 0xffu)
        }
        for (i in 16 until 80) words[i] = (words[i - 3] xor words[i - 8] xor words[i - 14] xor words[i - 16]).rotateLeft(1)

        var a = h0; var b = h1; var c = h2; var d = h3; var e = h4
        for (i in 0 until 80) {
            val (f, k) = when (i) {
                in 0..19 -> ((b and c) or (b.inv() and d)) to 0x5a827999u
                in 20..39 -> (b xor c xor d) to 0x6ed9eba1u
                in 40..59 -> ((b and c) or (b and d) or (c and d)) to 0x8f1bbcdcu
                else -> (b xor c xor d) to 0xca62c1d6u
            }
            val next = a.rotateLeft(5) + f + e + k + words[i]
            e = d; d = c; c = b.rotateLeft(30); b = a; a = next
        }
        h0 += a; h1 += b; h2 += c; h3 += d; h4 += e
    }
    return uintArrayOf(h0, h1, h2, h3, h4).joinToString("") { it.toString(16).padStart(8, '0') }
}

fun episodeIdFor(
    podcastId: PodcastId,
    guid: String?,
    enclosures: List<Enclosure>,
    permalink: String?,
    title: String,
    publishedAtMillis: Long?,
): EpisodeId {
    val (kind, value) = when {
        !guid.isNullOrBlank() -> "guid" to guid.trim()
        enclosures.firstOrNull()?.url?.isNotBlank() == true ->
            "enclosure" to normalizeFeedUrl(enclosures.first().url)
        !permalink.isNullOrBlank() -> "permalink" to normalizeFeedUrl(permalink)
        else -> "fallback" to "${title.trim()}\u0000${publishedAtMillis ?: "unknown"}"
    }
    return EpisodeId(sha256("episode\u0000${podcastId.value}\u0000$kind\u0000$value"))
}

private val sha256Constants = uintArrayOf(
    0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u, 0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
    0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u, 0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
    0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu, 0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
    0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u, 0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
    0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u, 0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
    0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u, 0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
    0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u, 0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
    0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u, 0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u,
)

fun sha256(value: String): String {
    val input = value.encodeToByteArray()
    val bitLength = input.size.toLong() * 8L
    val paddedSize = ((input.size + 9 + 63) / 64) * 64
    val data = ByteArray(paddedSize)
    input.copyInto(data)
    data[input.size] = 0x80.toByte()
    for (i in 0 until 8) data[paddedSize - 1 - i] = (bitLength ushr (i * 8)).toByte()
    val hash = uintArrayOf(0x6a09e667u, 0xbb67ae85u, 0x3c6ef372u, 0xa54ff53au, 0x510e527fu, 0x9b05688cu, 0x1f83d9abu, 0x5be0cd19u)
    val words = UIntArray(64)
    for (offset in data.indices step 64) {
        for (i in 0 until 16) {
            val p = offset + i * 4
            words[i] = ((data[p].toUInt() and 0xffu) shl 24) or ((data[p + 1].toUInt() and 0xffu) shl 16) or
                ((data[p + 2].toUInt() and 0xffu) shl 8) or (data[p + 3].toUInt() and 0xffu)
        }
        for (i in 16 until 64) {
            val s0 = words[i - 15].rotateRight(7) xor words[i - 15].rotateRight(18) xor (words[i - 15] shr 3)
            val s1 = words[i - 2].rotateRight(17) xor words[i - 2].rotateRight(19) xor (words[i - 2] shr 10)
            words[i] = words[i - 16] + s0 + words[i - 7] + s1
        }
        var a = hash[0]; var b = hash[1]; var c = hash[2]; var d = hash[3]
        var e = hash[4]; var f = hash[5]; var g = hash[6]; var h = hash[7]
        for (i in 0 until 64) {
            val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
            val choice = (e and f) xor (e.inv() and g)
            val t1 = h + s1 + choice + sha256Constants[i] + words[i]
            val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
            val majority = (a and b) xor (a and c) xor (b and c)
            val t2 = s0 + majority
            h = g; g = f; f = e; e = d + t1; d = c; c = b; b = a; a = t1 + t2
        }
        hash[0] += a; hash[1] += b; hash[2] += c; hash[3] += d
        hash[4] += e; hash[5] += f; hash[6] += g; hash[7] += h
    }
    return hash.joinToString("") { it.toString(16).padStart(8, '0') }
}
