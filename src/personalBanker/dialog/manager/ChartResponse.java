package personalBanker.dialog.manager;

public class ChartResponse {
    private final String statistics;
    private final String chartPath;

    public ChartResponse(String statistics, String chartPath) {
        this.statistics = statistics;
        this.chartPath = chartPath;
    }

    public String getStatistics() {
        return statistics;
    }

    public String getChartPath() {
        return chartPath;
    }

    public boolean hasChart() {
        return chartPath != null;
    }
}