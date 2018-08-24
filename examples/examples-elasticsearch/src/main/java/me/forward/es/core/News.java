package me.forward.es.core;

import lombok.Data;
import me.forward.es.core.ElasticDocument;

/**
 * @author denghui
 * @create 2018/8/23
 */
@Data
public class News implements ElasticDocument {

    private String id;
    private String title;
    private String publishTime;

}
