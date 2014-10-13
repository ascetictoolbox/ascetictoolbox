package api;

import es.bsc.amon.gui.GuiMetricsDBMapper;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.text.ParseException;

/**
 * Created by mmacias on 13/10/14.
 */
public class Gui extends Controller {
    public static Result getAllMetricPanels() throws ParseException {
        return ok(GuiMetricsDBMapper.getInstance().getPanels());
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result addMetricPanel() {
        try {
            String body = request().body().asJson().toString();
            return ok(GuiMetricsDBMapper.getInstance().addPanel(body));
        } catch(Exception e) {
            e.printStackTrace();
            return internalServerError(e.getMessage());
        }
    }

    public static Result deleteMetricPanel(String id) {
        GuiMetricsDBMapper.getInstance().deletePanel(id);
        return ok();
    }
}
