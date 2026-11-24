
package com.fintrack.viscaly;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;


// ==========================================
// 1. USER ENTITY
// ==========================================

@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt = LocalDateTime.now();

    public UserAccount() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


// ==========================================
// 2. CATEGORY ENTITY
// ==========================================

@Entity
@Table(name = "categories")
class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    public Category() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getType() { return type; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
}


// ==========================================
// 3. FINANCIAL TRANSACTION ENTITY
// ==========================================

@Entity
@Table(name = "transactions")
class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long categoryId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String paymentMethod;

    private String description;

    private LocalDate transactionDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    public FinancialTransaction() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDescription() { return description; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


// ==========================================
// 4. BUDGET ENTITY
// ==========================================

@Entity
@Table(name = "budgets")
class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long categoryId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetAmount;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    public Budget() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCategoryId() { return categoryId; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
    public void setMonth(int month) { this.month = month; }
    public void setYear(int year) { this.year = year; }
}


// ==========================================
// 5. INVESTMENT ENTITY
// ==========================================

@Entity
@Table(name = "investments")
class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String investmentName;

    private String investmentType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal investedAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal currentValue;

    private LocalDate investmentDate;

    private String notes;

    public Investment() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getInvestmentName() { return investmentName; }
    public String getInvestmentType() { return investmentType; }
    public BigDecimal getInvestedAmount() { return investedAmount; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public LocalDate getInvestmentDate() { return investmentDate; }
    public String getNotes() { return notes; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }
    public void setInvestmentType(String investmentType) {
        this.investmentType = investmentType;
    }
    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = investedAmount;
    }
    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }
    public void setInvestmentDate(LocalDate investmentDate) {
        this.investmentDate = investmentDate;
    }
    public void setNotes(String notes) { this.notes = notes; }
}


// ==========================================
// 6. SAVINGS GOAL ENTITY
// ==========================================

@Entity
@Table(name = "savings_goals")
class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String goalName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    private LocalDate targetDate;

    private String description;

    public SavingsGoal() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getGoalName() { return goalName; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public LocalDate getTargetDate() { return targetDate; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setGoalName(String goalName) { this.goalName = goalName; }
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}


// ==========================================
// 7. SAVINGS CONTRIBUTION ENTITY
// ==========================================

@Entity
@Table(name = "savings_contributions")
class SavingsContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long goalId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDate contributionDate;

    private String note;

    public SavingsContribution() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getGoalId() { return goalId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public String getNote() { return note; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public void setContributionDate(LocalDate contributionDate) {
        this.contributionDate = contributionDate;
    }
    public void setNote(String note) { this.note = note; }
}
