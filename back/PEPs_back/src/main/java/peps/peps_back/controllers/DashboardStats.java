package peps.peps_back.controllers;

public class DashboardStats {
    private int totalInteractions;
    private int activeModules;
    private String lastInteraction;

    public DashboardStats() {
    }

    public DashboardStats(int totalInteractions, int activeModules, String lastInteraction) {
        this.totalInteractions = totalInteractions;
        this.activeModules = activeModules;
        this.lastInteraction = lastInteraction;
    }

    public int getTotalInteractions() {
        return totalInteractions;
    }

    public void setTotalInteractions(int totalInteractions) {
        this.totalInteractions = totalInteractions;
    }

    public int getActiveModules() {
        return activeModules;
    }

    public void setActiveModules(int activeModules) {
        this.activeModules = activeModules;
    }

    public String getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(String lastInteraction) {
        this.lastInteraction = lastInteraction;
    }
}
