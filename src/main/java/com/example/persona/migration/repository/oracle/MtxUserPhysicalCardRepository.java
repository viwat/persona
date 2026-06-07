package com.example.persona.migration.repository.oracle;

import com.example.persona.migration.model.oracle.MtxUserPhysicalCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MtxUserPhysicalCardRepository extends JpaRepository<MtxUserPhysicalCard, String> {

    MtxUserPhysicalCard findByAccountNo(String accountNo);

    List<MtxUserPhysicalCard> findAllByAccountNo(String accountNo);

    List<MtxUserPhysicalCard> findByMasterAccountId(String masterAccountId);
}
