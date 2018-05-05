package moe.nikky.curseproxy

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */

object VersionComparator : Comparator<String> {
    override fun compare(o1: String, o2: String): Int {
        val v1 = o1.split(".")
        val v2 = o2.split(".")
        var i = 0
        while (true) {
            val part1 = v1.getOrNull(i)
            val part2 = v2.getOrNull(i)

            if (part1 == null && part2 == null) return 0

            val version1 = part1?.toIntOrNull() ?: return -1
            val version2 = part2?.toIntOrNull() ?: return 1

            if (version1 != version2) {
                return Integer.compare(version1, version2)
            }
            i++
        }
    }

}