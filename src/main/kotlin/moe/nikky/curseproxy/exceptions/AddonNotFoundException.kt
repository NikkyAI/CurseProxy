package moe.nikky.curseproxy.exceptions

/**
 * Created by nikky on 01/05/18.
 * @author Nikky
 * @version 1.0
 */
open class AddonNotFoundException(
        val unknown_id: Int
) : MessageException("Addon not found")