package com.example.htmlstats;

import com.example.htmlstats.service.StatisticService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class HtmlStatsApplication implements CommandLineRunner {

    private StatisticService service;

    @Autowired
    public void setService(StatisticService service) {
        this.service = service;
    }

    public static void main(String[] args) {
        SpringApplication.run(HtmlStatsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            if (args.length == 1) {
                service.analyze(args[0]);
                return;
            }
            while (true) {
                log.info("Input url to download or 'exit' to exit... ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    log.info("Good bye!");
                    break;
                }
                service.analyze(input);
            }
        } catch (Exception e) {
            log.error("Unexpected error", e);
        }
    }
}
