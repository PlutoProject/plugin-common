package club.plutomc.plutoproject.apiutils.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.function.UnaryOperator

object GsonProvider {

     var gson = Gson()
        private set

    fun applies(operate: UnaryOperator<GsonBuilder>) {
        synchronized(this) {
            gson = operate.apply(gson.newBuilder()).create()
        }
    }

}