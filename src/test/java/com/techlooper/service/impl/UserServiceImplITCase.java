package com.techlooper.service.impl;

import com.techlooper.entity.ProfileEntity;
import com.techlooper.entity.UserEntity;
import com.techlooper.repository.couchbase.UserRepository;
import com.techlooper.service.UserService;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.Arrays;

import static org.junit.Assert.*;

public class UserServiceImplITCase {

    @Resource
    private UserService userService;

    @Test
    public void testSave() throws Exception {
        String key = "4";
        UserEntity user = new UserEntity(key);
        user.setFirstName("Khoa");
        user.setLastName("Nguyen");
        user.setEmailAddress("ndkhoa.is@gmail.com");
        user.setPassword("password");
        user.setLoginSources(Arrays.asList("LinkedIn", "Facebook", "G+", "Github", "Bitbucket", "StackOverflow"));

        ProfileEntity profile = new ProfileEntity();
        profile.setSns("LinkedIn");
        profile.setId("zJROJ4-vlg");
        profile.setLastName("Nguyen");
        profile.setFirstName("Khoa");
        profile.setAccessToken("b1ce044e-ca68-4fab-9590-f2708ffcd04c");
        profile.setHeadline("Java Developer");
        profile.setIndustry("Information Technology and Services");
        profile.setLocation("vn");
        profile.setNumConnections(119);
        profile.setPictureUrl("https://media.licdn.com/mpr/mprx/0_H1YHLaclQdd4lEgNot4ULSbPk7jWlupNo-EBLSF8UEdZmHIqkzmXwDPmE3gB-wjZdry9oov8JZik");
        profile.setPublicProfileUrl("https://www.linkedin.com/in/khoanguyendang");
        user.setProfiles(Arrays.asList(profile));

        userService.save(user);
    }

    @Test
    public void testFindByEmail() throws Exception {
        UserEntity userEntity = userService.findByEmail("ndkhoa.is@gmail.com");
        assertNotNull(userEntity);
        assertEquals(userEntity.getFirstName(), "Khoa");
    }
}