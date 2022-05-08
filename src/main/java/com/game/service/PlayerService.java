package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.List;

public interface PlayerService {
    List<Player> getPlayers(String name, String title, Race race, Profession profession,
                               Long after, Long before, Boolean banned, Integer minExperience,
                               Integer maxExperience, Integer minLevel, Integer maxLevel);
    Player create(Player player);
    void delete(Long id);
    Player update(Player playerOld, Player playerNew);
    Player getPlayerById(Long id);
    boolean isPlayer(Player player);
    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);
    List<Player> sortPlayers(List<Player> players, PlayerOrder order);
}
