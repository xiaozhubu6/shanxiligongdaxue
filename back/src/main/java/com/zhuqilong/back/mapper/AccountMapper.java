package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AccountMapper {
    int insert(Account account);
    int updateById(Account account);
    int deleteById(Long id);
    int deleteByElderId(Long elderId);
    Account selectById(Long id);
    List<Account> selectAll();
    Account selectByElderId(Long elderId);
}
