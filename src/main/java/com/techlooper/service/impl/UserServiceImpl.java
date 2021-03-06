package com.techlooper.service.impl;

import com.techlooper.entity.userimport.UserImportEntity;
import com.techlooper.entity.vnw.dto.VnwUserDto;
import com.techlooper.model.*;
import com.techlooper.repository.elasticsearch.SalaryReviewRepository;
import com.techlooper.repository.talentsearch.query.TalentSearchQuery;
import com.techlooper.repository.userimport.UserImportRepository;
import com.techlooper.repository.vnw.VnwUserRepo;
import com.techlooper.service.*;
import org.dozer.Mapper;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryFilterBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

/**
 * Created by NguyenDangKhoa on 12/11/14.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserImportRepository userImportRepository;

    @Resource
    private Mapper dozerMapper;

    @Resource
    private VietnamWorksUserService vietnamworksUserService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private SalaryReviewRepository salaryReviewRepository;

    @Resource
    private VnwUserRepo vnwUserRepo;

    @Resource
    private MimeMessage fromTechlooperMailMessage;

    @Resource
    private JobAggregatorService jobAggregatorService;

    @Value("${web.baseUrl}")
    private String baseUrl;

    @Resource
    private EmailService emailService;

    public boolean addCrawledUser(UserImportEntity userImportData, SocialProvider socialProvider) {
        UserImportEntity userImportEntity = findUserImportByEmail(userImportData.getEmail());

        if (userImportEntity != null) {
            userImportEntity.withProfile(socialProvider, userImportData.getProfiles().get(socialProvider));
        } else {
            userImportEntity = dozerMapper.map(userImportData, UserImportEntity.class);
            userImportEntity.withProfile(socialProvider, userImportData.getProfiles().get(socialProvider));
            userImportEntity.setCrawled(true);
        }

        return userImportRepository.save(userImportEntity) != null;
    }

    public int addCrawledUserAll(List<UserImportEntity> users, SocialProvider socialProvider, UpdateModeEnum updateMode) {
        List<UserImportEntity> shouldBeSavedUsers = new ArrayList<>();

        for (UserImportEntity user : users) {
            try {
                UserImportEntity userImportEntity = findUserImportByEmail(user.getEmail());

                switch (updateMode) {
                    case ADD_NEW:
                        if (userImportEntity == null) {
                            shouldBeSavedUsers.add(user);
                        }
                        break;
                    case OVERWRITE:
                        if (userImportEntity != null) {
                            userImportEntity.withProfile(socialProvider, user.getProfiles().get(socialProvider));
                            shouldBeSavedUsers.add(userImportEntity);
                        } else {
                            shouldBeSavedUsers.add(user);
                        }
                        break;
                    default: //MERGE
                        if (userImportEntity != null) {
                            userImportEntity.mergeProfile(socialProvider, user.getProfiles().get(socialProvider));
                            shouldBeSavedUsers.add(userImportEntity);
                        } else {
                            shouldBeSavedUsers.add(user);
                        }
                        break;
                }
            } catch (Exception ex) {
                LOGGER.error("User Import Fail : " + user.getEmail(), ex);
            }
        }

        return Lists.newArrayList(userImportRepository.save(shouldBeSavedUsers)).size();
    }

    public UserImportEntity findUserImportByEmail(String email) {
        UserImportEntity userImportEntity = userImportRepository.findOne(email);

        if (userImportEntity != null) {
            return userImportEntity;
        } else {
            QueryFilterBuilder queryFilterBuilder = FilterBuilders.queryFilter(
                    QueryBuilders.queryStringQuery(email).defaultOperator(QueryStringQueryBuilder.Operator.AND)).cache(true);
            SearchQuery userSearchQuery = new NativeSearchQueryBuilder()
                    .withFilter(queryFilterBuilder)
                    .withPageable(new PageRequest(0, 10))
                    .build();
            Page<UserImportEntity> result = userImportRepository.search(userSearchQuery);
            if (result.getNumberOfElements() > 0) {
                return result.getContent().get(0);
            } else {
                return null;
            }
        }

    }

    public List<UserImportEntity> getAll(int pageNumber, int pageSize) {
        final SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery()
                        .mustNot(wildcardQuery("email", "*users.noreply.github.com"))
                        .mustNot(wildcardQuery("email", "*missing.com")))
                .withPageable(new PageRequest(pageNumber, pageSize))
                .build();

        return userImportRepository.search(searchQuery).getContent();
    }

    public TalentSearchResponse findTalent(TalentSearchRequest param) {
        List<SocialProvider> socialProviders = Arrays.asList(SocialProvider.GITHUB);
        TalentSearchResponse.Builder builder = new TalentSearchResponse.Builder();

        for (SocialProvider socialProvider : socialProviders) {
            TalentSearchQuery talentSearchQuery =
                    applicationContext.getBean(socialProvider + "TalentSearchQuery", TalentSearchQuery.class);
            ElasticsearchRepository talentSearchRepository =
                    applicationContext.getBean(socialProvider + "TalentSearchRepository", ElasticsearchRepository.class);
            TalentSearchDataProcessor talentSearchDataProcessor =
                    applicationContext.getBean(socialProvider + "TalentSearchDataProcessor", TalentSearchDataProcessor.class);

            TalentSearchRequest customizedParam = param;
            if (param != null) {
                talentSearchDataProcessor.normalizeInputParameter(param);
            } else {
                customizedParam = talentSearchDataProcessor.getSearchAllRequestParameter();
            }

            FacetedPage<UserImportEntity> pageResult = talentSearchRepository.search(talentSearchQuery.getSearchQuery(customizedParam));
            builder.withTotal(pageResult.getTotalElements());
            builder.withResult(talentSearchDataProcessor.process(pageResult.getContent()));
        }

        return builder.build();
    }

    public SalaryReviewDto findSalaryReviewById(String base64Id) {
        Long id = Long.parseLong(new String(Base64.getDecoder().decode(base64Id)));
        return dozerMapper.map(salaryReviewRepository.findOne(id), SalaryReviewDto.class);
    }

    public VnwUserDto findVnwUserByUsername(String username) {
        return dozerMapper.map(vnwUserRepo.findByUsernameIgnoreCase(username), VnwUserDto.class);
    }

    public boolean sendOnBoardingEmail(String email, Language language) {
        boolean existUser = vietnamworksUserService.existUser(email);

        if (!existUser) {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("webBaseUrl", baseUrl);
            templateModel.put("totalOfItJobs", jobAggregatorService.findJob(new JobSearchCriteria()).getJobs().size());

            EmailRequestModel emailRequestModel = new EmailRequestModel.Builder()
                    .withTemplateName(EmailTemplateNameEnum.ONBOARDING.name())
                    .withLanguage(language)
                    .withTemplateModel(templateModel)
                    .withMailMessage(fromTechlooperMailMessage)
                    .withRecipientAddresses(email).build();
            emailService.sendMail(emailRequestModel);
        }
        return true;
    }

}
