package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.dao.UserDao;
import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.request.UserRequest;
import com.coffee.sweepstakes.model.response.ResponseDto;
import com.coffee.sweepstakes.model.response.UserResponse;
import com.coffee.sweepstakes.util.ErrorConstants;
import com.coffee.sweepstakes.validator.UserValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserValidator userValidator;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private Pageable pageable;
    private User user;
    private UserResponse userResponse;
    private List<User> users;
    private List<UserResponse> userResponseList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setFirstName("John Doe");
        user.setUserEmail("john.doe@example.com");

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setFirstName("John Doe");
        userResponse.setUserEmail("john.doe@example.com");

        users = List.of(user);
        userResponseList = users.stream().map((user) -> userResponse).collect(Collectors.toList());
    }

//    @Test
//    public void testSaveUser() {
//
//        UserRequest userRequest = new UserRequest();
//
//        doNothing().when(userValidator).requestValidator(userRequest);
//        when(mapper.convertValue(userRequest, User.class)).thenReturn(user);
//        when(userDao.saveUser(user)).thenReturn(user);
//
//        ResponseDto<User> response = userService.saveUser(userRequest);
//
//        assertNotNull(response);
//        assertEquals(0, response.getStatus());
//        assertEquals(1, response.getData().size());
//        assertEquals(user, response.getData().get(0));
//
//        verify(userValidator).requestValidator(userRequest);
//        verify(mapper).convertValue(userRequest, User.class);
//        verify(userDao).saveUser(user);
//    }

    @Test
    public void testSaveUser_ValidatorException() {

        UserRequest userRequest = new UserRequest();

        doThrow(new IllegalArgumentException("Validation error")).when(userValidator).requestValidator(userRequest);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(userRequest);
        });

        assertEquals("Validation error", exception.getMessage());

        verify(userValidator).requestValidator(userRequest);
        verify(mapper, never()).convertValue(any(), eq(User.class));
        verify(userDao, never()).saveUser(any());
    }

    @Test
    public void testSaveUserMapperException() {

        UserRequest userRequest = new UserRequest();

        doNothing().when(userValidator).requestValidator(userRequest);
        when(mapper.convertValue(userRequest, User.class)).thenThrow(new RuntimeException("Mapping error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.saveUser(userRequest);
        });

        assertEquals("Mapping error", exception.getMessage());
        verify(userValidator).requestValidator(userRequest);
        verify(mapper).convertValue(userRequest, User.class);
        verify(userDao, never()).saveUser(any());
    }

    @Test
    public void testSaveUserDaoException() {

        UserRequest userRequest = new UserRequest();

        doNothing().when(userValidator).requestValidator(userRequest);
        when(mapper.convertValue(userRequest, User.class)).thenReturn(user);
        when(userDao.saveUser(user)).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.saveUser(userRequest);
        });

        assertEquals("Database error", exception.getMessage());
        verify(userValidator).requestValidator(userRequest);
        verify(mapper).convertValue(userRequest, User.class);
        verify(userDao).saveUser(user);
    }

