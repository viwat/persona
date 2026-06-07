package com.example.persona.migration.repository.oracle;

import com.example.persona.migration.model.oracle.MtxVirtualCard;
import com.example.persona.migration.model.oracle.MtxVirtualCardId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MtxVirtualCardRepository extends JpaRepository<MtxVirtualCard, MtxVirtualCardId> {

    MtxVirtualCard findByAccountNo(String accountNo);

    List<MtxVirtualCard> findAllByAccountNo(String accountNo);

    List<MtxVirtualCard> findByMasterAccId(String masterAccId);

    List<MtxVirtualCard> findByTrackingNumber(String trackingNumber);
}
