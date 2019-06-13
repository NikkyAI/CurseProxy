package moe.nikky.curseproxy.exceptions

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonFileNotFoundException(
        val unknown_id: Int,
        val unknown_file_id: Int
) : MessageException("AddonFile not found")