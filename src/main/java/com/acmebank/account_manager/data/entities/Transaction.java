package com.acmebank.account_manager.data.entities;

import com.acmebank.account_manager.shared.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "from_account", referencedColumnName = "id")
    private Account fromAccount;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "to_account", referencedColumnName = "id")
    private Account toAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private Currency currency;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createAt;
}
