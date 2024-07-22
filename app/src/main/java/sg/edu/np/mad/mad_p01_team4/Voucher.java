package sg.edu.np.mad.mad_p01_team4;

public class Voucher {
    private String title;
    private String description;
    private int discountAmt;

    // Required for Firestore serialization
    public Voucher() {}

    public Voucher(String title, String description, int discountAmt) {
        this.title = title;
        this.description = description;
        this.discountAmt = discountAmt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDiscountAmt() {
        return discountAmt;
    }

    public void setDiscountAmt(int discountAmt) {
        this.discountAmt = discountAmt;
    }
}
