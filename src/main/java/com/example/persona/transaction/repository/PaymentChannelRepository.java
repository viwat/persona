package com.example.persona.transaction.repository;

import com.example.persona.transaction.model.PaymentChannel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentChannelRepository extends JpaRepository<PaymentChannel, Long> {
    boolean existsByChannelCode(String channelCode);

    Optional<PaymentChannel> findByChannelCode(String channelCode);
}
