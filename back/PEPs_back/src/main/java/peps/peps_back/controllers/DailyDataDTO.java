/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the DailyDataDTO class, which is a Data Transfer Object for daily statistics.
 */
package peps.peps_back.controllers;

public class DailyDataDTO {
    private String time;
    private int count;

    public DailyDataDTO() {
    }

    public DailyDataDTO(String time, int count) {
        this.time = time;
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
