package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by mmacias on 08/06/14.
 */
public class MetricsController extends Controller {
    public static Result getAllMetricsHistory(String oper, String appId, String nodeId, Long start, Long end, Long resolution) {
        if(start < 0L) {
            start = 0L;
        }
        if(end < 0L) {
            end = System.currentTimeMillis();
        }

        StringBuilder stringBuilder = new StringBuilder("oper: ").append(oper)
                .append("\nappId: ").append(appId)
                .append("\nnodeId: ").append(nodeId)
                .append("\nstart: ").append(start)
                .append("\nend: ").append(end).append("\n");
        return ok(stringBuilder.toString());

    }

    private static final String OP_SUM = "sum";
    private static final String OP_AVG = "avg";
    private static final String OP_COUNT = "count";
    private static final String OP_HIST = "hist"; //histogram

}
