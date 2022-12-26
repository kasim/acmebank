package com.acmebank.account_manager.data.repositories;

import com.acmebank.account_manager.data.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
