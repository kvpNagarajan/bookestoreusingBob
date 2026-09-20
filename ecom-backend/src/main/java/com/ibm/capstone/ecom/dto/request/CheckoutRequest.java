package com.ibm.capstone.ecom.dto.request;

import com.ibm.capstone.ecom.entity.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull
    private Long shippingAddressId;

    @NotNull
    private Payment.PaymentMethod paymentMethod;

    /** Optional: number of gift points to redeem */
    private Integer giftPointsToRedeem;

    private String notes;
}
