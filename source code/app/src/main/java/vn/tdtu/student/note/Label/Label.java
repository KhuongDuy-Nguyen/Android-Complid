package vn.tdtu.student.note.Label;

public class Label {
    public String name;
    public String id;
    public int number_note;
    public int number_note_done;

    public Label() {}

    public Label(String id, String name, int number_note, int number_note_done) {
        this.id = id;
        this.name = name;
        this.number_note = number_note;
        this.number_note_done = number_note_done;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber_note() {
        return number_note;
    }

    public void setNumber_note(int number_note) {
        this.number_note = number_note;
    }

    public int getNumber_note_done() {
        return number_note_done;
    }

    public void setNumber_note_done(int number_note_done) {
        this.number_note_done = number_note_done;
    }
}
