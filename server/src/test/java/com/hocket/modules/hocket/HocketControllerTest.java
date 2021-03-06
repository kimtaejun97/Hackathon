package com.hocket.modules.hocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hocket.modules.account.Account;
import com.hocket.modules.account.AccountFactory;
import com.hocket.modules.account.AccountRepository;
import com.hocket.modules.account.AccountService;
import com.hocket.modules.category.CategoryRepository;
import com.hocket.modules.image.Image;
import com.hocket.modules.image.ImageRepository;
import com.hocket.modules.likeheart.LikeHeartRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class HocketControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    HocketRepository hocketRepository;

    @Autowired
    AccountFactory accountFactory;
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    HocketService hocketService;
    @Autowired
    LikeHeartRepository likeHeartRepository;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    AccountService accountService;
    @Autowired
    HocketFactory hocketFactory;
    @Autowired
    ImageRepository imageRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @LocalServerPort
    int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();


    @BeforeEach
    void cleanUp(){
        likeHeartRepository.deleteAll();
        imageRepository.deleteAll();
        hocketRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @DisplayName("Simple Hocket List ?????????")
    @Test
    void getSimpleList() throws Exception {

        String token = UUID.randomUUID().toString();

        Account account = accountFactory.createNewAccount("?????????", "test@email.com");
        when(accountService.getAccountIdByToken(token)).thenReturn(account.getId());

        Hocket hocket = hocketFactory.createNewHocket(account, token);

        MvcResult mvcResult = mockMvc.perform(get("/hocket/simpleList")
                .param("token", token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode =  objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        assertThat(jsonNode.findValue("numberOfHearts").asInt()).isEqualTo(1);
        assertThat(jsonNode.findValue("title").textValue()).isEqualTo("title");
        assertThat(jsonNode.findValue("id").asLong()).isEqualTo(hocket.getId());

    }

    @DisplayName("?????? ????????? ????????? ?????????")
    @Test
    void hocketImages() throws Exception {


        String token = UUID.randomUUID().toString();
        Account account = accountFactory.createNewAccount("?????????", "test@email.com");

        when(accountService.getAccountIdByToken(token)).thenReturn(account.getId());

        Hocket hocket = hocketFactory.createNewHocket(account, token);

        LocalDateTime now =LocalDateTime.now();

        Image image = new Image();
        image.setHocket(hocket);
        image.setAddDateTime(now);
        image.setUrl("http://local.test");

        imageRepository.save(image);

        Image image2 = new Image();
        image2.setHocket(hocket);
        image2.setAddDateTime(now);
        image2.setUrl("http://local.test2");

        imageRepository.save(image2);


        MvcResult mvcResult = mockMvc.perform(get("/hocket/images")
                .param("token", token)
                .param("hocketId", String.valueOf(hocket.getId())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode result = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        assertThat(result.get(0).get("url").textValue()).isEqualTo("http://local.test");
        assertThat(result.get(0).get("addDateTime").textValue()).isEqualTo(now.toString());

    }

    @DisplayName("?????? ?????? ?????? ")
    @Test
    void hocketDetails() throws Exception {

        String token = UUID.randomUUID().toString();
        Account account = accountFactory.createNewAccount("?????????", "test@email.com");
        Hocket hocket = hocketFactory.createNewHocket(account, token);

        MvcResult mvcResult = mockMvc.perform(get("/hocket/details")
                .param("hocketId", String.valueOf(hocket.getId())))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(mvcResult.getResponse());

        JsonNode hocketDetails = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        assertThat(hocketDetails.findValue("title").asText()).isEqualTo("title");
        assertThat(hocketDetails.findValue("numberOfHearts").asInt()).isEqualTo(1);
        assertThat(hocketDetails.findValue("categoryTitles").asText().contains("home"));
        assertThat(hocketDetails.findValue("categoryTitles").asText().contains("etc"));

    }
    @DisplayName("?????? ?????? ?????? - ???????????? ??????")
    @Test
    void hocketDetails_doseNotExists() throws Exception {

        String url ="http://localhost:"+port+"/hocket/details";


        mockMvc.perform(get(url)
                .param("hocketId", String.valueOf(1)))
                .andExpect((result -> assertTrue(result.getResolvedException().getMessage().equals("Invalid Input"))));


    }

    @DisplayName("????????? ??????????????? ????????? ????????????")
    @Test
    void categoryHocketList() throws Exception {

        Account account = accountFactory.createNewAccount("?????????", "test@email.com");

        Hocket hocket = hocketFactory.createNewHocket(account, UUID.randomUUID().toString());
        Hocket hocket2 = hocketFactory.createNewHocket(account, UUID.randomUUID().toString());


        MvcResult mvcResult = mockMvc.perform(get("/hocket/category")
                .param("category", "home"))
                .andExpect(status().isOk())
                .andReturn();

        String result = mvcResult.getResponse().getContentAsString();
        assertNotNull(result);

        JsonNode resultNode = objectMapper.readTree(result);
        assertThat(resultNode.size()).isEqualTo(2);

    }

    @DisplayName("????????? ??????????????? ????????? ???????????? - ?????? ??????????????? ????????? ??????.")
    @Test
    void categoryHocketList_nag() throws Exception {

        Account account = accountFactory.createNewAccount("?????????", "test@email.com");

        Hocket hocket = hocketFactory.createNewHocket(account, UUID.randomUUID().toString());
        Hocket hocket2 = hocketFactory.createNewHocket(account, UUID.randomUUID().toString());


        MvcResult mvcResult = mockMvc.perform(get("/hocket/category")
                .param("category", "challenge"))
                .andExpect(status().isOk())
                .andReturn();

        String result = mvcResult.getResponse().getContentAsString();
        assertThat(result).isEqualTo("[]");
    }




}