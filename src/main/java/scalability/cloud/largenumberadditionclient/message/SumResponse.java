package scalability.cloud.largenumberadditionclient.message;

import java.math.BigInteger;

public class SumResponse {

    private BigInteger sum;

    public SumResponse() {
        this.sum = BigInteger.ZERO;
    }

    public SumResponse(BigInteger sum) {
        this.sum = sum;
    }
    public BigInteger getSum() {
        return sum;
    }
    public void setSum(BigInteger sum) {
        this.sum = sum;
    }
}
