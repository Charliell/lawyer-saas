package cn.iocoder.lawsaas.module.infra.dal.mysql.db;

import cn.iocoder.lawsaas.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.lawsaas.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapperX<DataSourceConfigDO> {
}
