package com.example.persona.model;

import com.example.persona.dto.CustomerBaseRequest;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseCustomerEntity extends BaseModel {

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @Column(name = "customer_no")
    private String customerNo;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "master_account_no")
    private String masterAccountNo;

    @Column(name = "channel_code")
    private String channelCode;

    @Column(name = "variant")
    private String variant;

    @Column(name = "code")
    private String code;

    @Column(name = "device_id")
    private String deviceId;

    public void fromCustomerRequest(CustomerBaseRequest request) {
        this.customerKey = request.getCustomerKey();
        this.customerNo = request.getCustomerNo();
        this.accountNo = request.getAccountNo();
        this.phoneNo = request.getPhoneNo();
        this.masterAccountNo = request.getMasterAccountNo();
        this.channelCode = request.getChannelCode();
    }
}
