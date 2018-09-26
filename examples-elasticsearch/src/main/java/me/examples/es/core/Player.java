package me.examples.es.core;

import lombok.Data;

/**
 * @author denghui
 * @create 2018/8/23
 */
@Data
public class Player implements ElasticDocument {

    private String id;
    private String name;
    private String birthday;
    
}
