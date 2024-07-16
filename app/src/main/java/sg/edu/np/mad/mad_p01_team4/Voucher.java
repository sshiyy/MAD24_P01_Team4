package sg.edu.np.mad.mad_p01_team4;

public class Voucher {
    private String title;
    private String discount;

    // Required for Firestore serialization
    public Voucher() {}

    public Voucher(String title, String discount) {
        this.title = title;
        this.discount = discount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