//    @Test
//    public void testSaveUserFullyPopulatedUserRequest() {
//
//        UserRequest userRequest = new UserRequest();
//        userRequest.setFirstName("John Doe");
//        userRequest.setUserEmail("john@example.com");
//
//        doNothing().when(userValidator).requestValidator(userRequest);
//        when(mapper.convertValue(userRequest, User.class)).thenReturn(user);
//        when(userDao.saveUser(user)).thenReturn(user);
//
//        ResponseDto<User> response = userService.saveUser(userRequest);
//
//        assertNotNull(response);
//        assertEquals(0, response.getStatus());
//        assertEquals(1, response.getData().size());
//        assertEquals(user, response.getData().get(0));
//
//        verify(userValidator).requestValidator(userRequest);
//        verify(mapper).convertValue(userRequest, User.class);
//        verify(userDao).saveUser(user);
//    }

    @Test
    public void testGetByIdSuccess() {

        when(userDao.getUserById(userId)).thenReturn(Optional.of(user));
        when(mapper.convertValue(user, UserResponse.class)).thenReturn(userResponse);

        ResponseDto<UserResponse> response = userService.getById(userId);

        assertNotNull(response);
        assertEquals(0, response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(userResponse, response.getData().get(0));

        verify(userDao).getUserById(userId);
        verify(mapper).convertValue(user, UserResponse.class);
    }

    @Test
    public void testGetByIdUserNotFound() {

        when(userDao.getUserById(userId)).thenReturn(Optional.empty());

        CoffeeException exception = assertThrows(CoffeeException.class, () -> userService.getById(userId));

        assertEquals(ErrorConstants.NOT_FOUND_ERROR_CODE, exception.getErrorCode());
        assertEquals(MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "User"), exception.getErrorMessage());

        verify(userDao).getUserById(userId);
        verify(mapper, never()).convertValue(any(), eq(UserResponse.class));
    }

    @Test
    public void testGetAllUsersNoSearchAndNoFilter() {
        Page<User> userPage = new PageImpl<>(users);

        when(userDao.getAllUsers(pageable)).thenReturn(userPage);
        when(mapper.convertValue(user, UserResponse.class)).thenReturn(userResponse);

        ResponseDto<UserResponse> response = userService.getAllUsers(pageable, "", "");
        assertNotNull(response);
        assertEquals(0, response.getStatus());
        assertEquals(userResponseList, response.getData());
        assertEquals(users.size(), response.getTotal());
        assertEquals(users.size(), response.getCount());

        verify(userDao).getAllUsers(pageable);
        verify(mapper).convertValue(user, UserResponse.class);
    }

    @Test
    public void testGetAllUsers_SearchProvidedNoFilter() {

        String search = "John";
        Page<User> userPage = new PageImpl<>(users);

        when(userDao.getAllUsersBySearch(pageable, search)).thenReturn(userPage);
        when(mapper.convertValue(user, UserResponse.class)).thenReturn(userResponse);

        ResponseDto<UserResponse> response = userService.getAllUsers(pageable, search, "");

        assertNotNull(response);
        assertEquals(0, response.getStatus());
        assertEquals(userResponseList, response.getData());
        assertEquals(users.size(), response.getTotal());
        assertEquals(users.size(), response.getCount());

        verify(userDao).getAllUsersBySearch(pageable, search);
        verify(mapper).convertValue(user, UserResponse.class);
    }

    @Test
    public void testGetAllUsers_FilterProvidedNoSearch() {

        String filter = "active";
        Page<User> userPage = new PageImpl<>(users);

        when(userDao.getUsersByEventCode(pageable, filter)).thenReturn(userPage);
        when(mapper.convertValue(user, UserResponse.class)).thenReturn(userResponse);

        ResponseDto<UserResponse> response = userService.getAllUsers(pageable, "", filter);

        assertNotNull(response);
        assertEquals(0, response.getStatus());
        assertEquals(userResponseList, response.getData());
        assertEquals(users.size(), response.getTotal());
        assertEquals(users.size(), response.getCount());

        verify(userDao).getUsersByEventCode(pageable, filter);
        verify(mapper).convertValue(user, UserResponse.class);
    }

    @Test
    public void testGetAllUsersSearchAndFilterProvided() {
        String search = "John";
        String filter = "active";
        Page<User> userPage = new PageImpl<>(users);

        when(userDao.getAllUsersWithSearchAndEventCode(pageable, search, filter)).thenReturn(userPage);
        when(mapper.convertValue(user, UserResponse.class)).thenReturn(userResponse);

        ResponseDto<UserResponse> response = userService.getAllUsers(pageable, search, filter);

        assertNotNull(response);
        assertEquals(0, response.getStatus());
        assertEquals(userResponseList, response.getData());
        assertEquals(users.size(), response.getTotal());
        assertEquals(users.size(), response.getCount());

        verify(userDao).getAllUsersWithSearchAndEventCode(pageable, search, filter);
        verify(mapper).convertValue(user, UserResponse.class);
    }

    @Test
    public void testUpdateIsActiveById() {
        Boolean isActive = true;

        userService.updateIsActiveById(userId, isActive);
        verify(userDao, times(1)).updateIsActiveById(userId, isActive);
    }

    @Test
    public void testUpdateIsActiveByIdNullIsActive() {
        userService.updateIsActiveById(userId, null);
        verify(userDao, times(1)).updateIsActiveById(userId, null);
    }

    @Test
    public void testUpdateIsActiveById_isActiveFalse() {
        Boolean isActive = false;
        userService.updateIsActiveById(userId, isActive);
        verify(userDao, times(1)).updateIsActiveById(userId, isActive);
    }


}
