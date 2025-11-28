package cn.iocoder.lawsaas.module.member.api.level;

import cn.iocoder.lawsaas.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.lawsaas.module.member.convert.level.MemberLevelConvert;
import cn.iocoder.lawsaas.module.member.enums.MemberExperienceBizTypeEnum;
import cn.iocoder.lawsaas.module.member.service.level.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.lawsaas.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.lawsaas.module.member.enums.ErrorCodeConstants.EXPERIENCE_BIZ_NOT_SUPPORT;

/**
 * 会员等级 API 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class MemberLevelApiImpl implements MemberLevelApi {

    @Resource
    private MemberLevelService memberLevelService;

    @Override
    public MemberLevelRespDTO getMemberLevel(Long id) {
        return MemberLevelConvert.INSTANCE.convert02(memberLevelService.getLevel(id));
    }

    @Override
    public void addExperience(Long userId, Integer experience, Integer bizType, String bizId) {
        MemberExperienceBizTypeEnum bizTypeEnum = MemberExperienceBizTypeEnum.getByType(bizType);
        if (bizTypeEnum == null) {
            throw exception(EXPERIENCE_BIZ_NOT_SUPPORT);
        }
        memberLevelService.addExperience(userId, experience, bizTypeEnum, bizId);
    }

    @Override
    public void reduceExperience(Long userId, Integer experience, Integer bizType, String bizId) {
        addExperience(userId, -experience, bizType, bizId);
    }

}
