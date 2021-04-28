package com.example.htmlstats.service;

import java.util.Map;

public interface StatisticService {

    void analyze(String htmlUrl);

    String download(String htmlUrl);

    Map<String, Long> getStats(String htmlFileName);

    void print(Map<String, Long> map);

}
