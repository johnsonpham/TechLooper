package com.techlooper.service;

import com.techlooper.entity.JobOfferInfo;
import com.techlooper.entity.userimport.UserImportEntity;
import com.techlooper.model.JobOfferEvaluation;

import java.util.Map;

/**
 * Created by NguyenDangKhoa on 3/19/15.
 */
public interface UserEvaluationService {

  long score(UserImportEntity user, Map<String, Long> totalJobPerSkillMap);

  double rate(UserImportEntity user, Map<String, Long> totalJobPerSkillMap, Long totalITJobs);

  Map<String, Integer> rank(UserImportEntity user);

  Map<String, Long> getSkillMap();

  Map<String, Long> getTotalNumberOfJobPerSkill();

  JobOfferEvaluation evaluateJobOffer(JobOfferInfo jobOfferInfo);
}
