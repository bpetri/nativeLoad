package apache.celix.model;

/**
 * Created by mjansen on 27-10-15.
 */
public class OsgiBundle {
    private String status;
    private String symbolicName;
    private long id;
    private String location;

    public OsgiBundle(String symbolicName, String status, long id, String location) {
        this.symbolicName = symbolicName;
        this.status = status;
        this.id = id;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public long getId() {
        return id;
    }

    public String getFilename() {
        String[] split = location.split("/");
        return split[split.length -1];
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return id + " " + status + " " + symbolicName + " " + getFilename();
    }
}
