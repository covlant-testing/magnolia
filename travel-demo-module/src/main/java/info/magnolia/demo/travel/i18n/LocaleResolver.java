/**
 * This file Copyright (c) 2024 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 */
package info.magnolia.demo.travel.i18n;

import info.magnolia.cms.i18n.I18nContentSupport;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Central helper for resolving {@link Locale} values used by rendering models.
 * Extracted from {@code NavigationAreaModel} so other models can reuse the
 * same null-safe resolution rules.
 */
@Singleton
public class LocaleResolver {

    private final I18nContentSupport i18nContentSupport;

    @Inject
    public LocaleResolver(I18nContentSupport i18nContentSupport) {
        this.i18nContentSupport = i18nContentSupport;
    }

    /**
     * Resolves a {@link Locale} from the given language tag. Falls back to the
     * platform default when the input is null or empty.
     */
    public Locale resolve(String language) {
        if (language == null || language.isEmpty()) {
            return Locale.getDefault();
        }
        return i18nContentSupport.determineLocaleFromString(language);
    }

    /**
     * Returns the ISO 639-1 language code of the resolved locale.
     */
    public String languageCode(String language) {
        return resolve(language).getLanguage();
    }
}
