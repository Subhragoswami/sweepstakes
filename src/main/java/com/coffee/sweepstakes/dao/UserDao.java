package com.coffee.sweepstakes.dao;

import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDao {
    private final UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUserByEventCodeAndEmail(String eventCode, String email) {
        return userRepository.findByEventCodeAndUserEmailIgnoreCase(eventCode, email);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> getAllUsersBySearch(Pageable pageable, String search) {
        return userRepository.getAllBySearch(pageable, search);
    }

    public Page<User> getAllUsersWithSearchAndEventCode(Pageable pageable, String search, String eventCode) {
        return userRepository.getAllUsersWithSearchAndEventCode(pageable, search, eventCode);
    }

    public Page<User> getUsersByEventCode(Pageable pageable, String eventCode) {
        return userRepository.getAllUsersByEventCode(pageable, eventCode);
    }

    public boolean existsByUserEmail(String email, String eventCode) {
        return userRepository.existsByUserEmail(email, eventCode);
    }

    public void updateIsActiveById(UUID userId, Boolean isActive) {
        userRepository.updateIsActiveById(userId, isActive);
    }

    public List<Object[]> getEmailCampaignStatsWithUniqueClicks() {
        return userRepository.getEmailCampaignStatsForAllEvents();
    }
    public List<Object[]> findUserCountByEventCode(String eventCode, Date startdate, Date endDate ) {
        return userRepository.findUserCountByEventCode(eventCode, startdate, endDate);
    }

}
