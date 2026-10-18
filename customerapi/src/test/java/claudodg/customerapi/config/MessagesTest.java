package claudodg.customerapi.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Makes sure no text is missing in one of the two languages.
 */
class MessagesTest {

    @Test
    void englishAndGermanHaveTheSameKeys() throws IOException {
        Properties english = load("/messages.properties");
        Properties german = load("/messages_de.properties");

        assertThat(german.stringPropertyNames())
                .as("keys in messages_de.properties")
                .containsExactlyInAnyOrderElementsOf(english.stringPropertyNames());
    }

    @Test
    void noTextIsEmpty() throws IOException {
        for (String file : new String[]{"/messages.properties", "/messages_de.properties"}) {
            Properties messages = load(file);
            messages.forEach((key, value) ->
                    assertThat(value.toString()).as(file + " -> " + key).isNotBlank());
        }
    }

    private static Properties load(String path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(MessagesTest.class.getResourceAsStream(path), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
