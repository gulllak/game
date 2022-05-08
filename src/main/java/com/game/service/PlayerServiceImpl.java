package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    public PlayerServiceImpl(){
    }

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getPlayers(String name, String title, Race race, Profession profession,
                                   Long after, Long before, Boolean banned, Integer minExperience,
                                   Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> players = new ArrayList<>();

        playerRepository.findAll().forEach(player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && player.getBirthday().getTime() < after) return;
            if (before != null && player.getBirthday().getTime() > before) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            players.add(player);
        });

        return players;
    }

    @Override
    public Player create(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public Player update(Player playerOld, Player playerNew) {
        String name = playerNew.getName();
        if(name != null) {
            if (name.length() > 0 && name.length() <= 12) {
                playerOld.setName(name);
            }
        }

        String title = playerNew.getTitle();
        if(title != null) {
            if (title.length() > 0 && title.length() <= 30) {
                playerOld.setTitle(title);
            }
        }

        Race race = playerNew.getRace();
        if(race != null) {
            playerOld.setRace(race);
        }

        Profession profession = playerNew.getProfession();
        if(profession != null) {
            playerOld.setProfession(profession);
        }

        Date birthday = playerNew.getBirthday();
        if(birthday != null) {
            if (isBirthdayOk(birthday)) {
                playerOld.setBirthday(birthday);
            } else {
                throw new IllegalArgumentException();
            }
        }

        Boolean banned = playerNew.getBanned();
        if(banned != null) {
            playerOld.setBanned(banned);
        }

        Integer experience = playerNew.getExperience();
        if(experience != null) {
            if(experience >= 0 && experience <= 10000000) {
                playerOld.setExperience(experience);

                playerOld.setLevel((int) (Math.sqrt(2500 + 200 * playerOld.getExperience()) - 50) / 100);
                playerOld.setUntilNextLevel(50 * (playerOld.getLevel() + 1) * (playerOld.getLevel() + 2) - playerOld.getExperience());
            } else {
                throw new IllegalArgumentException();
            }
        }

        playerRepository.save(playerOld);
        return playerOld;
    }

    @Override
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public boolean isPlayer(Player player) {
        if (player != null) {
            if(player.getName() != null && player.getName().length() <= 12 && !player.getName().isEmpty()) {
                if(player.getTitle().length() > 0 && player.getTitle().length() <= 30) {
                    if(player.getRace() != null) {
                        if(player.getProfession() != null) {
                            if(isBirthdayOk(player.getBirthday())) {
                                return player.getExperience() != null && player.getExperience() >= 0 && player.getExperience() <= 10000000;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }

    private boolean isBirthdayOk(Date date) {
        Calendar after = Calendar.getInstance();
        after.set(Calendar.YEAR, 2000);
        Calendar before = Calendar.getInstance();
        before.set(Calendar.YEAR, 3000);

        return date != null &&
                date.getTime() >= after.getTimeInMillis() &&
                date.getTime() <= before.getTimeInMillis();
    }

    @Override
    public List<Player> sortPlayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((player1, player2) -> {
                switch (order) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    case LEVEL: return player1.getLevel().compareTo(player2.getLevel());
                    default: return 0;
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        int page = pageNumber == null ? 0 : pageNumber;
        int size = pageSize == null ? 3 : pageSize;
        int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }
}
