/**
 * @author Anas EL HOUDI
 * @description DTO representing a 3-month archive period with metadata.
 * Used by the Archive section to display available periods for export.
 */
package peps.peps_back.controllers;

import java.util.Date;

public class ArchivePeriodDTO {

    private String periodId; // e.g., "2025-10" (start month)
    private String periodLabel; // e.g., "Octobre - Décembre 2025"
    private Date startDate;
    private Date endDate;
    private int interactionCount; // Number of interactions in this period

    public ArchivePeriodDTO() {
    }

    public ArchivePeriodDTO(String periodId, String periodLabel, Date startDate, Date endDate, int interactionCount) {
        this.periodId = periodId;
        this.periodLabel = periodLabel;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interactionCount = interactionCount;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }
}
