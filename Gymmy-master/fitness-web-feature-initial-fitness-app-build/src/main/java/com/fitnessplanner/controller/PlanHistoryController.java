package com.fitnessplanner.controller;

import com.fitnessplanner.model.UserPlanHistory;
import com.fitnessplanner.service.PlanHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/history/plans")
public class PlanHistoryController {

    private final PlanHistoryService planHistoryService;

    @Autowired
    public PlanHistoryController(PlanHistoryService planHistoryService) {
        this.planHistoryService = planHistoryService;
    }

    @GetMapping
    public String showPlanHistory(Model model, Principal principal,
                                  @RequestParam(name = "type", required = false) String type) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        List<UserPlanHistory> planHistory;

        UserPlanHistory.PlanType filterType = null;
        if (type != null && !type.isEmpty()) {
            try {
                filterType = UserPlanHistory.PlanType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid type, log or ignore, show all by default
                model.addAttribute("errorMessage", "Invalid plan type filter: " + type);
            }
        }

        if (filterType != null) {
            planHistory = planHistoryService.getPlanHistoryForUserByType(username, filterType);
            model.addAttribute("filterType", filterType.toString());
        } else {
            planHistory = planHistoryService.getPlanHistoryForUser(username);
        }

        model.addAttribute("planHistory", planHistory);
        return "plan-history"; // Renders plan-history.html
    }
}
