package cn.iocoder.lawsaas.module.system.job;

import cn.iocoder.lawsaas.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.lawsaas.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.lawsaas.framework.quartz.core.handler.JobHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DemoJob implements JobHandler {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public String execute(String param) {
        List<AdminUserDO> users = adminUserMapper.selectList();
        return "用户数量：" + users.size();
    }

}
