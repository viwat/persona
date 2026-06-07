package com.example.persona.favorite.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.favorite.model.UserFavorite;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteRepository
        extends JpaRepository<@NonNull UserFavorite, @NonNull Long>, JpaSpecificationExecutor<@NonNull UserFavorite> {
    List<UserFavorite> findByCustomerKeyAndStatus(String customerKey, StatusType status);

    default List<UserFavorite> findByCustomerKeyAndActive(String customerKey) {
        return findByCustomerKeyAndStatus(customerKey, StatusType.ACTIVE);
    }

    List<UserFavorite> findByCustomerKeyAndServiceType(String customerKey, String serviceType);

    Optional<UserFavorite> findByCustomerKeyAndServiceTypeAndServiceCodeAndReferenceId(
            String customerKey, String serviceType, String serviceCode, String referenceId);

    List<UserFavorite> findByCustomerKeyOrderByDisplayOrderAsc(String customerKey);

    List<UserFavorite> findByCustomerKeyAndIsPinnedTrue(String customerKey);

    boolean existsByCustomerKeyAndServiceTypeAndServiceCodeAndReferenceId(
            String customerKey, String serviceType, String serviceCode, String referenceId);

    Optional<UserFavorite> findByIdAndCustomerKeyAndStatus(Long id, String customerKey, StatusType status);

    Optional<UserFavorite> findByIdAndServiceTypeAndServiceCodeAndCustomerKeyAndStatus(
            Long id, String serviceType, String serviceCode, String customerKey, StatusType status);

    List<UserFavorite> findByCustomerKeyAndStatusOrderByDisplayOrder(String customerKey, StatusType status);

    List<UserFavorite> findByCustomerNoAndStatusOrderByDisplayOrder(String customerKey, StatusType status);

    @Query("SELECT MAX(uf.displayOrder) FROM UserFavorite uf WHERE uf.customerKey = :customerKey")
    Optional<Integer> findMaxDisplayOrderByCustomerKey(@Param("customerKey") String customerKey);

    List<UserFavorite> findByCustomerKeyAndServiceTypeAndStatusOrderByDisplayOrder(
            String customerKey, String serviceType, StatusType status);

    @Query("SELECT uf FROM UserFavorite uf " + "WHERE uf.customerKey = :customerKey AND uf.status = :status "
            + "ORDER BY uf.isPinned DESC, uf.displayOrder ASC")
    List<UserFavorite> findByCustomerKeyOrderByPinnedAndDisplayOrder(
            @Param("customerKey") String customerKey, @Param("status") StatusType status);
}
