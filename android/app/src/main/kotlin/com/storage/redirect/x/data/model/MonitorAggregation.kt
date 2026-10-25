package com.storage.redirect.x.data.model

// 只合并显示，数据库保留每一条原始记录。
object MonitorAggregation {
    private const val WINDOW_MS = 3000L
    private val GROUPABLE = setOf("READ", "WRITE", "OPEN", "CREATE", "UPDATE")

    private data class Key(
        val process: String,
        val pid: Int,
        val uid: Int,
        val caller: String,
        val path: String,
        val op: String,
        val error: Int?,
        val isSuccess: Boolean,
        val extra: String?,
    )

    fun group(entries: List<FileMonitorEntry>): List<FileMonitorEntry> {
        val result = ArrayList<FileMonitorEntry>(entries.size)
        val indexByKey = HashMap<Key, Int>()
        for (entry in entries) {
            val canGroup = entry.timeMillis > 0 && entry.opType in GROUPABLE
            if (!canGroup) {
                result.add(entry)
                continue
            }
            // 偏移量不影响重复操作合并；错误、调用方与进程必须保持独立。
            val key =
                Key(
                    entry.packageName,
                    entry.processId,
                    entry.uid,
                    entry.caller,
                    entry.path,
                    entry.opType,
                    entry.errorNo,
                    entry.isSuccess,
                    entry.extra?.substringBefore("|offset="),
                )
            val index = indexByKey[key]
            val previous = index?.let(result::get)
            val first = minOf(previous?.timeMillis ?: entry.timeMillis, entry.timeMillis)
            val last = maxOf(previous?.lastTimeMillis ?: entry.lastTimeMillis, entry.lastTimeMillis)
            if (previous != null && last - first <= WINDOW_MS) {
                val latest = if (entry.lastTimeMillis > previous.lastTimeMillis) entry else previous
                result[index] =
                    latest.copy(
                        sequence = maxOf(previous.sequence, entry.sequence),
                        timeMillis = first,
                        lastTimeMillis = last,
                        count = previous.count + entry.count,
                        bytes = previous.bytes + entry.bytes,
                        lostRecords = previous.lostRecords + entry.lostRecords,
                    )
            } else {
                indexByKey[key] = result.size
                result.add(entry)
            }
        }
        return result
    }
}
