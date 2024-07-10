package sg.edu.np.mad.mad_p01_team4;

public class MessageModel {
    String message;
    String viewType;
    String time;

    public MessageModel(String message, String viewType, String time) {
        this.message = message;
        this.viewType = viewType;
        this.time = time;
    }

    public MessageModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
