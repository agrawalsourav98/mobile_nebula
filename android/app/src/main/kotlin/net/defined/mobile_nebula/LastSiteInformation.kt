package net.defined.mobile_nebula

import java.io.File
import android.content.Context
import android.util.JsonWriter
import android.util.JsonReader
import java.io.InputStreamReader

const val filename: String = "last_used_site.json"

class LastSiteInformation {
    companion object{
        fun getLastSite(context: Context): Pair<String?, String?> {
            try {
                context.openFileInput(filename).use {
                    var lastId: String? = null
                    var lastPath: String? = null
                    val jsonReader = JsonReader(InputStreamReader(it))
                    jsonReader.use { reader ->
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "id" -> lastId = reader.nextString()
                                "path" -> lastPath = reader.nextString()
                                else -> reader.skipValue()
                            }
                        }
                    }
                    if (lastId != null) {
                        val site = Site(context, File(lastPath!!))
                        if (checkIfSiteIsValid(context,site))
                            return Pair(lastId, lastPath)
                    }
                    return getFirstValidSite(context)
                }

            } catch (e: Exception) {
                return getFirstValidSite(context)
            }
        }

        private fun getFirstValidSite(context: Context): Pair<String?, String?> {
            val siteList = SiteList(context)

            val sites: Map<String, Site> = siteList.getSites()

            val iter = sites.keys.iterator()

            return if (iter.hasNext()) {
                val firstKey = iter.next()
                val firstVal = sites[firstKey]
                Pair(firstKey, firstVal!!.path)
            } else {
                Pair(null, null)
            }
        }

        private fun checkIfSiteIsValid(context: Context, site: Site): Boolean {
            return try {
                site.getKey(context)

                // Make sure we can load the DN credentials if managed
                if (site.managed) {
                    site.getDNCredentials(context)
                }
                true
            } catch (err: Exception) {
                false
            }

        }

        fun updateLastSite(context: Context, runningSiteID: String?, path: String?) {
            if (runningSiteID != null) {
                context.openFileOutput(
                    filename,
                    Context.MODE_PRIVATE
                ).use {
                    val writer = JsonWriter(java.io.OutputStreamWriter(it))
                    writer.setIndent("  ")
                    writer.beginObject()
                    writer.name("id").value(runningSiteID)
                    writer.name("path").value(path!!)
                    writer.endObject()
                    writer.close()
                }
            }
        }
    }
}