package apache.celix.model;

/**
 * Created by mjansen on 27-10-15.
 */
public class OsgiBundle {
    private String status;
    private String symbolicName;
    private long id;

    public OsgiBundle(String symbolicName, String status, long id) {
        this.symbolicName = symbolicName;
        this.status = status;
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {

        return id;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return id + " " + status + " " + symbolicName;
    }
}
