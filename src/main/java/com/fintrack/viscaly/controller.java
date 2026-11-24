
package com.fintrack.viscaly;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class controller {

    @Autowired
    private service service;

    // ==========================================
    // HOME
    // ==========================================

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    // ==========================================
    // LOGIN PAGE
    // ==========================================

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    // ==========================================
    // LOGIN PROCESS
    // ==========================================

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Long userId = service.login(
                    email.trim().toLowerCase(Locale.ROOT), password);

            if (userId == null) {
                redirectAttributes.addFlashAttribute(
                        "error", "Invalid email or password.");
                return "redirect:/login";
            }

            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("userId", userId);
            newSession.setAttribute(
                    "userName", service.getUserName(userId));

            return "redirect:/dashboard";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error", "Login failed. Please try again.");
            return "redirect:/login";
        }
    }

    // ==========================================
    // REGISTER PAGE
    // ==========================================

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    // ==========================================
    // REGISTER PROCESS
    // ==========================================

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            String cleanName = name.trim();
            String cleanEmail =
                    email.trim().toLowerCase(Locale.ROOT);

            if (cleanName.isEmpty() || cleanEmail.isEmpty()
                    || password.isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "error", "Please fill in all required fields.");
                return "redirect:/register";
            }

            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute(
                        "error", "Passwords do not match.");
                return "redirect:/register";
            }

            if (password.length() < 8) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Password must contain at least 8 characters.");
                return "redirect:/register";
            }

            boolean registered = service.register(
                    cleanName, cleanEmail, password);

            if (!registered) {
                redirectAttributes.addFlashAttribute(
                        "error", "This email is already registered.");
                return "redirect:/register";
            }

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Registration successful. Please log in.");

            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error", "Registration failed. Please try again.");
            return "redirect:/register";
        }
    }

    // ==========================================
    // SESSION USER ID
    // ==========================================

    private Long getUserId(HttpSession session) {

        Object id = session.getAttribute("userId");

        if (id == null) {
            return null;
        }

        return (Long) id;
    }

    // ==========================================
    // COMMON USER DETAILS
    // ==========================================

    private void addUserDetails(Model model, Long userId) {

        model.addAttribute("userId", userId);
        model.addAttribute("userName", service.getUserName(userId));
    }

    // ==========================================
    // DASHBOARD
    // ==========================================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        addUserDetails(model, userId);

        model.addAttribute("monthlyIncome",
                service.getMonthlyIncome(userId));

        model.addAttribute("monthlyExpenses",
                service.getMonthlyExpenses(userId));

        model.addAttribute("monthlySavings",
                service.getMonthlySavings(userId));

        model.addAttribute("totalBalance",
                service.getTotalBalance(userId));

        model.addAttribute("transactions",
                service.getTransactions(userId));

        model.addAttribute("investments",
                service.getInvestments(userId));

        model.addAttribute("goals",
                service.getGoals(userId));

        return "dashboard";
    }

    // ==========================================
    // TRANSACTIONS PAGE
    // ==========================================

    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        addUserDetails(model, userId);

        BigDecimal income = service.getMonthlyIncome(userId);
        BigDecimal expenses = service.getMonthlyExpenses(userId);

        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        model.addAttribute("transactions",
                service.getTransactions(userId));

        model.addAttribute("categories",
                service.getCategories(userId));

        model.addAttribute("totalIncome", income);
        model.addAttribute("totalExpenses", expenses);
        model.addAttribute("netBalance", income.subtract(expenses));

        return "transactions";
    }

    // ==========================================
    // SAVE TRANSACTION
    // ==========================================

    @PostMapping("/transactions/save")
    public String saveTransaction(
            HttpSession session,
            @RequestParam String title,
            @RequestParam BigDecimal amount,
            @RequestParam String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam LocalDate transactionDate,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {

            FinancialTransaction transaction =
                    new FinancialTransaction();

            transaction.setTitle(title);
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setCategoryId(categoryId);
            transaction.setTransactionDate(transactionDate);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setDescription(description);

            service.saveTransaction(userId, transaction);

            redirectAttributes.addFlashAttribute(
                    "success", "Transaction saved successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to save transaction.");
        }

        return "redirect:/transactions";
    }

    // ==========================================
    // DELETE TRANSACTION
    // ==========================================

    @PostMapping("/transactions/delete/{id}")
    public String deleteTransaction(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        service.deleteTransaction(userId, id);

        redirectAttributes.addFlashAttribute(
                "success", "Transaction deleted successfully.");

        return "redirect:/transactions";
    }

    // ==========================================
    // INVESTMENTS PAGE
    // ==========================================

    @GetMapping("/investments")
    public String investments(HttpSession session, Model model) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        addUserDetails(model, userId);

        BigDecimal invested = service.getTotalInvested(userId);
        BigDecimal currentValue =
                service.getCurrentInvestmentValue(userId);

        if (invested == null) invested = BigDecimal.ZERO;
        if (currentValue == null) currentValue = BigDecimal.ZERO;

        model.addAttribute("investments",
                service.getInvestments(userId));

        model.addAttribute("totalInvested", invested);
        model.addAttribute("currentInvestmentValue", currentValue);
        model.addAttribute("totalProfitLoss",
                currentValue.subtract(invested));

        return "investments";
    }

    // ==========================================
    // SAVE INVESTMENT
    // ==========================================

    @PostMapping("/investments/save")
    public String saveInvestment(
            HttpSession session,
            @RequestParam String investmentName,
            @RequestParam String investmentType,
            @RequestParam BigDecimal investedAmount,
            @RequestParam BigDecimal currentValue,
            @RequestParam LocalDate investmentDate,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {

            Investment investment = new Investment();

            investment.setInvestmentName(investmentName);
            investment.setInvestmentType(investmentType);
            investment.setInvestedAmount(investedAmount);
            investment.setCurrentValue(currentValue);
            investment.setInvestmentDate(investmentDate);
            investment.setNotes(notes);

            service.saveInvestment(userId, investment);

            redirectAttributes.addFlashAttribute(
                    "success", "Investment saved successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to save investment.");
        }

        return "redirect:/investments";
    }

    // ==========================================
    // DELETE INVESTMENT
    // ==========================================

    @PostMapping("/investments/delete/{id}")
    public String deleteInvestment(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        service.deleteInvestment(userId, id);

        redirectAttributes.addFlashAttribute(
                "success", "Investment deleted successfully.");

        return "redirect:/investments";
    }

    // ==========================================
    // GOALS PAGE
    // ==========================================

    @GetMapping("/goals")
    public String goals(HttpSession session, Model model) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        addUserDetails(model, userId);

        List<SavingsGoal> goals = service.getGoals(userId);

        BigDecimal totalSaved = BigDecimal.ZERO;
        BigDecimal totalTarget = BigDecimal.ZERO;

        if (goals != null) {

            for (SavingsGoal goal : goals) {

                if (goal.getSavedAmount() != null) {
                    totalSaved =
                            totalSaved.add(goal.getSavedAmount());
                }

                if (goal.getTargetAmount() != null) {
                    totalTarget =
                            totalTarget.add(goal.getTargetAmount());
                }
            }
        }

        model.addAttribute("goals", goals);
        model.addAttribute("totalSaved", totalSaved);
        model.addAttribute("totalTarget", totalTarget);
        model.addAttribute("activeGoals",
                goals == null ? 0 : goals.size());

        return "goals";
    }

    // ==========================================
    // SAVE GOAL
    // ==========================================

    @PostMapping("/goals/save")
    public String saveGoal(
            HttpSession session,
            @RequestParam String goalName,
            @RequestParam BigDecimal targetAmount,
            @RequestParam LocalDate targetDate,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {

            SavingsGoal goal = new SavingsGoal();

            goal.setGoalName(goalName);
            goal.setTargetAmount(targetAmount);
            goal.setTargetDate(targetDate);
            goal.setDescription(description);

            service.saveGoal(userId, goal);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Savings goal created successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to create savings goal.");
        }

        return "redirect:/goals";
    }

    // ==========================================
    // DELETE GOAL
    // ==========================================

    @PostMapping("/goals/delete/{id}")
    public String deleteGoal(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        service.deleteGoal(userId, id);

        redirectAttributes.addFlashAttribute(
                "success", "Savings goal deleted successfully.");

        return "redirect:/goals";
    }

    // ==========================================
    // ADD GOAL CONTRIBUTION
    // ==========================================

    @PostMapping("/goals/contribution")
    public String addContribution(
            HttpSession session,
            @RequestParam Long goalId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        try {

            service.addContribution(userId, goalId, amount, note);

            redirectAttributes.addFlashAttribute(
                    "success", "Contribution added successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to add contribution.");
        }

        return "redirect:/goals";
    }

    // ==========================================
    // ANALYTICS PAGE
    // ==========================================

    @GetMapping("/analytics")
    public String analytics(HttpSession session, Model model) {

        Long userId = getUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        addUserDetails(model, userId);

        BigDecimal income = service.getMonthlyIncome(userId);
        BigDecimal expenses = service.getMonthlyExpenses(userId);

        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        model.addAttribute("totalIncome", income);
        model.addAttribute("totalExpenses", expenses);
        model.addAttribute("netSavings", income.subtract(expenses));

        double savingsRate = 0;

        if (income.compareTo(BigDecimal.ZERO) > 0) {

            savingsRate = income.subtract(expenses)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(income, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        model.addAttribute("savingsRate", savingsRate);

        List<FinancialTransaction> transactions =
                service.getTransactions(userId);

        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        model.addAttribute("transactionCount", transactions.size());

        BigDecimal totalInvested =
                service.getTotalInvested(userId);

        if (totalInvested == null) {
            totalInvested = BigDecimal.ZERO;
        }

        model.addAttribute("totalInvested", totalInvested);

        List<SavingsGoal> goals = service.getGoals(userId);

        model.addAttribute("activeGoals",
                goals == null ? 0 : goals.size());

        model.addAttribute("averageMonthlyExpense", expenses);

        // MONTHLY INCOME VS EXPENSES GRAPH

        List<String> monthlyLabels = new ArrayList<>();
        List<Double> monthlyIncome = new ArrayList<>();
        List<Double> monthlyExpenses = new ArrayList<>();

        Map<YearMonth, BigDecimal> incomeByMonth =
                new LinkedHashMap<>();

        Map<YearMonth, BigDecimal> expensesByMonth =
                new LinkedHashMap<>();

        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(5);

        for (int i = 0; i < 6; i++) {

            YearMonth month = firstMonth.plusMonths(i);

            incomeByMonth.put(month, BigDecimal.ZERO);
            expensesByMonth.put(month, BigDecimal.ZERO);

            monthlyLabels.add(
                    month.format(
                            DateTimeFormatter.ofPattern("MMM yyyy")));
        }

        for (FinancialTransaction transaction : transactions) {

            if (transaction.getTransactionDate() == null
                    || transaction.getAmount() == null
                    || transaction.getType() == null) {
                continue;
            }

            YearMonth transactionMonth =
                    YearMonth.from(transaction.getTransactionDate());

            if (!incomeByMonth.containsKey(transactionMonth)) {
                continue;
            }

            String type =
                    transaction.getType().trim().toUpperCase(Locale.ROOT);

            if ("INCOME".equals(type)) {

                incomeByMonth.put(
                        transactionMonth,
                        incomeByMonth.get(transactionMonth)
                                .add(transaction.getAmount()));

            } else if ("EXPENSE".equals(type)) {

                expensesByMonth.put(
                        transactionMonth,
                        expensesByMonth.get(transactionMonth)
                                .add(transaction.getAmount()));
            }
        }

        for (YearMonth month : incomeByMonth.keySet()) {

            monthlyIncome.add(
                    incomeByMonth.get(month).doubleValue());

            monthlyExpenses.add(
                    expensesByMonth.get(month).doubleValue());
        }

        model.addAttribute("monthlyLabels", monthlyLabels);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpenses", monthlyExpenses);

        // EXPENSE CATEGORY PIE CHART

        List<Category> categories = service.getCategories(userId);

        Map<Long, String> categoryNames = new LinkedHashMap<>();

        if (categories != null) {

            for (Category category : categories) {

                if (category.getId() != null
                        && category.getName() != null) {

                    categoryNames.put(
                            category.getId(), category.getName());
                }
            }
        }

        Map<String, BigDecimal> expenseByCategory =
                new LinkedHashMap<>();

        for (FinancialTransaction transaction : transactions) {

            if (transaction.getAmount() == null
                    || transaction.getType() == null) {
                continue;
            }

            if (!"EXPENSE".equalsIgnoreCase(
                    transaction.getType().trim())) {
                continue;
            }

            String categoryName = "Uncategorized";

            if (transaction.getCategoryId() != null) {

                String savedCategoryName =
                        categoryNames.get(transaction.getCategoryId());

                if (savedCategoryName != null) {
                    categoryName = savedCategoryName;
                }
            }

            expenseByCategory.put(
                    categoryName,
                    expenseByCategory.getOrDefault(
                            categoryName, BigDecimal.ZERO)
                            .add(transaction.getAmount()));
        }

        List<String> expenseLabels = new ArrayList<>();
        List<Double> expenseValues = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry
                : expenseByCategory.entrySet()) {

            expenseLabels.add(entry.getKey());
            expenseValues.add(entry.getValue().doubleValue());
        }

        model.addAttribute("expenseLabels", expenseLabels);
        model.addAttribute("expenseValues", expenseValues);

        return "analytics";
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login";
    }
}
