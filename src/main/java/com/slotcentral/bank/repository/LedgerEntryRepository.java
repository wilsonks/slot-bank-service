package com.slotcentral.bank.repository;

import com.slotcentral.bank.domain.EntryType;
import com.slotcentral.bank.domain.LedgerEntry;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>, JpaSpecificationExecutor<LedgerEntry> {
    Optional<LedgerEntry> findByReferenceIdAndEntryType(String referenceId, EntryType entryType);

    default Page<LedgerEntry> findWithFilters(
            String accountUid,
            EntryType entryType,
            String egmId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable) {
        return findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (accountUid != null) {
                predicates.add(cb.equal(root.get("accountUid"), accountUid));
            }
            if (entryType != null) {
                predicates.add(cb.equal(root.get("entryType"), entryType));
            }
            if (egmId != null) {
                predicates.add(cb.equal(root.get("egmId"), egmId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }
}
