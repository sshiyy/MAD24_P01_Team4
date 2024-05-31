package sg.edu.np.mad.mad_p01_team4;

public class Voucher {
    public String name;
    public int pointCost;
    public String expiryDate;
    public boolean redeemed;
    public Voucher(String name, int pointCost, String expiryDate, boolean redeemed){
        this.name = name;
        this.pointCost = pointCost;
        this.expiryDate = expiryDate;
        this.redeemed = redeemed;
    }
}

