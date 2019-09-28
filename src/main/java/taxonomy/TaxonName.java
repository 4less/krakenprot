package taxonomy;

public class TaxonName {
    private String name;
    private String uniqueName;
    private String type;

    public TaxonName(String name, String uniqueName, String type) {
        this.name = name;
        this.uniqueName = uniqueName;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getType() {
        return type;
    }
}