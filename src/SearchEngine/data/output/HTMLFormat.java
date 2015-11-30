package SearchEngine.data.output;

/**
 * Created by sebastian on 30.11.2015.
 */
public class HTMLFormat implements OutputFormat {
    @Override
    public String getEnd() {
        return "</span>";
    }

    @Override
    public String getTitleStandard() {
        return "<span style=\"font-weight:bold;\">";
    }

    @Override
    public String getTitleHighlight() {
        return "<span style=\"font-weight:bold; color: blue;\">";
    }

    @Override
    public String getTextStandard() {
        return "<span>";
    }

    @Override
    public String getTextHighlight() {
        return "<span style=\"color: blue;\">";
    }
}
