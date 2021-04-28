package com.example.htmlstats.service;

import com.example.htmlstats.repository.StatisticRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class StatisticServiceImplTest {

    @Mock
    StatisticRepository statisticRepository;

    @InjectMocks
    StatisticService statisticService = new StatisticServiceImpl();

    @Test
    void getStatsTest() {
        StatisticServiceImpl service = (StatisticServiceImpl) statisticService;
        service.setCharset("utf-8");
        service.setExcludeTags(true);
        service.setDelimitersPattern("[ ,.!?\";:\\[\\]()\\n\\r\\t]");
        Map<String, Long> expected = new HashMap<>();
        expected.put("A", 2L);
        expected.put("B", 1L);
        expected.put("C", 3L);
        String fileName = getClass().getClassLoader().getResource("test.html").getFile();
        assertEquals(expected, statisticService.getStats(fileName));
    }
}
