package com.example.htmlstats.service;

import com.example.htmlstats.entity.Statistic;
import com.example.htmlstats.repository.StatisticRepository;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Setter
public class StatisticServiceImpl implements StatisticService {

    private StatisticRepository repository;

    @Autowired
    public void setRepository(StatisticRepository repository) {
        this.repository = repository;
    }

    @Value("${htmlStats.delimiters:{' ', ',', '.', '!', '?', '\"', ';', ':', '[', ']', '(', ')', '\\n', '\\r', '\\t'}}")
    private String delimiters;

    @Value("${htmlStats.tempFileName:downloaded.html}")
    private String tempFileName;

    @Value("${htmlStats.userAgent:Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0}")
    private String userAgent;

    @Value("${htmlStats.accept:text/html}")
    private String accept;

    @Value("${htmlStats.charset:utf-8}")
    private String charset;

    @Value("${htmlStats.excludeTags:true}")
    private boolean excludeTags;

    private String delimitersPattern;

    @PostConstruct
    private void setDelimitersPattern() {
        StringBuilder sb = new StringBuilder("[");
        Arrays.stream(delimiters.substring(1, delimiters.length() - 1).trim().split(", "))
                .map(String::trim)
                .map(s -> s.substring(1, s.length() - 1))
                .map(s -> s.contains("[") || s.contains("]") ? Pattern.quote(s) : s)
                .forEach(sb::append);
        delimitersPattern = sb.append("]").toString();
    }

    @Override
    public void analyze(String htmlUrl) {
        print(getStats(download(htmlUrl)));
    }

    @Override
    public String download(String htmlUrl) {
        if (isWrongUrl(htmlUrl)) {
            log.error(String.format("Incorrect URL %s", htmlUrl));
            return "";
        }
        try (InputStream inputStream = getConnection(htmlUrl).getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
             BufferedReader reader = new BufferedReader(inputStreamReader);
             FileWriter writer = new FileWriter(tempFileName, Charset.forName(charset))) {
            log.info("Downloading...");
            String input;
            while ((input = reader.readLine()) != null) {
                writer.write(input);
                writer.write(System.lineSeparator());
            }
            log.info("Done");
        } catch (IOException e) {
            log.error(String.format("Can't download HTML from URL %s", htmlUrl), e);
            return "";
        }
        return tempFileName;
    }

    private boolean isWrongUrl(String url) {
        String urlPattern = "^https*:\\/\\/\\S+\\s*$";
        return url == null || !url.toLowerCase().matches(urlPattern);
    }

    private HttpURLConnection getConnection(String htmlUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(htmlUrl).openConnection();
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Accept", accept);
        return connection;
    }

    @Override
    public Map<String, Long> getStats(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Collections.emptyMap();
        }
        log.info("Getting statistics...");
        Map<String, Long> map;
        try {
            if (excludeTags && hasMemoryForFile(fileName)) {
                String text = Jsoup.parse(new File(fileName), charset).body().text();
                map = Arrays.stream(text.split(delimitersPattern))
                        .filter(s -> !s.isEmpty())
                        .map(String::toUpperCase)
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            } else {
                map = Files.lines(Path.of(fileName), Charset.forName(charset))
                        .map(line -> excludeTags ? Jsoup.parseBodyFragment(line).text() : line)
                        .flatMap(line -> Stream.of(line.split(delimitersPattern)))
                        .filter(s -> !s.isEmpty())
                        .map(String::toUpperCase)
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            }
        } catch (IOException e) {
            log.error(String.format("Can't read file %s", fileName), e);
            return Collections.emptyMap();
        }
        repository.save(new Statistic(fileName, map));
        log.info("Done");
        return map;
    }

    private boolean hasMemoryForFile(String fileName) throws IOException {
        long fileSize = Files.size(Path.of(fileName));
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freeMemory = Runtime.getRuntime().maxMemory() - usedMemory;
        return freeMemory > 10 * fileSize;
    }

    @Override
    public void print(Map<String, Long> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        String separator = "============================================";
        System.out.println(separator);
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        System.out.println(separator);
    }

}
