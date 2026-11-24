
package com.fintrack.viscaly;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class service {

    private final repository repo;

    private static final int HASH_ITERATIONS = 120000;
    private static final int HASH_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public service(repository repo) {
        this.repo = repo;
    }

    // ==========================================
    // 1. PASSWORD HASHING
    // ==========================================

    private String hashPassword(String password) {

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        try {

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    HASH_ITERATIONS,
                    HASH_KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            spec.clearPassword();

            return "PBKDF2$" + HASH_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (GeneralSecurityException e) {

            throw new IllegalStateException(
                    "Password hashing failed.", e);
        }
    }

    private boolean verifyPassword(
            String enteredPassword,
            String storedPassword) {

        if (enteredPassword == null || storedPassword == null) {
            return false;
        }

        try {

            String[] parts = storedPassword.split("\\$", -1);

            if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations <= 0) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (salt.length == 0 || expectedHash.length == 0) {
                return false;
            }

            PBEKeySpec spec = new PBEKeySpec(
                    enteredPassword.toCharArray(),
                    salt,
                    iterations,
                    expectedHash.length * 8
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            spec.clearPassword();

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {

            return false;
        }
    }

    // ==========================================
    // 2. USER REGISTRATION AND LOGIN
    // ==========================================

    public boolean register(String name, String email, String password) {

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {

            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (repo.emailExists(normalizedEmail)) {
            return false;
        }

        UserAccount user = new UserAccount();

        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(hashPassword(password));

        repo.saveUser(user);

        return true;
    }

    public Long login(String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {

            return null;
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        UserAccount user = repo.findUserByEmail(normalizedEmail);

        if (user == null) {
            return null;
        }

        boolean passwordMatches =
                verifyPassword(password, user.getPassword());

        if (!passwordMatches) {
            return null;
        }

        return user.getId();
    }

    public String getUserName(Long userId) {

        UserAccount user = repo.findUserById(userId);

        return user == null ? "User" : user.getName();
    }

    // ==========================================
    // 3. CATEGORY OPERATIONS
    // ==========================================

    public Category saveCategory(Long userId, Category category) {

        category.setUserId(userId);

        return repo.saveCategory(category);
    }

    public List<Category> getCategories(Long userId) {

        return repo.findCategoriesByUser(userId);
    }

    public List<Category> getCategoriesByType(Long userId, String type) {

        return repo.findCategoriesByType(userId, type);
    }

    public void deleteCategory(Long userId, Long categoryId) {

        repo.deleteCategory(categoryId, userId);
    }

    // ==========================================
    // 4. TRANSACTION OPERATIONS
    // ==========================================

    public FinancialTransaction saveTransaction(
            Long userId, FinancialTransaction transaction) {

        transaction.setUserId(userId);

        return repo.saveTransaction(transaction);
    }

    public List<FinancialTransaction> getTransactions(Long userId) {

        return repo.findTransactionsByUser(userId);
    }

    public List<FinancialTransaction> getTransactionsByType(
            Long userId, String type) {

        return repo.findTransactionsByType(userId, type);
    }

    public List<FinancialTransaction> getTransactionsByDate(
            Long userId, LocalDate date) {

        return repo.findTransactionsByDateRange(userId, date, date);
    }

    public void deleteTransaction(Long userId, Long transactionId) {

        repo.deleteTransaction(transactionId, userId);
    }

    // ==========================================
    // 5. BUDGET OPERATIONS
    // ==========================================

    public Budget saveBudget(Long userId, Budget budget) {

        budget.setUserId(userId);

        return repo.saveBudget(budget);
    }

    public List<Budget> getBudgets(Long userId) {

        return repo.findBudgetsByUser(userId);
    }

    public List<Budget> getMonthlyBudgets(
            Long userId, int month, int year) {

        return repo.findBudgetsByMonth(userId, month, year);
    }

    public void deleteBudget(Long userId, Long budgetId) {

        repo.deleteBudget(budgetId, userId);
    }

    // ==========================================
    // 6. INVESTMENT OPERATIONS
    // ==========================================

    public Investment saveInvestment(Long userId, Investment investment) {

        investment.setUserId(userId);

        return repo.saveInvestment(investment);
    }

    public List<Investment> getInvestments(Long userId) {

        return repo.findInvestmentsByUser(userId);
    }

    public BigDecimal getTotalInvested(Long userId) {

        return repo.getTotalInvestedAmount(userId);
    }

    public BigDecimal getCurrentInvestmentValue(Long userId) {

        return repo.getTotalInvestmentValue(userId);
    }

    public void deleteInvestment(Long userId, Long investmentId) {

        repo.deleteInvestment(investmentId, userId);
    }

    // ==========================================
    // 7. SAVINGS GOAL OPERATIONS
    // ==========================================

    public SavingsGoal saveGoal(Long userId, SavingsGoal goal) {

        goal.setUserId(userId);

        return repo.saveGoal(goal);
    }

    public List<SavingsGoal> getGoals(Long userId) {

        return repo.findGoalsByUser(userId);
    }

    public void deleteGoal(Long userId, Long goalId) {

        repo.deleteGoal(goalId, userId);
    }

    // ==========================================
    // 8. SAVINGS CONTRIBUTIONS
    // ==========================================

    public SavingsContribution addContribution(
            Long userId,
            Long goalId,
            BigDecimal amount,
            String note) {

        SavingsGoal goal = repo.findGoalById(goalId, userId);

        if (goal == null || amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        SavingsContribution contribution =
                new SavingsContribution();

        contribution.setUserId(userId);
        contribution.setGoalId(goalId);
        contribution.setAmount(amount);
        contribution.setContributionDate(LocalDate.now());
        contribution.setNote(note);

        BigDecimal currentSaved = goal.getSavedAmount();

        if (currentSaved == null) {
            currentSaved = BigDecimal.ZERO;
        }

        goal.setSavedAmount(currentSaved.add(amount));

        repo.saveGoal(goal);

        return repo.saveContribution(contribution);
    }

    public List<SavingsContribution> getContributions(
            Long goalId, Long userId) {

        return repo.findContributionsByGoal(goalId, userId);
    }

    // ==========================================
    // 9. DASHBOARD CALCULATIONS
    // ==========================================

    public BigDecimal getMonthlyIncome(Long userId) {

        YearMonth currentMonth = YearMonth.now();

        return repo.getTransactionTotal(
                userId,
                "INCOME",
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth()
        );
    }

    public BigDecimal getMonthlyExpenses(Long userId) {

        YearMonth currentMonth = YearMonth.now();

        return repo.getTransactionTotal(
                userId,
                "EXPENSE",
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth()
        );
    }

    public BigDecimal getMonthlySavings(Long userId) {

        return getMonthlyIncome(userId)
                .subtract(getMonthlyExpenses(userId));
    }

    public BigDecimal getTotalBalance(Long userId) {

        LocalDate startDate = LocalDate.of(1900, 1, 1);
        LocalDate endDate = LocalDate.of(9999, 12, 31);

        BigDecimal income = repo.getTransactionTotal(
                userId, "INCOME", startDate, endDate
        );

        BigDecimal expenses = repo.getTransactionTotal(
                userId, "EXPENSE", startDate, endDate
        );

        return income.subtract(expenses);
    }
}
