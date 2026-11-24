
package com.fintrack.viscaly;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class repository {

    @PersistenceContext
    private EntityManager em;

    // ==========================================
    // 1. USER DATABASE OPERATIONS
    // ==========================================

    public UserAccount saveUser(UserAccount user) {
        if (user.getId() == null) {
            em.persist(user);
            em.flush();
            return user;
        }

        UserAccount updatedUser = em.merge(user);
        em.flush();
        return updatedUser;
    }

    public UserAccount findUserById(Long id) {
        return em.find(UserAccount.class, id);
    }

    public UserAccount findUserByEmail(String email) {
        List<UserAccount> users = em.createQuery(
                "SELECT u FROM UserAccount u WHERE LOWER(u.email) = :email",
                UserAccount.class)
                .setParameter("email", email.toLowerCase())
                .setMaxResults(1)
                .getResultList();

        return users.isEmpty() ? null : users.get(0);
    }

    public boolean emailExists(String email) {
        Long count = em.createQuery(
                "SELECT COUNT(u) FROM UserAccount u WHERE LOWER(u.email) = :email",
                Long.class)
                .setParameter("email", email.toLowerCase())
                .getSingleResult();

        return count > 0;
    }

    // ==========================================
    // 2. CATEGORY DATABASE OPERATIONS
    // ==========================================

    public Category saveCategory(Category category) {
        if (category.getId() == null) {
            em.persist(category);
            return category;
        }
        return em.merge(category);
    }

    public Category findCategoryById(Long id, Long userId) {
        List<Category> result = em.createQuery(
                "SELECT c FROM Category c WHERE c.id = :id AND c.userId = :userId",
                Category.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<Category> findCategoriesByUser(Long userId) {
        return em.createQuery(
                "SELECT c FROM Category c WHERE c.userId = :userId ORDER BY c.name",
                Category.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Category> findCategoriesByType(Long userId, String type) {
        return em.createQuery(
                "SELECT c FROM Category c WHERE c.userId = :userId AND c.type = :type ORDER BY c.name",
                Category.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .getResultList();
    }

    public void deleteCategory(Long id, Long userId) {
        Category category = findCategoryById(id, userId);
        if (category != null) {
            em.remove(category);
        }
    }

    // ==========================================
    // 3. TRANSACTION DATABASE OPERATIONS
    // ==========================================

    public FinancialTransaction saveTransaction(FinancialTransaction transaction) {
        if (transaction.getId() == null) {
            em.persist(transaction);
            return transaction;
        }
        return em.merge(transaction);
    }

    public FinancialTransaction findTransactionById(Long id, Long userId) {
        List<FinancialTransaction> result = em.createQuery(
                "SELECT t FROM FinancialTransaction t WHERE t.id = :id AND t.userId = :userId",
                FinancialTransaction.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<FinancialTransaction> findTransactionsByUser(Long userId) {
        return em.createQuery(
                "SELECT t FROM FinancialTransaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC, t.id DESC",
                FinancialTransaction.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<FinancialTransaction> findTransactionsByType(Long userId, String type) {
        return em.createQuery(
                "SELECT t FROM FinancialTransaction t WHERE t.userId = :userId AND t.type = :type ORDER BY t.transactionDate DESC",
                FinancialTransaction.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .getResultList();
    }

    public List<FinancialTransaction> findTransactionsByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        return em.createQuery(
                "SELECT t FROM FinancialTransaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC",
                FinancialTransaction.class)
                .setParameter("userId", userId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<FinancialTransaction> findTransactionsByCategory(
            Long userId, Long categoryId) {

        return em.createQuery(
                "SELECT t FROM FinancialTransaction t WHERE t.userId = :userId AND t.categoryId = :categoryId ORDER BY t.transactionDate DESC",
                FinancialTransaction.class)
                .setParameter("userId", userId)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    public BigDecimal getTransactionTotal(
            Long userId, String type, LocalDate startDate, LocalDate endDate) {

        BigDecimal total = em.createQuery(
                "SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.userId = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate",
                BigDecimal.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getSingleResult();

        return total == null ? BigDecimal.ZERO : total;
    }

    public void deleteTransaction(Long id, Long userId) {
        FinancialTransaction transaction = findTransactionById(id, userId);
        if (transaction != null) {
            em.remove(transaction);
        }
    }

    // ==========================================
    // 4. BUDGET DATABASE OPERATIONS
    // ==========================================

    public Budget saveBudget(Budget budget) {
        if (budget.getId() == null) {
            em.persist(budget);
            return budget;
        }
        return em.merge(budget);
    }

    public Budget findBudgetById(Long id, Long userId) {
        List<Budget> result = em.createQuery(
                "SELECT b FROM Budget b WHERE b.id = :id AND b.userId = :userId",
                Budget.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<Budget> findBudgetsByUser(Long userId) {
        return em.createQuery(
                "SELECT b FROM Budget b WHERE b.userId = :userId ORDER BY b.year DESC, b.month DESC",
                Budget.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Budget> findBudgetsByMonth(Long userId, int month, int year) {
        return em.createQuery(
                "SELECT b FROM Budget b WHERE b.userId = :userId AND b.month = :month AND b.year = :year",
                Budget.class)
                .setParameter("userId", userId)
                .setParameter("month", month)
                .setParameter("year", year)
                .getResultList();
    }

    public void deleteBudget(Long id, Long userId) {
        Budget budget = findBudgetById(id, userId);
        if (budget != null) {
            em.remove(budget);
        }
    }

    // ==========================================
    // 5. INVESTMENT DATABASE OPERATIONS
    // ==========================================

    public Investment saveInvestment(Investment investment) {
        if (investment.getId() == null) {
            em.persist(investment);
            return investment;
        }
        return em.merge(investment);
    }

    public Investment findInvestmentById(Long id, Long userId) {
        List<Investment> result = em.createQuery(
                "SELECT i FROM Investment i WHERE i.id = :id AND i.userId = :userId",
                Investment.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<Investment> findInvestmentsByUser(Long userId) {
        return em.createQuery(
                "SELECT i FROM Investment i WHERE i.userId = :userId ORDER BY i.investmentDate DESC",
                Investment.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public BigDecimal getTotalInvestedAmount(Long userId) {
        BigDecimal total = em.createQuery(
                "SELECT SUM(i.investedAmount) FROM Investment i WHERE i.userId = :userId",
                BigDecimal.class)
                .setParameter("userId", userId)
                .getSingleResult();

        return total == null ? BigDecimal.ZERO : total;
    }

    public BigDecimal getTotalInvestmentValue(Long userId) {
        BigDecimal total = em.createQuery(
                "SELECT SUM(i.currentValue) FROM Investment i WHERE i.userId = :userId",
                BigDecimal.class)
                .setParameter("userId", userId)
                .getSingleResult();

        return total == null ? BigDecimal.ZERO : total;
    }

    public void deleteInvestment(Long id, Long userId) {
        Investment investment = findInvestmentById(id, userId);
        if (investment != null) {
            em.remove(investment);
        }
    }

    // ==========================================
    // 6. SAVINGS GOAL DATABASE OPERATIONS
    // ==========================================

    public SavingsGoal saveGoal(SavingsGoal goal) {
        if (goal.getId() == null) {
            em.persist(goal);
            return goal;
        }
        return em.merge(goal);
    }

    public SavingsGoal findGoalById(Long id, Long userId) {
        List<SavingsGoal> result = em.createQuery(
                "SELECT g FROM SavingsGoal g WHERE g.id = :id AND g.userId = :userId",
                SavingsGoal.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<SavingsGoal> findGoalsByUser(Long userId) {
        return em.createQuery(
                "SELECT g FROM SavingsGoal g WHERE g.userId = :userId ORDER BY g.targetDate",
                SavingsGoal.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = findGoalById(id, userId);
        if (goal != null) {
            em.remove(goal);
        }
    }

    // ==========================================
    // 7. SAVINGS CONTRIBUTION DATABASE OPERATIONS
    // ==========================================

    public SavingsContribution saveContribution(SavingsContribution contribution) {
        if (contribution.getId() == null) {
            em.persist(contribution);
            return contribution;
        }
        return em.merge(contribution);
    }

    public SavingsContribution findContributionById(Long id, Long userId) {
        List<SavingsContribution> result = em.createQuery(
                "SELECT c FROM SavingsContribution c WHERE c.id = :id AND c.userId = :userId",
                SavingsContribution.class)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<SavingsContribution> findContributionsByGoal(Long goalId, Long userId) {
        return em.createQuery(
                "SELECT c FROM SavingsContribution c WHERE c.goalId = :goalId AND c.userId = :userId ORDER BY c.contributionDate DESC",
                SavingsContribution.class)
                .setParameter("goalId", goalId)
                .setParameter("userId", userId)
                .getResultList();
    }

    public void deleteContribution(Long id, Long userId) {
        SavingsContribution contribution = findContributionById(id, userId);
        if (contribution != null) {
            em.remove(contribution);
        }
    }
}
