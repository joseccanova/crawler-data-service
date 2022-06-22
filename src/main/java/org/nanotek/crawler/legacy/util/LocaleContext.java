package org.nanotek.crawler.legacy.util;

import java.util.Locale;
import java.util.Optional;

public class LocaleContext {
	
    private static ThreadLocal<Locale> currentLocale = new InheritableThreadLocal<>();
    
    public static final Locale DEFAULT_LOCALE_ID = Locale.getDefault();
    
    public static Locale getCurrentLocale() {
        return Optional.ofNullable(currentLocale.get()).orElse(DEFAULT_LOCALE_ID);
    }

    public static void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }

    public static void clear() {
        currentLocale.set(null);
    }
    
}