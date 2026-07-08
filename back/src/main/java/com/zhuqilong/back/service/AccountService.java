package com.zhuqilong.back.service;

import com.zhuqilong.back.entity.Account;
import java.util.List;

public interface AccountService {
    int createAccount(Account account);
    int updateAccount(Account account);
    int deleteAccount(Long id);
    Account getAccountById(Long id);
    List<Account> getAllAccounts();
}
