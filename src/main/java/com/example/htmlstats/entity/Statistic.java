package com.example.htmlstats.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    public Statistic(String fileName, Map<String, Long> map) {
        this.fileName = fileName;
        this.map = map;
    }

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDateTime;

    @ElementCollection
    @MapKeyColumn(length = 1023)
    private Map<String, Long> map;

}