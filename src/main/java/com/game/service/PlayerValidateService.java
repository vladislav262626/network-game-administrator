package com.game.service;
import com.game.dto.PlayerDto;
import com.game.exception.InvalidIdException;
import com.game.exception.NoSuchPlayerException;
import com.game.exception.ValidationException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class PlayerValidateService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerValidateService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    private boolean isPlayerDtoValidForCreate(PlayerDto playerDto) {
        if (isNullInDtoParamsForCreate(playerDto)) {
            return false;
        }
        return (isNameLenValid(playerDto) && isTitleLenValid(playerDto) && isExperienceValid(playerDto) && isBirthdayValid(playerDto));
    }

    public void validatePlayerForCreate(PlayerDto playerDto) {
        if (!isPlayerDtoValidForCreate(playerDto)) {
            throw new ValidationException();
        }
    }


    private boolean isNullInDtoParamsForCreate(PlayerDto playerDto) {
        boolean isNullInDtoParams =
                playerDto.getName() == null ||
                        playerDto.getTitle() == null ||
                        playerDto.getRace() == null ||
                        playerDto.getProfession() == null ||
                        playerDto.getBirthday() == null ||
                        playerDto.getExperience() == null;

        return isNullInDtoParams;
    }


    boolean isNameLenValid(PlayerDto playerDto) {
        int nameLen = playerDto.getName().trim().length();
        return nameLen > 0 && nameLen <= 12;
    }


    boolean isTitleLenValid(PlayerDto playerDto) {
        int titleLen = playerDto.getTitle().trim().length();
        return titleLen > 0 && titleLen <= 30;
    }


    boolean isExperienceValid(PlayerDto playerDto) {
        return playerDto.getExperience() >= 0 && playerDto.getExperience() <= 10000000;
    }


    boolean isBirthdayValid(PlayerDto playerDto) {
        if (playerDto.getBirthday() < 0) {
            return false;
        }
        Date date = new Date(playerDto.getBirthday());
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();

        return date.getTime() >= 0 && (year >= 2000 && year <= 3000);
    }

    public void validateId(Long id) {
        if (id <= 0) {
            throw new InvalidIdException();
        } else if (id > playerRepository.count() || !playerRepository.findById(id).isPresent()) {
            throw new NoSuchPlayerException();
        }
    }

    void validateBirthday(PlayerDto playerDto) {
        if (!isBirthdayValid(playerDto)) {
            throw new ValidationException();
        }
    }

    void validateExperience(PlayerDto playerDto) {
        if (!isExperienceValid(playerDto)) {
            throw new ValidationException();
        }
    }
}
