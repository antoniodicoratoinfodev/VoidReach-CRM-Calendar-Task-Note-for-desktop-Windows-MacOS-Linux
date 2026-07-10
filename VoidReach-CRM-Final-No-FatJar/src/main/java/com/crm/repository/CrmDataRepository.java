package com.crm.repository;

import com.crm.model.CrmDataSnapshot;

/** Owner-scoped boundary; a JDBC implementation will filter every query by owner_user_id. */
public interface CrmDataRepository {
    CrmDataSnapshot loadForUser(String userId);
    void saveForUser(String userId, CrmDataSnapshot data);
}
