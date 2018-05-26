package moe.nikky.curseproxy.exceptions

/**
 * Created by nikky on 03/03/18.
 * @author Nikky
 * @version 1.0
 */
data class MissingParameterException(
        val missing: String
) : MessageException("Parameter not found")