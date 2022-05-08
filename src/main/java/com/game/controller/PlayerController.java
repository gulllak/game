package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {

    private PlayerService playerService;

    public PlayerController() {
    }

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.GET)
    public ResponseEntity<List<Player>> getPlayers(@RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   @RequestParam(value = "race", required = false) Race race,
                                                   @RequestParam(value = "profession", required = false) Profession profession,
                                                   @RequestParam(value = "after", required = false) Long after,
                                                   @RequestParam(value = "before", required = false) Long before,
                                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                                   @RequestParam(value = "order", required = false) PlayerOrder order,
                                                   @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                   @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        final List<Player> players = playerService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        final List<Player> orderBy = playerService.sortPlayers(players, order);
        final List<Player> playersOnPage = playerService.getPage(orderBy, pageNumber, pageSize);
        return new ResponseEntity<>(playersOnPage, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/count", method = RequestMethod.GET)
    public Integer getPlayersCount(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "race", required = false) Race race,
                                   @RequestParam(value = "profession", required = false) Profession profession,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {
        return playerService.getPlayers(name, title, race, profession, after, before, banned, minExperience,
                maxExperience, minLevel, maxLevel).size();
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.GET)
    public ResponseEntity<Player> getPlayerById(@PathVariable(value = "id") String idString) {
        try {
            Long.parseLong(idString);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        long id = Long.parseLong(idString);
        if(id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Player player = playerService.getPlayerById(id);
        if(player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Player> delete(@PathVariable(value = "id") String idString) {
        try {
            Long.parseLong(idString);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Long id = Long.parseLong(idString);
        if(id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Player player = playerService.getPlayerById(id);

        if(player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        playerService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if(!playerService.isPlayer(player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if(player.getBanned() == null) player.setBanned(false);

        player.setLevel((int) (Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());

        Player newPlayer = playerService.create(player);
        return new ResponseEntity<>(newPlayer, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@RequestBody Player newPlayer,
                                               @PathVariable(value = "id") String idString) {
        long id = Long.parseLong(idString);
        if(id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<Player> responseEntity = getPlayerById(idString);
        Player oldPlayer = responseEntity.getBody();
        if(oldPlayer == null) {
            return responseEntity;
        }

        Player updatePlayer;
        try {
            updatePlayer = playerService.update(oldPlayer, newPlayer);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(updatePlayer, HttpStatus.OK);
    }
}
