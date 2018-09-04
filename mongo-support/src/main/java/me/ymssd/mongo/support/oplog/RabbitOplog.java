package me.ymssd.mongo.support.oplog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author denghui
 * @create 2018/9/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RabbitOplog {

    private char op;
    private int time;
    private int inc;
    private String ns;
    private String o;
    private String o2;
}
