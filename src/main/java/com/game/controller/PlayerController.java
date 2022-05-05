package com.game.controller;

import com.game.dto.FilterDto;
import com.game.dto.PlayerDto;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import com.game.service.PlayerValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {
    private final PlayerService playerService;
    private final PlayerValidateService playerValidationService;

    @Autowired
    public PlayerController(PlayerService playerService, PlayerValidateService playerValidationService) {
        this.playerService = playerService;
        this.playerValidationService = playerValidationService;
    }


    @GetMapping("/count")
    public int getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false, defaultValue = "0") Long after,
            @RequestParam(value = "before", required = false, defaultValue = "0") Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false, defaultValue = "0") Integer minExperience,
            @RequestParam(value = "maxExperience", required = false, defaultValue = "0") Integer maxExperience,
            @RequestParam(value = "minLevel", required = false, defaultValue = "0") Integer minLevel,
            @RequestParam(value = "maxLevel", required = false, defaultValue = "0") Integer maxLevel,
            @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize
    ) {
        FilterDto filterDto = playerService.mapRequestParamToFilterDto(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);
        return playerService.playersCount(filterDto);
    }


    @GetMapping("/{id}")
    public Player getByID(@PathVariable Long id) {
        playerValidationService.validateId(id);
        return playerService.findById(id);
    }


    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        playerValidationService.validateId(id);
        playerService.deletePlayer(id);
    }


    @PostMapping
    public Player createPlayer(@RequestBody PlayerDto playerDto) {
        playerValidationService.validatePlayerForCreate(playerDto);
        return playerService.createPlayer(playerDto);
    }


    @PostMapping("/{id}")
    public Player updatePlayer(@RequestBody PlayerDto playerDto, @PathVariable(required = false) Long id) {
        playerValidationService.validateId(id);
        return playerService.updatePlayer(id, playerDto);
    }


    @GetMapping
    public List<Player> getPlayersList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false, defaultValue = "0") Long after,
            @RequestParam(value = "before", required = false, defaultValue = "0") Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false, defaultValue = "0") Integer minExperience,
            @RequestParam(value = "maxExperience", required = false, defaultValue = "0") Integer maxExperience,
            @RequestParam(value = "minLevel", required = false, defaultValue = "0") Integer minLevel,
            @RequestParam(value = "maxLevel", required = false, defaultValue = "0") Integer maxLevel,
            @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize
    ) {
        FilterDto filterDto = playerService.mapRequestParamToFilterDto(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageSize, pageNumber);

        return playerService.getListPlayers(filterDto).getContent();
    }
}
