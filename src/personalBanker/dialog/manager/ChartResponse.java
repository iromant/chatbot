package personalBanker.dialog.manager;

public class ChartResponse {
    private final String statistics;
    @Deprecated
    private final String chartPath;

    public ChartResponse(String statistics, String chartPath) {
        this.statistics = statistics;
        this.chartPath = null;
    }

    public String getStatistics() {
        return statistics;
    }

    @Deprecated
    public String getChartPath() {
        return null;
    }

    @Deprecated
    public boolean hasChart() {
        return false;
    }
}