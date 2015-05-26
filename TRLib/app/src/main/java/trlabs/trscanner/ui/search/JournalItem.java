package trlabs.trscanner.ui.search;


public class JournalItem {
    private String content;
    private String date;

    public JournalItem(String content, String date) {
        this.content = content;
        this.date = date;
    }

    public String getContent() { return this.content; }
    public String getDate() { return this.date; }
}
