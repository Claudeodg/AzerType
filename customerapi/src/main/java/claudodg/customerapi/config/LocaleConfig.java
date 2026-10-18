package claudodg.customerapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * Display language: English (default) or German.
 * <p>
 * The user picks a language with {@code ?lang=en} or {@code ?lang=de} (the EN | DE
 * switcher in the top bar). The choice is stored in a "lang" cookie for one year.
 * Without a cookie, the browser language is used if it is German, English otherwise.
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "de");

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setDefaultLocaleFunction(request ->
                Locale.GERMAN.getLanguage().equals(request.getLocale().getLanguage())
                        ? Locale.GERMAN
                        : Locale.ENGLISH);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleResolver localeResolver = localeResolver();
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String lang = request.getParameter("lang");
                // Only supported languages are accepted, anything else is ignored.
                if (lang != null && SUPPORTED_LANGUAGES.contains(lang)) {
                    localeResolver.setLocale(request, response, Locale.forLanguageTag(lang));
                }
                return true;
            }
        });
    }
}
