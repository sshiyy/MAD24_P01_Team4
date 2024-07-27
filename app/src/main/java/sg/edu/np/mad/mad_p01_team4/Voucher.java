package sg.edu.np.mad.mad_p01_team4;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("discountAmt", discountAmt);
        return map;
    }
}
