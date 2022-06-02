package vn.tdtu.student.note.Trash;

public class Trash {
    String id;
    String title;
    String label;
    String content;
    String day;
    String time;
    boolean isDone;

    public Trash() {}

    public Trash(String id, String title, String label, String content, String day, String time, boolean isDone) {
        this.id = id;
        this.title = title;
        this.label = label;
        this.content = content;
        this.day = day;
        this.time = time;
        this.isDone = isDone;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
