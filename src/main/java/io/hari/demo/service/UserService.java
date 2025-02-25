package io.hari.demo.service;

import io.hari.demo.config.AppConfig;
import io.hari.demo.dao.ContestDao;
import io.hari.demo.dao.UserDao;
import io.hari.demo.entity.Contest;
import io.hari.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final QuestionService questionService;
    private final ContestDao contestDao;
    private final AppConfig config;


    public User create(User user) {
        user.setScore(BigInteger.valueOf(1500));
        return userDao.save(user);
    }

    public List<User> createMultiUsers(List<User> users) {
        users.forEach(user -> user.setScore(BigInteger.valueOf(1500)));
        return userDao.saveAll(users);
    }

    public void assignContestToUser(User user, Contest contest) {//low -> 20 , 1,2...20
        setUserContestQuestion(user, contest);
        userDao.save(user);
    }

    public void setUserContestQuestion(User user, Contest contest) {
        validateUserAndContest(user, contest);

        List<Long> questions = new LinkedList<>();
        if (config.getContestQuestionAssignment().equals(AppConfig.ContestQuestionAssignment.all_questions)) {
            questions = contest.getContestQuestions().getQuestions();
        } else if (config.getContestQuestionAssignment().equals(AppConfig.ContestQuestionAssignment.random_questions)) {
            questions = questionService.getContestQuestions(contest);
        }
        final Map<Long, List<Long>> userContestQuestionsMap = user.getUserContestQuestions().getUserContestQuestions();
        if (!userContestQuestionsMap.containsKey(contest.getId())) {
            userContestQuestionsMap.put(contest.getId(), questions);
        }
    }

    public void validateUserAndContest(User user, Contest contest) {
        validateUser(user);
        validateContest(contest);
    }

    public void validateUser(User user) {
        final Optional<User> fetchedUser = userDao.findById(user.getId());
        if (fetchedUser == null) throw new RuntimeException("user not found");
    }

    private void validateContest(Contest contest) {
        final Contest fetchedContest = contestDao.findById(contest.getUserId()).get();
        if (fetchedContest == null) throw new RuntimeException("contest not found");
    }

    public List<User> findAllContestUser(Long contestId) {
        List<User> users = new LinkedList<>();
        userDao.findAll().forEach(user -> {
            final Map<Long, List<Long>> userContestQuestions = user.getUserContestQuestions().getUserContestQuestions();
            if (userContestQuestions.containsKey(contestId)) users.add(user);
        });
        return users;
    }
}
