package SearchEngine.data.output;

/**
 * Created by sebastian on 30.11.2015.
 */
public class AnsiEscapeFormat implements OutputFormat {
    @Override
    public String getEnd() {
        return "\033[39;0m";
    }

    @Override
    public String getTitleStandard() {
        return "\033[30;1m";
    }

    @Override
    public String getTitleHighlight() {
        return "\033[34;1m";
    }

    @Override
    public String getTextStandard() {
        return "\033[39;0m";
    }

    @Override
    public String getTextHighlight() {
        return "\033[34;0m";
    }
}
