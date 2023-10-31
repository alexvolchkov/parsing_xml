package volchkov.model;

/**
 * @author Aleksandr Volchkov
 */
public enum NameField {
    OBJECTID("OBJECTID"),
    PARENTOBJID("PARENTOBJID"),
    NAME("NAME"),
    TYPENAME("TYPENAME"),
    STARTDATE("STARTDATE"),
    ENDDATE("ENDDATE");

    private final String title;

    NameField(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
