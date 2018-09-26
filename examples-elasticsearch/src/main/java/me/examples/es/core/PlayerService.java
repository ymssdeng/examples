package me.examples.es.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author denghui
 * @create 2018/8/24
 */
@Service
@Slf4j
public class PlayerService extends AbstractElasticService<Player> {

    public PlayerService() {
        super(Player.class);
    }

    @Override
    protected String getIndex() {
        return "player";
    }

    @Override
    protected String getType() {
        return "player";
    }

}
