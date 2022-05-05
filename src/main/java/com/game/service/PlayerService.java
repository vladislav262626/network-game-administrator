package com.game.service;
import com.game.controller.PlayerOrder;
import com.game.dto.FilterDto;
import com.game.dto.PlayerDto;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerValidateService playerValidationService;
    private FilterDto filterDto;
    private Specification<Player> specification = new Specification<Player>() {
        @Override
        public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getName() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + filterDto.getName().toLowerCase() + "%"));
            }
            if (filterDto.getTitle() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + filterDto.getTitle().toLowerCase() + "%"));
            }
            if (filterDto.getRace() != null) {
                predicates.add(criteriaBuilder.equal(root.get("race"), filterDto.getRace()));
            }
            if (filterDto.getProfession() != null) {
                predicates.add(criteriaBuilder.equal(root.get("profession"), filterDto.getProfession()));
            }
            if (filterDto.getMinExperience() < filterDto.getMaxExperience()) {
                predicates.add(criteriaBuilder.between(root.get("experience"), filterDto.getMinExperience(), filterDto.getMaxExperience()));
            } else if (filterDto.getMinExperience() != 0 && filterDto.getMaxExperience() == 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), filterDto.getMinExperience()));
            } else if (filterDto.getMinExperience() == 0 && filterDto.getMaxExperience() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("experience"), filterDto.getMaxExperience()));
            }

            Date before = new Date(filterDto.getBefore());
            Date after = new Date(filterDto.getAfter());
            if (filterDto.getAfter() != 0 && filterDto.getBefore() != 0 && filterDto.getAfter() < filterDto.getBefore()) {
                predicates.add(criteriaBuilder.between(root.get("birthday"), after, before));
            } else if (filterDto.getAfter() != 0 && filterDto.getBefore() == 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), after));
            } else if (filterDto.getAfter() == 0 && filterDto.getBefore() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), before));
            }

            if (filterDto.getBanned() != null) {
                predicates.add(criteriaBuilder.equal(root.get("banned"), filterDto.getBanned()));
            }

            if (filterDto.getMinLevel() != 0 && filterDto.getMaxLevel() != 0 && filterDto.getMinLevel() < filterDto.getMaxLevel()) {
                predicates.add(criteriaBuilder.between(root.get("level"), filterDto.getMinLevel(), filterDto.getMaxLevel()));
            } else if (filterDto.getMinLevel() != 0 && filterDto.getMaxLevel() == 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("level"), filterDto.getMinLevel()));
            } else if (filterDto.getMinLevel() == 0 && filterDto.getMaxLevel() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("level"), filterDto.getMaxLevel()));
            }

            orderBy(filterDto, root, criteriaBuilder, query);

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }
    };

    @Autowired
    public PlayerService(PlayerRepository playerRepository, PlayerValidateService playerValidationService) {
        this.playerRepository = playerRepository;
        this.playerValidationService = playerValidationService;
    }

    private void setFilterDto(FilterDto filterDto) {
        this.filterDto = filterDto;
    }


    public int playersCount(FilterDto filterDto) {
        setFilterDto(filterDto);
        if (filterDto == null) {
            return Math.toIntExact(playerRepository.count());
        }
        return Math.toIntExact(playerRepository.count(specification));
    }


    public Player findById(Long id) {
        return playerRepository.findById(id).get();
    }


    public Player createPlayer(PlayerDto playerDto) {
        Player player = new Player();
        return playerRepository.save(mapDtoToEntity(player, playerDto));
    }


    public Page<Player> getListPlayers(FilterDto filterDto) {
        setFilterDto(filterDto);

        Pageable pageable = PageRequest.of(filterDto.getPageNumber(), filterDto.getPageSize());

        Page<Player> page = playerRepository.findAll(specification, pageable);

        return page;
    }


    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }


    public Player updatePlayer(Long playerId, PlayerDto playerDto) {
        Player player = findById(playerId);

        if (playerDto.getName() != null && playerValidationService.isNameLenValid(playerDto)) {
            player.setName(playerDto.getName());
        }
        if (playerDto.getTitle() != null && playerValidationService.isTitleLenValid(playerDto)) {
            player.setTitle(playerDto.getTitle());
        }
        if (playerDto.getRace() != null) {
            player.setRace(playerDto.getRace());
        }
        if (playerDto.getProfession() != null) {
            player.setProfession(playerDto.getProfession());
        }

        if (playerDto.getBirthday() != null) {
            playerValidationService.validateBirthday(playerDto);
            player.setBirthday(new Date(playerDto.getBirthday()));
        }

        if (playerDto.getBanned() != null) {
            player.setBanned(playerDto.getBanned());
        }

        if (playerDto.getExperience() != null) {
            playerValidationService.validateExperience(playerDto);
            player.setExperience(playerDto.getExperience());

            int level = levelCalculations(playerDto);
            player.setLevel(level);

            int untilNextLevel = untilNextLevelCalculations(playerDto, level);
            player.setUntilNextLevel(untilNextLevel);
        }
        return playerRepository.saveAndFlush(player);
    }

    private Player mapDtoToEntity(Player player, PlayerDto playerDto) {
        player.setName(playerDto.getName());
        player.setTitle(playerDto.getTitle());
        player.setRace(playerDto.getRace());
        player.setProfession(playerDto.getProfession());
        player.setBirthday(new Date(playerDto.getBirthday()));
        player.setBanned(playerDto.getBanned());
        player.setExperience(playerDto.getExperience());

        int level = levelCalculations(playerDto);
        player.setLevel(level);
        player.setUntilNextLevel(untilNextLevelCalculations(playerDto, level));

        return player;
    }

    private Integer levelCalculations(PlayerDto playerDto) {
        int exp = playerDto.getExperience();
        return (((int) (Math.sqrt(2500 + 200 * exp)) - 50) / 100);
    }

    private Integer untilNextLevelCalculations(PlayerDto playerDto, Integer lvl) {
        int exp = playerDto.getExperience();
        return (50 * (lvl + 1) * (lvl + 2) - exp);
    }

    private void orderBy(FilterDto filterDto, Root<Player> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query) {
        Order order = null;
        switch (filterDto.getOrder()) {
            case ID:
                order = criteriaBuilder.asc(root.get("id"));
                break;
            case NAME:
                order = criteriaBuilder.asc(root.get("name"));
                break;
            case EXPERIENCE:
                order = criteriaBuilder.asc(root.get("experience"));
                break;
            case BIRTHDAY:
                order = criteriaBuilder.asc(root.get("birthday"));
                break;
            case LEVEL:
                order = criteriaBuilder.asc(root.get("level"));
                break;
        }
        query.orderBy(order);
    }

    public FilterDto mapRequestParamToFilterDto(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel,
            PlayerOrder order,
            Integer pageSize,
            Integer pageNumber
    ) {
        FilterDto filterDto = new FilterDto();
        filterDto.setName(name);
        filterDto.setTitle(title);
        filterDto.setRace(race);
        filterDto.setProfession(profession);
        filterDto.setAfter(after);
        filterDto.setBefore(before);
        filterDto.setBanned(banned);
        filterDto.setMinExperience(minExperience);
        filterDto.setMaxExperience(maxExperience);
        filterDto.setMinLevel(minLevel);
        filterDto.setMaxLevel(maxLevel);
        filterDto.setOrder(order);
        filterDto.setPageSize(pageSize);
        filterDto.setPageNumber(pageNumber);
        return filterDto;
    }
}
