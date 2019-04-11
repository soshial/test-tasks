package lv.chi.giffoid.app

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList

import java.util.Locale

/**
 * Created by soshial on 30/01/2018.
 * This is needed for Android 7+
 */

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {

        fun wrap(context: Context, newLocale: Locale): ContextWrapper {

            val res = context.resources
            val configuration = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.locales = localeList
            } else {
                configuration.setLocale(newLocale)
            }
            return ContextWrapper(context.createConfigurationContext(configuration))
        }
    }
}
