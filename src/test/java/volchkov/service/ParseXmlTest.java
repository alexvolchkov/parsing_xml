package volchkov.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import volchkov.ParseXml;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Aleksandr Volchkov
 */
class ParseXmlTest {

    @Test
    void parsingAddrObj() {
        List<String> expected = List.of("1422396: ул Северная",
                "1450759: р-н Заполярный",
                "1449192: п Нельмин Нос",
                "1451562: п Екуша");
        List<String> actual = ParseXml.parsingAddrObj("2010-01-01", "1422396, 1450759, 1449192, 1451562");
        Assertions.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    void parsingAdmHierarchy() {
        String expected = "АО Ненецкий, г Нарьян-Мар, проезд Лесопильщиков";
        String actual = ParseXml.parsingAdmHierarchy("проезд").get(0);
        assertThat(actual).isEqualTo(expected);
    }

}