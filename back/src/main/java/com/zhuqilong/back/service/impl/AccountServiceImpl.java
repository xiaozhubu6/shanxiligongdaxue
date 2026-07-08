package com.zhuqilong.back.service.impl;

import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public int createAccount(Account account) {
        return accountMapper.insert(account);
    }

    @Override
    public int updateAccount(Account account) {
        return accountMapper.updateById(account);
    }

    @Override
    public int deleteAccount(Long id) {
        return accountMapper.deleteById(id);
    }

    @Override
    public Account getAccountById(Long id) {
        return accountMapper.selectById(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountMapper.selectAll();
    }
}
